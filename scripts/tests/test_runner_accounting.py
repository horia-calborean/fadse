"""Runner regressions use a local fake process; real gem5 is explicitly opt-in."""
import json
import csv
import multiprocessing
import os
import shutil
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import unittest
from unittest import mock

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS.parent / "gem5_configs"))
sys.path.insert(0, str(SCRIPTS))
from candidate_hash import candidate_id
from gem5_contract import validate_and_repair_architecture
from parse_gem5_stats import append_stat_mapping
from result_store import file_lock
from simulation_window import simulate_window

ARCHITECTURE = dict(width=4, rob_entries=128, iq_entries=64, lsq_entries=32,
                    l1i_kb=32, l1d_kb=32, l2_kb=512, l2_assoc=8, num_cores=1)
STATS = """---------- Begin Simulation Statistics ----------
system.cpu.numCycles 1000
simInsts 100
system.l2.overallMisses::total 2
---------- End Simulation Statistics ----------
"""


def normalized_candidate_id(candidate):
    normalized = dict(candidate)
    normalized["architecture"] = validate_and_repair_architecture(
        candidate.get("architecture", {})
    )[0]
    return candidate_id(normalized)


def mapping_worker(path, index, barrier):
    barrier.wait(timeout=10)
    append_stat_mapping(path, f"stats-{index}", {"counter_mapping": {"cpi": f"counter-{index}"}})


class RunnerFixture(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.fake = self.root / "fake.py"
        self.fake.write_text(f'''#!{sys.executable}
import pathlib, sys, time
out = pathlib.Path(next(a.split("=", 1)[1] for a in sys.argv if a.startswith("--outdir=")))
with (out.parent / "actual-launches").open("a") as log:
    log.write(str(out) + "\\n")
time.sleep(0.3)
(out / "stats.txt").write_text({STATS!r})
''')
        self.fake.chmod(0o755)
        self.ledger = self.root / "launches.json"
        self.ledger.write_text(json.dumps(dict(run_id="test", limit=20, used=0, launches=[])))

    def candidate(self, name="a", executable=None):
        return dict(candidate_id=name, benchmark="bfs", method="nsga2", run_id="test", seed=7,
                    architecture=dict(ARCHITECTURE), gem5=dict(gem5_binary=str(executable or self.fake),
                    config_script="unused", benchmark_binary="unused", benchmark_args="", warmup_insts=0,
                    maxinsts=1000, output_dir=str(self.root / name), timeout_seconds=10,
                    budget_file=str(self.ledger)))

    def start(self, candidate, response=None):
        request = self.root / ("request-" + str(len(list(self.root.glob("request-*")))) + ".json")
        request.write_text(json.dumps(candidate))
        return subprocess.Popen([sys.executable, str(SCRIPTS / "run_gem5_candidate.py"),
            "--candidate-json", str(request), "--result-json", str(Path(candidate["gem5"]["output_dir"]) / "fadse_result.json"),
            "--repairs-csv", str(self.root / "repairs.csv"),
            "--evaluated-csv", str(self.root / "evaluated.csv"),
            "--stat-mapping-out", str(self.root / "mapping.json")]
            + (["--response-properties", str(response)] if response else []),
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

    def finish(self, process, expected=0):
        stdout, stderr = process.communicate(timeout=20)
        self.assertEqual(expected, process.returncode, stdout + stderr)

    def result(self, candidate):
        return json.loads((Path(candidate["gem5"]["output_dir"]) / "fadse_result.json").read_text())


class RunnerAccountingTest(RunnerFixture):
    def test_host_time_objectives_preserve_the_gem5_statistic_on_fresh_and_cached_runs(self):
        stats = STATS.replace("system.cpu.numCycles 1000", "hostSeconds 123.75\nsystem.cpu.numCycles 1000")
        self.fake.write_text(self.fake.read_text().replace(repr(STATS), repr(stats)))
        for objective in ("host_seconds", "hostSeconds"):
            candidate = self.candidate(objective)
            candidate["objectives"] = [objective]
            response = self.root / (objective + ".properties")
            for attempt in range(2):
                self.finish(self.start(candidate, response))
                result = self.result(candidate)
                self.assertEqual("hostSeconds", result["counter_mapping"]["host_seconds"])
                self.assertEqual(123.75, result["metrics"]["host_seconds"])
                self.assertNotEqual(123.75, result["host_seconds"])
                self.assertIn(f"{objective} = 123.75\n", response.read_text())
            self.assertEqual(1, sum(line == candidate["gem5"]["output_dir"]
                                    for line in (self.root / "actual-launches").read_text().splitlines()))

    def test_missing_gem5_host_time_is_not_replaced_by_wrapper_time(self):
        candidate = self.candidate()
        candidate["objectives"] = ["host_seconds"]

        self.finish(self.start(candidate), 2)

        result = self.result(candidate)
        self.assertEqual("failed_missing_objective", result["status"])
        self.assertNotIn("host_seconds", result["metrics"])
        self.assertGreater(result["host_seconds"], 0)

    def test_partial_isolated_workdir_is_removed_when_copy_fails(self):
        candidate = self.candidate()
        source = self.root / "spec-run"
        source.mkdir()
        (source / "input.txt").write_text("input")
        candidate["gem5"].update(run_cwd=str(source), isolate_cwd=True)

        def fail_after_creating_destination(_source, destination, **_kwargs):
            Path(destination).mkdir(parents=True)
            (Path(destination) / "partial").write_text("partial")
            raise OSError("copy failed")

        request = self.root / "request-copy-failure.json"
        request.write_text(json.dumps(candidate))
        result_path = Path(candidate["gem5"]["output_dir"]) / "fadse_result.json"
        with mock.patch("run_gem5_candidate.shutil.copytree", fail_after_creating_destination):
            from run_gem5_candidate import run_locked
            args = mock.Mock(
                result_json=str(result_path), response_properties=None,
                evaluated_csv=None, repairs_csv=None, stat_mapping_out=None,
                budget_index=None, force=True,
            )
            run_locked(
                args, candidate, candidate["architecture"], [], [], result_path,
                Path(candidate["gem5"]["output_dir"]) / "fadse_result.properties",
                time.monotonic() + 10,
            )

        self.assertFalse((Path(candidate["gem5"]["output_dir"]) / "workdir").exists())
        self.assertEqual("failed_launch", json.loads(result_path.read_text())["status"])

    def test_cleanup_error_does_not_replace_the_primary_failure(self):
        self.fake.write_text(f"#!{sys.executable}\nraise SystemExit(7)\n")
        source = self.root / "spec-run"
        source.mkdir()
        (source / "input.txt").write_text("input")
        candidate = self.candidate()
        candidate["gem5"].update(run_cwd=str(source), isolate_cwd=True)
        result_path = Path(candidate["gem5"]["output_dir"]) / "fadse_result.json"
        from run_gem5_candidate import run_locked
        args = mock.Mock(response_properties=None, evaluated_csv=None, repairs_csv=None,
                         stat_mapping_out=None, budget_index=None, force=True)
        with mock.patch("run_gem5_candidate.remove_isolated_cwd",
                        side_effect=[None, OSError("cleanup denied")]):
            run_locked(
                args, candidate, candidate["architecture"], [], [], result_path,
                result_path.with_name("fadse_result.properties"), time.monotonic() + 10,
            )

        result = json.loads(result_path.read_text())
        self.assertEqual("failed_gem5_exit", result["status"])
        self.assertEqual("cleanup denied", result["cleanup_error"])

    def test_cleanup_error_does_not_discard_or_relaunch_a_success(self):
        source = self.root / "spec-run"
        source.mkdir()
        (source / "input.txt").write_text("input")
        candidate = self.candidate()
        candidate["gem5"].update(run_cwd=str(source), isolate_cwd=True)
        result_path = Path(candidate["gem5"]["output_dir"]) / "fadse_result.json"
        from run_gem5_candidate import run_locked
        args = mock.Mock(response_properties=None, evaluated_csv=None, repairs_csv=None,
                         stat_mapping_out=None, budget_index=None, force=False)
        with mock.patch("run_gem5_candidate.remove_isolated_cwd",
                        side_effect=[None, OSError("cleanup denied")]):
            first = run_locked(
                args, candidate, candidate["architecture"], [], [], result_path,
                result_path.with_name("fadse_result.properties"), time.monotonic() + 10,
            )

        result = json.loads(result_path.read_text())
        self.assertEqual(0, first)
        self.assertEqual("ok", result["status"])
        self.assertEqual("cleanup denied", result["cleanup_error"])
        self.assertLess(result["objectives"]["cpi"], sys.float_info.max)
        second = run_locked(
            args, candidate, candidate["architecture"], [], [], result_path,
            result_path.with_name("fadse_result.properties"), time.monotonic() + 10,
        )
        self.assertEqual(0, second)
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])

    def test_fresh_result_missing_requested_objective_is_rejected(self):
        candidate = self.candidate()
        candidate["objectives"] = ["sim_seconds"]
        response = self.root / "fresh-missing-objective.properties"

        self.finish(self.start(candidate, response), 2)

        result = self.result(candidate)
        self.assertEqual("failed_missing_objective", result["status"])
        self.assertEqual({"sim_seconds": sys.float_info.max}, result["objectives"])
        self.assertFalse(result["cache_hit"])

    def test_request_snapshots_cover_success_cache_hit_and_failure_without_being_overwritten(self):
        candidate = self.candidate()
        success, cached, failed = [self.root / name for name in ("success.properties", "cached.properties", "failed.properties")]
        self.finish(self.start(candidate, success))
        original = success.read_text()
        self.finish(self.start(candidate, cached))
        self.assertEqual(original, cached.read_text())
        state = json.loads(self.ledger.read_text()); state["limit"] = state["used"]
        self.ledger.write_text(json.dumps(state))
        candidate["gem5"]["benchmark_args"] = "different workload"
        self.finish(self.start(candidate, failed), 2)
        self.assertIn("status = budget_exhausted\n", failed.read_text())
        self.assertEqual(original, success.read_text())
        self.assertEqual(original, cached.read_text())
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])

    def test_lock_timeout_does_not_publish_a_response_or_reuse_shared_success(self):
        candidate = self.candidate()
        self.finish(self.start(candidate))
        shared = (self.root / "a/fadse_result.properties").read_text()
        candidate["gem5"]["timeout_seconds"] = 1
        response = self.root / "unowned.properties"
        with file_lock(self.root / "a/.candidate.lock"):
            self.finish(self.start(candidate, response), 124)
        self.assertFalse(response.exists())
        self.assertEqual(shared, (self.root / "a/fadse_result.properties").read_text())
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])

    def test_prior_cache_format_cannot_reuse_an_old_success(self):
        candidate = self.candidate()
        self.finish(self.start(candidate))
        path = Path(candidate["gem5"]["output_dir"]) / "fadse_result.json"
        cached = json.loads(path.read_text())
        with mock.patch("candidate_hash.CACHE_FORMAT", "fadse-gem5-v6"):
            cached["fingerprint"] = candidate_id(candidate)
        path.write_text(json.dumps(cached))
        self.finish(self.start(candidate))
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])
        self.assertEqual(normalized_candidate_id(candidate), self.result(candidate)["fingerprint"])

    def test_implicit_window_matches_explicit_defaults_in_execution_and_cache(self):
        candidate = self.candidate()
        del candidate["gem5"]["warmup_insts"]
        del candidate["gem5"]["maxinsts"]
        self.finish(self.start(candidate))
        result = self.result(candidate)
        self.assertIn("--warmup-insts=10000000", result["command_line"])
        self.assertIn("--maxinsts=50000000", result["command_line"])
        original_id = candidate_id(candidate)
        candidate["gem5"].update(warmup_insts=10000000, maxinsts=50000000)
        self.assertEqual(original_id, candidate_id(candidate))
        self.finish(self.start(candidate))
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])
        receipt = json.loads((self.root / "a/runs/test.json").read_text())
        self.assertTrue(receipt["cache_hit"])

    def test_enabled_prefetcher_degree_is_normalized_before_execution_and_cache(self):
        candidate = self.candidate()
        candidate["architecture"].update(prefetcher=1, prefetch_degree=0)
        self.finish(self.start(candidate))
        first = self.result(candidate)
        self.assertIn("--prefetch-degree=1", first["command_line"])
        self.assertAlmostEqual(8.205, first["objectives"]["cost_proxy"])

        candidate["architecture"]["prefetch_degree"] = 1
        self.finish(self.start(candidate))
        self.assertEqual(first["fingerprint"], self.result(candidate)["fingerprint"])

        del candidate["architecture"]["prefetch_degree"]
        self.finish(self.start(candidate))
        self.assertEqual(first["fingerprint"], self.result(candidate)["fingerprint"])
        self.assertEqual(1, len((self.root / "actual-launches").read_text().splitlines()))
        self.assertEqual(2, len((self.root / "repairs.csv").read_text().splitlines()))

        candidate["run_id"] = "second-run"
        self.finish(self.start(candidate))
        self.assertEqual(3, len((self.root / "repairs.csv").read_text().splitlines()))

    def test_missing_requested_objective_is_attributed_for_fresh_and_cached_results(self):
        candidate = self.candidate()
        self.finish(self.start(candidate))
        self.assertEqual(1, len((self.root / "actual-launches").read_text().splitlines()))

        candidate["objectives"] = ["sim_seconds"]
        response = self.root / "missing-objective.properties"
        self.finish(self.start(candidate, response), 2)

        result = json.loads((self.root / "a/runs/test.json").read_text())
        self.assertEqual("failed_missing_objective", result["status"])
        self.assertEqual(["sim_seconds"], result["missing_objectives"])
        self.assertTrue(all(value == sys.float_info.max
                            for value in result["objectives"].values()))
        self.assertTrue(result["cache_hit"])
        self.assertIn("status = failed_missing_objective", response.read_text())
        self.assertEqual(1, len((self.root / "actual-launches").read_text().splitlines()))

    def test_truncated_stats_are_rejected_and_not_reused_as_success(self):
        self.fake.write_text(f'''#!{sys.executable}
import pathlib, sys
out = pathlib.Path(next(a.split("=", 1)[1] for a in sys.argv if a.startswith("--outdir=")))
(out / "stats.txt").write_text({STATS.split('---------- End')[0]!r})
''')
        candidate = self.candidate()
        for expected_used in (1, 2):
            response = self.root / f"invalid-stats-{expected_used}.properties"
            self.finish(self.start(candidate, response), 2)
            self.assertIn("status = failed_missing_stats\n", response.read_text())
            result = self.result(candidate)
            self.assertEqual("failed_missing_stats", result["status"])
            self.assertFalse(result["cache_hit"])
            self.assertEqual(expected_used, json.loads(self.ledger.read_text())["used"])

    def test_concurrent_duplicate_misses_start_one_process_and_publish_complete_results(self):
        candidate = self.candidate()
        processes = [self.start(candidate) for _ in range(8)]
        for process in processes:
            self.finish(process)
        self.assertEqual(1, len((self.root / "actual-launches").read_text().splitlines()))
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])
        self.assertEqual("ok", self.result(candidate)["status"])
        self.assertEqual(normalized_candidate_id(candidate), self.result(candidate)["fingerprint"])
        receipt = json.loads((self.root / "a/runs/test.json").read_text())
        self.assertTrue(receipt["cache_hit"])
        with (self.root / "evaluated.csv").open() as handle:
            rows = list(csv.DictReader(handle))
        self.assertEqual(8, len(rows))
        self.assertTrue(all(row["candidate_id"] == candidate["candidate_id"] for row in rows))
        self.assertTrue(all(row["run_id"] == "test" for row in rows))
        self.assertTrue(all(None not in row for row in rows))

    def test_quota_is_atomic_across_distinct_candidates(self):
        state = json.loads(self.ledger.read_text()); state["limit"] = 2
        self.ledger.write_text(json.dumps(state))
        candidates = [self.candidate(str(i)) for i in range(8)]
        processes = [self.start(c) for c in candidates]
        for process in processes:
            process.communicate(timeout=20)
            self.assertIn(process.returncode, (0, 2))
        statuses = [self.result(c)["status"] for c in candidates]
        self.assertEqual(2, statuses.count("ok"))
        self.assertEqual(6, statuses.count("budget_exhausted"))
        self.assertEqual(2, len((self.root / "actual-launches").read_text().splitlines()))
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])

    def test_changed_workload_cannot_reuse_cached_objectives(self):
        candidate = self.candidate()
        self.finish(self.start(candidate))
        candidate["gem5"]["benchmark_args"] = "-g 20 -n 4"
        self.finish(self.start(candidate))
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])
        self.assertIn("--benchmark-args=-g 20 -n 4", self.result(candidate)["command_line"])

    def test_replacing_simulator_at_same_path_invalidates_success(self):
        candidate = self.candidate()
        self.finish(self.start(candidate))
        fingerprint = self.result(candidate)["fingerprint"]
        self.finish(self.start(candidate))
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])
        self.fake.write_text(f"#!{sys.executable}\nraise SystemExit(7)\n")
        self.finish(self.start(candidate), 2)
        self.assertEqual("failed_gem5_exit", self.result(candidate)["status"])
        self.assertNotEqual(fingerprint, self.result(candidate)["fingerprint"])
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])

    def test_invalid_candidate_and_failed_exec_do_not_consume_budget(self):
        candidate = self.candidate(); candidate["architecture"]["width"] = 99
        self.finish(self.start(candidate), 2)
        self.assertEqual("invalid_pre_gem5", self.result(candidate)["status"])
        candidate = self.candidate("missing", self.root / "absent")
        self.finish(self.start(candidate), 2)
        self.assertEqual("failed_launch", self.result(candidate)["status"])
        self.assertEqual(0, json.loads(self.ledger.read_text())["used"])

    def test_started_failure_and_timeout_consume_budget_and_clear_stale_stats(self):
        self.fake.write_text(f"#!{sys.executable}\nraise SystemExit(7)\n")
        candidate = self.candidate()
        out = Path(candidate["gem5"]["output_dir"]); out.mkdir()
        (out / "stats.txt").write_text(STATS)
        self.finish(self.start(candidate), 2)
        self.assertEqual("failed_gem5_exit", self.result(candidate)["status"])
        self.assertFalse((out / "stats.txt").exists())
        self.fake.write_text(f"#!{sys.executable}\nimport time\ntime.sleep(5)\n")
        candidate["gem5"]["timeout_seconds"] = 1
        response = self.root / "timeout.properties"
        self.finish(self.start(candidate, response), 124)
        self.assertIn("status = timeout\n", response.read_text())
        self.assertEqual("timeout", self.result(candidate)["status"])
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])

    def test_simulator_output_is_streamed_to_logs_with_bounded_result_tails(self):
        self.fake.write_text(f'''#!{sys.executable}
import pathlib, sys
out = pathlib.Path(next(a.split("=", 1)[1] for a in sys.argv if a.startswith("--outdir=")))
sys.stdout.write("A" * 100000 + "stdout-end")
sys.stderr.write("B" * 100000 + "stderr-end")
(out / "stats.txt").write_text({STATS!r})
''')
        candidate = self.candidate()
        self.finish(self.start(candidate))
        output = Path(candidate["gem5"]["output_dir"])
        self.assertEqual(100010, (output / "gem5.stdout.log").stat().st_size)
        self.assertEqual(100010, (output / "gem5.stderr.log").stat().st_size)
        result = self.result(candidate)
        self.assertEqual(4000, len(result["stdout_tail"]))
        self.assertEqual(4000, len(result["stderr_tail"]))
        self.assertTrue(result["stdout_tail"].endswith("stdout-end"))
        self.assertTrue(result["stderr_tail"].endswith("stderr-end"))

    def test_isolated_workdir_is_removed_after_candidate_finishes(self):
        source = self.root / "spec-run"
        source.mkdir()
        (source / "input.txt").write_text("reference input")
        candidate = self.candidate()
        candidate["gem5"].update(run_cwd=str(source), isolate_cwd=True)

        self.finish(self.start(candidate))

        self.assertFalse((Path(candidate["gem5"]["output_dir"]) / "workdir").exists())
        self.assertEqual("reference input", (source / "input.txt").read_text())

    def test_isolated_workdir_is_removed_after_failure_and_timeout(self):
        source = self.root / "spec-run"
        source.mkdir()
        (source / "input.txt").write_text("reference input")

        self.fake.write_text(f"#!{sys.executable}\nraise SystemExit(7)\n")
        failed = self.candidate("failed")
        failed["gem5"].update(run_cwd=str(source), isolate_cwd=True)
        self.finish(self.start(failed), 2)
        self.assertFalse((Path(failed["gem5"]["output_dir"]) / "workdir").exists())

        self.fake.write_text(f"#!{sys.executable}\nimport time\ntime.sleep(5)\n")
        timed_out = self.candidate("timeout")
        timed_out["gem5"].update(
            run_cwd=str(source), isolate_cwd=True, timeout_seconds=1
        )
        self.finish(self.start(timed_out), 124)
        self.assertFalse((Path(timed_out["gem5"]["output_dir"]) / "workdir").exists())

    def test_mapping_update_keeps_all_parallel_writers(self):
        context = multiprocessing.get_context("spawn")
        barrier = context.Barrier(8)
        path = self.root / "mapping.json"
        workers = [context.Process(target=mapping_worker, args=(path, i, barrier)) for i in range(8)]
        for worker in workers: worker.start()
        while any(worker.is_alive() for worker in workers):
            if path.exists(): json.loads(path.read_text())
        for worker in workers:
            worker.join(15)
            self.assertEqual(0, worker.exitcode)
        self.assertEqual({f"stats-{i}" for i in range(8)}, set(json.loads(path.read_text())))


class SimulationWindowTest(unittest.TestCase):
    def test_warmup_must_complete_before_stats_reset_and_measurement(self):
        class Event:
            def __init__(self, cause, code=0): self.cause, self.code = cause, code
            def getCause(self): return self.cause
            def getCode(self): return self.code
        class Engine:
            def __init__(self, events): self.events, self.calls, self.stats = iter(events), [], self
            def simulate(self): self.calls.append("simulate"); return next(self.events)
            def reset(self): self.calls.append("reset")
            def scheduleInstStop(self, _, instructions, cause): self.calls.append((instructions, cause))
        early = Engine([Event("exiting with last active thread context")])
        _, metadata, code = simulate_window(early, early, 100, 200)
        self.assertEqual("incomplete_window", metadata["status"])
        self.assertNotEqual(0, code)
        self.assertNotIn("reset", early.calls)
        full = Engine([Event("warmup complete"), Event("exiting with last active thread context", 7)])
        _, metadata, code = simulate_window(full, full, 100, 200)
        self.assertEqual("failed_guest_exit", metadata["status"])
        self.assertTrue(metadata["measurement_started"])
        self.assertEqual(7, metadata["guest_exit_code"])
        self.assertNotEqual(0, code)
        self.assertEqual([(100, "warmup complete"), "simulate", "reset", (200, "max instruction count reached"), "simulate"], full.calls)


@unittest.skipUnless(os.environ.get("GEM5_EXECUTABLE"), "Set GEM5_EXECUTABLE for real gem5 checks")
class RealWindowIntegrationTest(RunnerFixture):
    # Only this integration method is collected; inherited fake-runner checks stay
    # in the base suite and do not need an external simulator.
    def test_real_same_path_workload_replacement_cannot_reuse_success(self):
        binary = self.root / "workload"
        shutil.copy2("/bin/true", binary)
        candidate = self.candidate("replacement", os.environ["GEM5_EXECUTABLE"])
        candidate["gem5"].update(config_script=str(SCRIPTS.parent / "gem5_configs/o3_fadse_se.py"),
                                 benchmark_binary=str(binary), warmup_insts=0, maxinsts=1000000)
        self.finish(self.start(candidate))
        original = self.result(candidate)["fingerprint"]
        self.finish(self.start(candidate))
        self.assertEqual(1, json.loads(self.ledger.read_text())["used"])
        code = self.root / "exit7.c"
        code.write_text("int main(void) { return 7; }\n")
        subprocess.run(["gcc", "-static", str(code), "-o", str(binary)], check=True)
        self.finish(self.start(candidate), 2)
        result = self.result(candidate)
        self.assertEqual("failed_guest_exit", result["status"])
        self.assertNotEqual(original, result["fingerprint"])
        self.assertFalse(result["cache_hit"])
        self.assertEqual(2, json.loads(self.ledger.read_text())["used"])

    def test_real_guest_and_warmup_failures(self):
        code = self.root / "exit7.c"; code.write_text("int main(void) { return 7; }\n")
        binary = self.root / "exit7"
        subprocess.run(["gcc", "-static", str(code), "-o", str(binary)], check=True)
        cases = [(str(binary), 0, "failed_guest_exit"), ("/bin/true", 1000000, "incomplete_window"),
                 ("/bin/true", 1000, "ok"), ("/bin/true", 0, "ok")]
        for index, (program, warmup, status) in enumerate(cases):
            with self.subTest(status=status, warmup=warmup):
                candidate = self.candidate(str(index), os.environ["GEM5_EXECUTABLE"])
                candidate["gem5"].update(config_script=str(SCRIPTS.parent / "gem5_configs/o3_fadse_se.py"),
                                         benchmark_binary=program, warmup_insts=warmup, maxinsts=1000000)
                self.finish(self.start(candidate), 0 if status == "ok" else 2)
                result = self.result(candidate)
                self.assertEqual(status, result["status"], result)
                self.assertEqual(status, result["measurement"]["status"])

    def test_real_memory_geometry_matches_configured_range(self):
        for memory_size, ranks in (("16GB", 4), ("64GB", 16)):
            with self.subTest(memory_size=memory_size):
                candidate = self.candidate("memory-" + str(ranks), os.environ["GEM5_EXECUTABLE"])
                candidate["gem5"].update(
                    config_script=str(SCRIPTS.parent / "gem5_configs/o3_fadse_se.py"),
                    benchmark_binary="/bin/true",
                    warmup_insts=0,
                    maxinsts=1000,
                    mem_size=memory_size,
                )
                self.finish(self.start(candidate))
                output = Path(candidate["gem5"]["output_dir"])
                self.assertIn(
                    f"ranks_per_channel={ranks}",
                    (output / "config.ini").read_text(),
                )
                self.assertNotIn("does not match", self.result(candidate)["stderr_tail"])
