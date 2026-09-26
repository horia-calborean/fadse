"""Follow-up review regressions: queued deadlines, cancellation, and plotting."""
import csv
import json
import os
from pathlib import Path
import sys
import time
import types
import unittest
from unittest import mock

from test_runner_accounting import RunnerFixture
from result_store import file_lock


class QueuedRunnerTest(RunnerFixture):
    def sleeper(self):
        self.fake.write_text(f'''#!{sys.executable}
import os, pathlib, sys, time
out = pathlib.Path(next(a.split("=", 1)[1] for a in sys.argv if a.startswith("--outdir=")))
(out / "ready").write_text(str(os.getpid()))
time.sleep(30)
''')

    def wait_for_child(self, candidate):
        marker = Path(candidate["gem5"]["output_dir"]) / "ready"
        deadline = time.monotonic() + 5
        while not marker.exists() and time.monotonic() < deadline:
            time.sleep(0.01)
        self.assertTrue(marker.exists(), "fake gem5 did not start")
        return int(marker.read_text())

    def test_queued_retry_uses_remaining_request_time(self):
        self.sleeper()
        candidate = self.candidate()
        candidate["gem5"]["timeout_seconds"] = 2
        first = self.start(candidate)
        self.wait_for_child(candidate)
        started = time.monotonic()
        second = self.start(candidate)
        self.finish(first, 124)
        self.finish(second, 124)
        self.assertLess(time.monotonic() - started, 3.5, "lock wait must not grant a fresh simulation timeout")
        ledger = json.loads(self.ledger.read_text())
        self.assertGreaterEqual(ledger["used"], 1)
        for launch in ledger["launches"]:
            with self.assertRaises(ProcessLookupError): os.kill(launch["pid"], 0)

    def test_lock_timeout_does_not_touch_owners_result_or_launch(self):
        candidate = self.candidate()
        candidate["gem5"]["timeout_seconds"] = 1
        output = Path(candidate["gem5"]["output_dir"])
        with file_lock(output / ".candidate.lock"):
            result = output / "fadse_result.json"
            result.write_text('{"status":"active-owner"}')
            self.finish(self.start(candidate), 124)
            self.assertEqual('{"status":"active-owner"}', result.read_text())
        self.assertEqual(0, json.loads(self.ledger.read_text())["used"])

    def test_failed_launch_record_reaps_the_started_child(self):
        import result_store
        self.sleeper()
        candidate = self.candidate()
        (self.root / "a").mkdir()
        command = [str(self.fake), "--outdir=" + candidate["gem5"]["output_dir"]]
        processes = []
        real_popen = result_store.subprocess.Popen
        def capture(*args, **kwargs):
            process = real_popen(*args, **kwargs)
            processes.append(process)
            return process
        with mock.patch.object(result_store.subprocess, "Popen", side_effect=capture), mock.patch.object(
                result_store, "atomic_json", side_effect=OSError("ledger unavailable")):
            with self.assertRaises(OSError):
                result_store.start_simulator(command, candidate, time.monotonic() + 5)
        self.assertEqual(1, len(processes))
        self.assertIsNotNone(processes[0].poll())
        for stream in (processes[0].stdout, processes[0].stderr): stream.close()

    def test_termination_reaps_child_before_releasing_candidate(self):
        self.sleeper()
        candidate = self.candidate()
        runner = self.start(candidate)
        pid = self.wait_for_child(candidate)
        runner.terminate()
        self.finish(runner, 143)
        with file_lock(Path(candidate["gem5"]["output_dir"]) / ".candidate.lock", time.monotonic() + 1):
            with self.assertRaises(ProcessLookupError): os.kill(pid, 0)
