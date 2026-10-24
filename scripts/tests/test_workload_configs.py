import argparse
import json
from pathlib import Path
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "gem5_configs"))
sys.path.insert(0, str(ROOT / "scripts"))
import setup_spec_workloads
from cache_latency_table import latency_cycles
from run_gem5_candidate import DISCRETE_VALUES, OPTIONAL_DISCRETE_VALUES
from smoke_test_gem5 import require_positive


class WorkloadConfigsTest(unittest.TestCase):
    def test_spec_static_overlay_is_portable_and_single_threaded(self):
        config = (ROOT / 'configs/spec/fadse-gem5-static.cfg').read_text()
        self.assertIn('include: Example-gcc-linux-x86.cfg', config)
        self.assertIn('copies                  = 1', config)
        self.assertIn('threads                 = 1', config)
        self.assertNotIn('preENV_OMP_NUM_THREADS', config)
        self.assertIn('-march=x86-64', config)
        self.assertIn('-static', config)
        self.assertNotIn('-march=native', config)
        self.assertNotIn('/home/', config)

    def test_generated_spec_configs_use_supported_algorithms_and_launch_budget(self):
        with tempfile.TemporaryDirectory() as folder:
            args = argparse.Namespace(config_dir=Path(folder), gem5_bin=Path('/gem5.opt'),
                timeout_seconds=120, warmup_insts=100, maxinsts=200, memory_size='16GB',
                launch_budget=3, extended_architecture=True)
            setup_spec_workloads.write_design_spaces(args, Path('/manifest.json'),
                [dict(id='invoke&1', benchmark='821.gcc_s', memory_size='64GB'),
                 dict(id='invoke2', benchmark='821.gcc_s', memory_size='64GB')])
            files = list(Path(folder).glob('*.xml'))
            self.assertEqual(1, len(files))
            tree = ET.parse(files[0]).getroot()
            self.assertEqual('NSGAII', tree.find('metaheuristic').get('name'))
            config = {p.get('name'):p.get('value') for p in tree.find('problem_config')}
            self.assertEqual('3', config['gem5_launch_budget'])
            self.assertEqual('64GB', config['gem5_memory_size'])
            self.assertEqual(['invoke&1','invoke2'], [p.get('name') for p in tree.find('benchmarks')])
            self.assertEqual(15, len(tree.find('parameters')))

    def test_example_algorithms_and_architecture_values_are_supported(self):
        for path in (ROOT / 'configs/designSpace').glob('gem5*.xml'):
            tree = ET.parse(path).getroot()
            problem = tree.find('problem_config')
            if problem is None or problem.get('name') != 'gem5':
                continue
            algorithm = tree.find('metaheuristic')
            with self.subTest(path=path):
                self.assertEqual('NSGAII', algorithm.get('name'))
                self.assertTrue((ROOT / 'configs/metaheuristicConfig' / algorithm.get('config_path')).is_file())
                config = {p.get('name'):p.get('value') for p in problem}
                self.assertNotIn('/home/', config['gem5_executable'])
                for param in tree.find('parameters'):
                    name = param.get('name')
                    if name not in DISCRETE_VALUES and name not in OPTIONAL_DISCRETE_VALUES:
                        continue
                    low, high = int(param.get('min')), int(param.get('max'))
                    if param.get('type') == 'exp2':
                        values, value = [], low
                        while value <= high:
                            values.append(value)
                            value *= 2
                    else:
                        values = range(low, high + 1, int(param.get('step', 1)))
                    allowed = DISCRETE_VALUES.get(name, OPTIONAL_DISCRETE_VALUES.get(name))
                    self.assertTrue(set(values).issubset(allowed), (name, list(values), allowed))

    def test_gapbs_manifest_has_distinct_complete_workload_entries(self):
        entries = json.loads((ROOT / 'workloads/gapbs.example.json').read_text())['entries']
        self.assertEqual({'bfs', 'pr', 'cc'}, {row['id'] for row in entries})
        self.assertTrue(all(row['args'] == '-g 15 -n 4' for row in entries))
        self.assertEqual(3, len({row['binary'] for row in entries}))

    def test_cache_set_counts_and_latency_scaling_cover_extended_space(self):
        for level, sizes, assocs in (
            ('l1i', DISCRETE_VALUES['l1i_kb'], [2]),
            ('l1d', DISCRETE_VALUES['l1d_kb'], [2]),
            ('l2', DISCRETE_VALUES['l2_kb'], DISCRETE_VALUES['l2_assoc']),
            ('l3', [s for s in OPTIONAL_DISCRETE_VALUES['l3_kb'] if s], OPTIONAL_DISCRETE_VALUES['l3_assoc']),
        ):
            for assoc in assocs:
                previous = (0, 0, 0)
                for size in sizes:
                    sets = size * 1024 // (assoc * 64)
                    self.assertGreater(sets, 0)
                    self.assertEqual(0, sets & (sets - 1))
                    latency = latency_cycles(level, size, assoc)
                    self.assertTrue(all(b >= a for a, b in zip(previous, latency)))
                    previous = latency

    def test_dram_rank_count_matches_requested_capacity(self):
        from dram_geometry import ranks_for_capacity

        gib = 1024 ** 3
        self.assertEqual(2, ranks_for_capacity(8 * gib))
        self.assertEqual(4, ranks_for_capacity(16 * gib))
        self.assertEqual(16, ranks_for_capacity(64 * gib))
        for invalid_gib in (10, 12, 20, 24):
            with self.subTest(invalid_gib=invalid_gib), self.assertRaises(ValueError):
                ranks_for_capacity(invalid_gib * gib)

    def test_smoke_validation_rejects_nan_infinity_and_failure_sentinels(self):
        for value in ('nan', 'inf', '-1', '0', str(sys.float_info.max)):
            with self.subTest(value=value), self.assertRaises(SystemExit):
                require_positive({'cpi':value}, 'cpi')


if __name__ == '__main__':
    unittest.main()
