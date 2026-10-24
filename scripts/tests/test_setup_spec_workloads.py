import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))


def load_module(name, relative_path):
    path = Path(__file__).resolve().parents[2] / relative_path
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


setup_spec = load_module("setup_spec_workloads", "scripts/setup_spec_workloads.py")
gem5_runner = load_module("run_gem5_candidate", "scripts/run_gem5_candidate.py")


class SetupSpecWorkloadsTest(unittest.TestCase):
    def test_parses_all_invocations_stdin_and_one_thread_environment(self):
        with tempfile.TemporaryDirectory() as temporary:
            bench_dir = Path(temporary) / "821.gcc_s"
            exe_dir = bench_dir / "exe"
            run_dir = bench_dir / "run" / "run_base_refspeed_test.0000"
            exe_dir.mkdir(parents=True)
            run_dir.mkdir(parents=True)
            command_dir = run_dir / "commands"
            command_dir.mkdir()
            (exe_dir / "cc1_s").write_bytes(b"\x7fELF")
            (command_dir / "input.txt").write_text("input")
            (run_dir / "speccmds.cmd").write_text(
                "-E OMP_NUM_THREADS 1\n"
                "-E OMP_STACKSIZE 120M\n"
                "-C commands\n"
                "-i input.txt -o one.out -e one.err ../run/cc1_s first.c -O2 > one.out 2>> one.err\n"
                "../run/cc1_s second.c -O3 > two.out 2>> two.err\n"
            )

            with patch.object(setup_spec, "verify_static"):
                entries = setup_spec.parse_invocations(bench_dir)

            self.assertEqual(2, len(entries))
            self.assertEqual(["first.c", "-O2"], entries[0]["argv"])
            self.assertTrue(entries[0]["stdin"].endswith("input.txt"))
            self.assertEqual("", entries[1]["stdin"])
            self.assertEqual("1", entries[0]["environment"]["OMP_NUM_THREADS"])
            self.assertEqual("120M", entries[0]["environment"]["OMP_STACKSIZE"])
            self.assertEqual("16GB", entries[0]["memory_size"])
            self.assertEqual(command_dir.resolve(), Path(entries[0]["cwd"]))
            self.assertEqual(command_dir.resolve(), Path(entries[1]["cwd"]))

    def test_candidate_command_forwards_spec_process_metadata(self):
        with tempfile.TemporaryDirectory() as temporary:
            candidate = {
                "architecture": {},
                "benchmark": "821.gcc_s",
                "gem5": {
                    "gem5_binary": "/gem5.opt",
                    "config_script": "/o3.py",
                    "output_dir": temporary,
                    "benchmark_binary": "/spec/cc1_s",
                    "benchmark_args": "input.c -O2",
                    "run_cwd": "/spec/run",
                    "stdin": "/spec/run/input.txt",
                    "environment": {"OMP_NUM_THREADS": "1"},
                    "warmup_insts": 10_000_000,
                    "maxinsts": 50_000_000,
                    "mem_size": "16GB",
                },
            }

            command = gem5_runner.command_from_candidate(candidate)

            self.assertIn("--cwd=/spec/run", command)
            self.assertIn("--stdin=/spec/run/input.txt", command)
            self.assertIn('--env-json={"OMP_NUM_THREADS": "1"}', command)
            self.assertIn("--warmup-insts=10000000", command)
            self.assertIn("--maxinsts=50000000", command)
            self.assertIn("--mem-size=16GB", command)

    def test_isolated_workdir_copies_inputs_and_cannot_write_through(self):
        with tempfile.TemporaryDirectory() as temporary:
            source = Path(temporary) / "source"
            destination = Path(temporary) / "destination"
            (source / "inputs").mkdir(parents=True)
            original = source / "inputs" / "data.txt"
            original.write_text("reference input")
            linked_original = source / "shared.txt"
            linked_original.symlink_to(original)

            isolated = gem5_runner.prepare_isolated_cwd(source, destination)

            mirrored = isolated / "inputs" / "data.txt"
            mirrored_link = isolated / "shared.txt"
            self.assertFalse(mirrored.is_symlink())
            self.assertFalse(mirrored_link.is_symlink())
            self.assertEqual("reference input", mirrored.read_text())
            mirrored.write_text("candidate output")
            mirrored_link.write_text("candidate output through former symlink")
            self.assertEqual("reference input", original.read_text())

            (destination / "stale-output.txt").write_text("failed attempt")
            retried = gem5_runner.prepare_isolated_cwd(source, destination)
            self.assertFalse((retried / "stale-output.txt").exists())
            self.assertEqual("reference input", (retried / "inputs" / "data.txt").read_text())
            self.assertEqual("reference input", original.read_text())

            stale_destination = Path(temporary) / "stale-symlink"
            stale_destination.symlink_to(source, target_is_directory=True)
            replaced = gem5_runner.prepare_isolated_cwd(source, stale_destination)
            self.assertFalse(replaced.is_symlink())
            self.assertEqual("reference input", original.read_text())


if __name__ == "__main__":
    unittest.main()
