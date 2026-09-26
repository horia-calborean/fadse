import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import time
import unittest
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from candidate_hash import candidate_id, content_hash
from parse_gem5_stats import (
    WORST_OBJECTIVE,
    build_result,
    parse_stats_file,
    published_values,
    write_outputs,
)
from result_store import append_csv, terminate_simulator
from run_gem5_candidate import ensure_requested_objectives
from test_runner_accounting import ARCHITECTURE


class ObjectiveIntegrityTest(unittest.TestCase):
    def test_incomplete_framed_stats_cannot_fall_back_to_unframed_success(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "stats.txt"
            counters = "simInsts 100\nsystem.cpu.numCycles 1000\nsystem.l2.overallMisses::total 2\n"
            path.write_text("---------- Begin Simulation Statistics ----------\n" + counters)
            self.assertEqual(({}, 0), parse_stats_file(path))
            # Keep support for genuine unframed statistics.
            path.write_text(counters)
            stats, count = parse_stats_file(path)
            self.assertEqual(1, count)
            self.assertEqual("ok", build_result(stats, count, ARCHITECTURE)["status"])

    def test_incomplete_tail_does_not_override_the_last_complete_dump(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "stats.txt"
            path.write_text("---------- Begin Simulation Statistics ----------\nsimInsts 100\n"
                            "---------- End Simulation Statistics ----------\n"
                            "---------- Begin Simulation Statistics ----------\nsimInsts 999\n")
            self.assertEqual(({"simInsts": 100.0}, 1), parse_stats_file(path))

    def test_malformed_l2_text_is_not_treated_as_an_absent_counter(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "stats.txt"
            path.write_text("---------- Begin Simulation Statistics ----------\n"
                            "simInsts 1000\nsystem.cpu.numCycles 2000\n"
                            "system.l2.overallMisses::total invalid\n"
                            "---------- End Simulation Statistics ----------\n")
            stats, count = parse_stats_file(path)
            self.assertEqual("failed_invalid_l2_counter", build_result(stats, count, ARCHITECTURE)["status"])

    def test_present_invalid_l2_counters_are_not_zero_suppressed(self):
        for value in (float("nan"), float("inf"), -1):
            for name in ("system.l2.overallMisses::total", "system.ruby.l2_cntrl0.L2cache.m_demand_misses"):
                with self.subTest(value=value, name=name), tempfile.TemporaryDirectory() as folder:
                    stats = {"simInsts": 1000, "system.cpu.numCycles": 2000, name: value}
                    result = build_result(stats, 1, ARCHITECTURE)
                    self.assertEqual("failed_invalid_l2_counter", result["status"])
                    self.assertIsNone(result["objectives"]["l2_mpki_pos"])
                    path = Path(folder)
                    write_outputs(result, path / "result.properties", path / "result.json")
                    objectives = (path / "result.properties").read_text().splitlines()[1:]
                    self.assertTrue(all(float(line.split("=", 1)[1]) == WORST_OBJECTIVE for line in objectives))

    def test_invalid_primary_counter_cannot_be_hidden_by_a_valid_fallback(self):
        stats = {"simInsts": 1000, "system.cpu.numCycles": 2000,
                 "system.l2.overallMisses::total": float("nan"), "system.l2.demandMisses::total": 3}
        self.assertEqual("failed_invalid_l2_counter", build_result(stats, 1, ARCHITECTURE)["status"])

    def test_actual_zero_and_suppressed_zero_remain_valid(self):
        base = {"simInsts": 1000, "system.cpu.numCycles": 2000, "system.l2.tags.tagsInUse": 4}
        for extra in ({}, {"system.l2.overallMisses::total": 0}):
            result = build_result(dict(base, **extra), 1, ARCHITECTURE)
            self.assertEqual("ok", result["status"])
            self.assertEqual(1e-9, result["objectives"]["l2_mpki_pos"])

    def test_properties_publish_supported_metrics_as_objectives(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder)
            result = build_result({
                "simInsts": 1000,
                "system.cpu.numCycles": 2000,
                "system.cpu.ipc": 0.5,
                "system.l2.overallMisses::total": 4,
                "simSeconds": 0.25,
            }, 1, ARCHITECTURE)
            write_outputs(result, path / "result.properties", path / "result.json")
            properties = dict(
                line.split(" = ", 1)
                for line in (path / "result.properties").read_text().splitlines()
            )
            self.assertEqual("0.5", properties["ipc"])
            self.assertEqual("0.25", properties["sim_seconds"])

    def test_requested_objective_validation_uses_the_published_aliases(self):
        result = {
            "status": "ok",
            "metrics": {"sim_seconds": 0.25},
            "objectives": {},
        }
        self.assertEqual(0.25, published_values(result)["simSeconds"])
        for objective in ("sim_seconds", "simSeconds"):
            candidate = {"objectives": [objective]}
            checked = ensure_requested_objectives(dict(result), candidate)
            self.assertEqual("ok", checked["status"])

    def test_failed_receipts_use_the_configured_objective_set(self):
        result = {
            "status": "timeout",
            "metrics": {"sim_seconds": 2.0},
            "objectives": {"cpi": 2.0},
        }
        checked = ensure_requested_objectives(result, {"objectives": ["sim_seconds"]})
        self.assertEqual({"sim_seconds": WORST_OBJECTIVE}, checked["objectives"])

    def test_reaped_leader_still_has_its_process_group_cleaned_once(self):
        process = mock.Mock()
        process.pid = 1234
        process.poll.return_value = 0
        with mock.patch("result_store.os.killpg") as killpg:
            terminate_simulator(process)
            terminate_simulator(process)
        killpg.assert_called_once_with(1234, signal.SIGKILL)
        process.wait.assert_not_called()

    def test_reaped_leader_does_not_leave_a_running_descendant(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            marker = root / "child.pid"
            leader = root / "leader.py"
            leader.write_text(
                "import pathlib, subprocess, sys\n"
                "child = subprocess.Popen(['sleep', '30'])\n"
                "pathlib.Path(sys.argv[1]).write_text(str(child.pid))\n"
            )
            process = subprocess.Popen(
                [sys.executable, str(leader), str(marker)], start_new_session=True)
            try:
                deadline = time.monotonic() + 3
                while not marker.exists() and time.monotonic() < deadline:
                    time.sleep(0.01)
                self.assertTrue(marker.exists())
                child_pid = int(marker.read_text())
                process.wait(timeout=3)

                terminate_simulator(process)

                deadline = time.monotonic() + 3
                state = None
                while time.monotonic() < deadline:
                    stat = Path(f"/proc/{child_pid}/stat")
                    if not stat.exists():
                        state = "gone"
                        break
                    state = stat.read_text().split()[2]
                    if state == "Z":
                        break
                    time.sleep(0.02)
                self.assertIn(state, ("gone", "Z"))
            finally:
                try:
                    os.killpg(process.pid, signal.SIGKILL)
                except ProcessLookupError:
                    pass

    def test_repair_csv_deduplicates_keys_within_and_across_appends(self):
        with tempfile.TemporaryDirectory() as folder:
            from run_gem5_candidate import append_repair_log
            path = Path(folder) / "repairs.csv"
            repair = "same-repair"
            append_repair_log(path, None, None, {}, {}, [repair, repair])
            append_repair_log(path, None, None, {}, {}, [repair])

            self.assertEqual(2, len(path.read_text().splitlines()))

    def test_csv_schema_change_rotates_the_old_file_before_appending(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "evaluated.csv"
            path.write_text("candidate_id,generation,status\nold,1,ok\n")
            append_csv(path, ["candidate_id", "status"], [
                {"candidate_id": "new", "status": "ok"}
            ])
            self.assertEqual(
                ["candidate_id,status", "new,ok"],
                path.read_text().splitlines(),
            )
            backups = list(path.parent.glob(path.name + ".schema-mismatch-*.bak"))
            self.assertEqual(1, len(backups))
            self.assertIn("old,1,ok", backups[0].read_text())

    def test_ipc_is_derived_when_only_cycles_and_instructions_are_available(self):
        result = build_result({
            "simInsts": 1000,
            "system.cpu.numCycles": 2000,
            "system.l2.overallMisses::total": 4,
        }, 1, ARCHITECTURE)
        self.assertEqual("ok", result["status"])
        self.assertEqual(0.5, result["metrics"]["ipc"])
        self.assertEqual("1/cpi", result["counter_mapping"]["ipc"])

    def test_content_changes_invalidate_identity_even_with_unchanged_size_and_mtime(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            config = root / "config"; config.mkdir()
            cwd = root / "inputs"; cwd.mkdir()
            files = [root / "gem5", root / "binary", config / "main.py", config / "helper.py", cwd / "input"]
            for path in files: path.write_text("before")
            candidate = dict(benchmark="bfs", gem5=dict(gem5_binary=str(files[0]),
                benchmark_binary=str(files[1]), config_script=str(files[2]), run_cwd=str(cwd), stdin="input"))
            before = candidate_id(candidate)
            for path in files:
                stamp = path.stat()
                path.write_text("after!")
                os.utime(path, ns=(stamp.st_atime_ns, stamp.st_mtime_ns))
                self.assertNotEqual(before, candidate_id(candidate), str(path))
                path.write_text("before")
                self.assertEqual(before, candidate_id(candidate))
            (config / "__pycache__").mkdir()
            (config / "__pycache__/helper.pyc").write_bytes(b"compiled")
            self.assertEqual(before, candidate_id(candidate))
            (cwd / "new-input").write_text("new")
            self.assertEqual(before, candidate_id(candidate))

    def test_tree_hash_matches_java_vector_and_follows_input_symlinks(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / "a").write_text("abc")
            (root / "z").mkdir()
            (root / "z/☃").write_text("data")
            self.assertEqual("ca1dc8bf41fda5815f6c9f53c753d983fd39921d6a48426e1991af9f1ee844bb", content_hash(str(root)))
            (root / "link").symlink_to(root / "a")
            before = content_hash(str(root))
            (root / "a").write_text("xyz")
            self.assertNotEqual(before, content_hash(str(root)))
            (root / "cycle").symlink_to(root, target_is_directory=True)
            with self.assertRaises(ValueError): content_hash(str(root))
