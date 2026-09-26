from pathlib import Path
import copy
import sys
import unittest


SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))

from candidate_hash import candidate_id
from gem5_contract import cost_proxy, validate_and_repair_architecture
from generate_gem5_contract import generated_outputs, validate_contract
from generated_gem5_contract import OPTIONAL_PARAMETERS, REPAIRS, SUPPORTED_OBJECTIVES
from run_gem5_candidate import validate_and_repair_candidate, validate_memory_size


BASE_ARCHITECTURE = {
    "width": 4,
    "rob_entries": 128,
    "iq_entries": 64,
    "lsq_entries": 32,
    "l1i_kb": 32,
    "l1d_kb": 32,
    "l2_kb": 512,
    "l2_assoc": 8,
}


class Gem5ContractTest(unittest.TestCase):
    def test_generated_adapters_are_current(self):
        for path, expected in generated_outputs().items():
            self.assertEqual(expected, path.read_text(encoding="utf-8"), str(path))

    def test_contract_drives_repairs_and_cost(self):
        architecture = dict(BASE_ARCHITECTURE, rob_entries=64, iq_entries=128,
                            lsq_entries=96, prefetcher=0, prefetch_degree=4)
        repaired, original, repairs, invalid = validate_and_repair_architecture(architecture)
        self.assertEqual(architecture | {"num_cores": 1}, original)
        self.assertEqual(64, repaired["iq_entries"])
        self.assertEqual(64, repaired["lsq_entries"])
        self.assertEqual(0, repaired["prefetch_degree"])
        self.assertEqual([
            "iq_entries_lte_rob_entries",
            "lsq_entries_lte_rob_entries",
            "prefetch_degree_zero_without_prefetcher",
        ], repairs)
        self.assertEqual([], invalid)
        self.assertAlmostEqual(8.08, cost_proxy(BASE_ARCHITECTURE))

    def test_rejects_an_unknown_contract_version(self):
        _, _, _, invalid = validate_and_repair_candidate({
            "contract_version": 999,
            "architecture": BASE_ARCHITECTURE,
        })
        self.assertIn("unsupported_contract_version", invalid)

    def test_memory_geometry_is_rejected_before_launch(self):
        from dram_geometry import parse_capacity_bytes, ranks_for_capacity

        self.assertIsNone(validate_memory_size("16GB"))
        self.assertEqual(4, ranks_for_capacity(parse_capacity_bytes("16GB")))
        self.assertEqual("mem_size_invalid_format", validate_memory_size("garbage"))
        for value in ("0GB", "6GB", "12GB", "24GB"):
            with self.subTest(value=value):
                self.assertEqual(
                    "mem_size_incompatible_with_dram_geometry",
                    validate_memory_size(value),
                )

    def test_enabled_prefetchers_normalize_zero_or_missing_degree(self):
        enabled_rules = [
            rule for rule in REPAIRS
            if rule["kind"] == "set_when" and rule.get("when_not_value") == 0
        ]
        self.assertEqual(1, len(enabled_rules))
        self.assertTrue(all(rule.get("repair_values") == [0] for rule in enabled_rules))
        self.assertTrue(all("skip_values" not in rule for rule in enabled_rules))
        for prefetcher in OPTIONAL_PARAMETERS["prefetcher"]["values"]:
            if prefetcher == 0:
                continue
            with self.subTest(prefetcher=prefetcher, degree="zero"):
                architecture = dict(BASE_ARCHITECTURE, prefetcher=prefetcher,
                                    prefetch_degree=0)
                repaired, _, repairs, invalid = validate_and_repair_architecture(architecture)
                self.assertEqual(1, repaired["prefetch_degree"])
                self.assertEqual(["prefetch_degree_one_with_enabled_prefetcher"], repairs)
                self.assertEqual([], invalid)
                self.assertEqual(
                    cost_proxy(dict(BASE_ARCHITECTURE, prefetcher=prefetcher,
                                    prefetch_degree=1)),
                    cost_proxy(repaired),
                )

            with self.subTest(prefetcher=prefetcher, degree="missing"):
                architecture = dict(BASE_ARCHITECTURE, prefetcher=prefetcher)
                repaired, _, repairs, invalid = validate_and_repair_architecture(architecture)
                self.assertEqual(1, repaired["prefetch_degree"])
                self.assertEqual(["prefetch_degree_one_with_enabled_prefetcher"], repairs)
                self.assertEqual([], invalid)

    def test_contract_rejects_an_explicit_empty_repair_filter(self):
        contract = __import__("json").loads(
            (SCRIPTS.parent / "configs/gem5/architecture-contract.json").read_text()
        )
        contract = copy.deepcopy(contract)
        contract["repairs"][0]["repair_values"] = []
        with self.assertRaisesRegex(ValueError, "empty repair_values"):
            validate_contract(contract)

    def test_zero_capable_l2_metrics_are_not_advertised_as_objectives(self):
        self.assertNotIn("l2_misses", SUPPORTED_OBJECTIVES)
        self.assertNotIn("l2_mpki", SUPPORTED_OBJECTIVES)
        self.assertIn("l2_mpki_pos", SUPPORTED_OBJECTIVES)

    def test_fingerprint_has_a_cross_language_golden_vector(self):
        candidate = {
            "method": "nsga2",
            "benchmark": "bfs",
            "seed": 0,
            "budget_index": 1,
            "architecture": dict(BASE_ARCHITECTURE, num_cores=1),
            "gem5": {
                "gem5_binary": "/missing/gem5",
                "config_script": "/missing/config.py",
                "benchmark_binary": "/missing/bfs",
                "timeout_seconds": 3600,
                "warmup_insts": 10000000,
                "maxinsts": 50000000,
                "cacheline_size": 64,
                "mem_size": "16GB",
            },
        }
        self.assertEqual(
            "nsga2_bfs_seed0_budget1_aa0eb0f970ed930f7e66d96f",
            candidate_id(candidate),
        )


if __name__ == "__main__":
    unittest.main()
