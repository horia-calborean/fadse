"""Process-safe publication and physical-launch accounting for Linux gem5 workers."""

from contextlib import contextmanager
import csv
import fcntl
import json
import os
import signal
import time
from pathlib import Path
import subprocess
import tempfile
import uuid


@contextmanager
def file_lock(path, deadline=None):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    # Keep the lock inode: deleting it would allow two independent lock owners.
    with path.open("a") as handle:
        if deadline is None:
            fcntl.flock(handle, fcntl.LOCK_EX)
        else:
            while True:
                if time.monotonic() >= deadline:
                    raise TimeoutError("Request deadline expired waiting for " + str(path))
                try:
                    fcntl.flock(handle, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    break
                except BlockingIOError:
                    time.sleep(min(0.05, max(0, deadline - time.monotonic())))
        try:
            yield
        finally:
            fcntl.flock(handle, fcntl.LOCK_UN)


def atomic_text(path, text):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary = tempfile.mkstemp(prefix=path.name + ".", dir=path.parent)
    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as handle:
            handle.write(text)
            handle.flush()
            os.fsync(handle.fileno())
        os.replace(temporary, path)
    finally:
        Path(temporary).unlink(missing_ok=True)


def atomic_json(path, value):
    atomic_text(path, json.dumps(value, indent=2, sort_keys=True) + "\n")


def append_csv(path, columns, rows, unique_key=None):
    """Append whole records under the POSIX lock also used by Java FileChannel."""
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with Path(str(path) + ".lock").open("a") as lock:
        fcntl.lockf(lock, fcntl.LOCK_EX)
        try:
            write_header = not path.exists() or path.stat().st_size == 0
            if not write_header:
                with path.open(newline="", encoding="utf-8") as existing:
                    header = next(csv.reader(existing), [])
                if header != list(columns):
                    rotated = path.with_name(
                        path.name + ".schema-mismatch-" + uuid.uuid4().hex + ".bak"
                    )
                    os.replace(path, rotated)
                    write_header = True
            with path.open("a", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=columns)
                if write_header:
                    writer.writeheader()
                if unique_key:
                    known = set()
                    if not write_header:
                        with path.open(newline="", encoding="utf-8") as existing:
                            known = {
                                row.get(unique_key, "")
                                for row in csv.DictReader(existing)
                            }
                    unique_rows = []
                    for row in rows:
                        value = row.get(unique_key, "")
                        if value in known:
                            continue
                        known.add(value)
                        unique_rows.append(row)
                    rows = unique_rows
                writer.writerows(rows)
        finally:
            fcntl.lockf(lock, fcntl.LOCK_UN)


def start_simulator(command, candidate, deadline=None, stdout=None, stderr=None):
    """Admit and record an actual process start under the shared run quota.

    Failed execs never consume budget. Started processes consume budget even if
    they subsequently fail or time out. The ledger must be on the shared filesystem.
    """
    def launch():
        if deadline is not None and time.monotonic() >= deadline:
            raise TimeoutError("Request deadline expired before simulator launch")
        return subprocess.Popen(command,
                                stdout=subprocess.PIPE if stdout is None else stdout,
                                stderr=subprocess.PIPE if stderr is None else stderr,
                                text=True, start_new_session=True)

    budget_file = candidate.get("gem5", {}).get("budget_file")
    if not budget_file:
        return launch()
    path = Path(budget_file)
    with file_lock(str(path) + ".lock", deadline):
        state = json.loads(path.read_text())
        if state["run_id"] != candidate.get("run_id"):
            raise ValueError("Launch ledger belongs to a different run")
        if state["used"] >= state["limit"]:
            return None
        process = launch()
        try:
            state["used"] += 1
            state["launches"].append({"launch_id": uuid.uuid4().hex,
                                      "candidate_id": candidate["candidate_id"],
                                      "budget_index": state["used"], "pid": process.pid})
            atomic_json(path, state)
        except BaseException:
            terminate_simulator(process)
            raise
        return process


def terminate_simulator(process):
    """Reap gem5 and stop any descendants in its private process group."""
    previous = signal.pthread_sigmask(signal.SIG_BLOCK, {signal.SIGTERM, signal.SIGINT})
    try:
        if getattr(process, "_fadse_group_terminated", False) is True:
            return
        leader_reaped = process.poll() is not None
        try:
            os.killpg(process.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass
        process._fadse_group_terminated = True
        if not leader_reaped:
            process.wait()
    finally:
        signal.pthread_sigmask(signal.SIG_SETMASK, previous)


@contextmanager
def running_simulator(command, candidate, deadline, stdout=None, stderr=None):
    process = None
    try:
        # Defer cancellation until the newly spawned process has an owner.
        previous = signal.pthread_sigmask(signal.SIG_BLOCK, {signal.SIGTERM, signal.SIGINT})
        try:
            process = start_simulator(command, candidate, deadline, stdout, stderr)
        finally:
            signal.pthread_sigmask(signal.SIG_SETMASK, previous)
        yield process
    finally:
        if process is not None:
            terminate_simulator(process)


@contextmanager
def termination_signals():
    def cancel(signum, frame):
        # A repeated stop request must not interrupt child cleanup.
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
        signal.signal(signal.SIGINT, signal.SIG_IGN)
        raise SystemExit(128 + signum)
    previous = {sig: signal.signal(sig, cancel) for sig in (signal.SIGTERM, signal.SIGINT)}
    try:
        yield
    finally:
        for sig, handler in previous.items():
            signal.signal(sig, handler)
