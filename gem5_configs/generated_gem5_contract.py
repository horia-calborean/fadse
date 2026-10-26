"""Generated from configs/gem5/architecture-contract.json. Do not edit."""

CONTRACT_VERSION = 2

CACHE_FORMAT = 'fadse-gem5-v7'

DRAM_RANK_BYTES = 4294967296

SIMULATION_DEFAULTS = {'method': 'nsga2',
 'seed': '0',
 'budget_index': '0',
 'timeout_seconds': '3600',
 'warmup_insts': '10000000',
 'maxinsts': '50000000',
 'cacheline_size': '64',
 'mem_size': '16GB'}

SUPPORTED_OBJECTIVES = ['cpi',
 'l2_mpki_pos',
 'cost_proxy',
 'ipc',
 'sim_seconds',
 'sim_ticks',
 'host_seconds',
 'cycles',
 'committed_instructions',
 'sim_insts',
 'simTicks',
 'simSeconds',
 'hostSeconds',
 'simInsts']

FIXED_PARAMETERS = {'num_cores': {'default': 1, 'values': [1], 'invalid_reason': 'num_cores_must_equal_1'}}

REQUIRED_PARAMETERS = {'width': {'values': [2, 4, 6, 8], 'cost_weight': 0.4},
 'rob_entries': {'values': [64, 128, 192, 256], 'cost_weight': 0.01},
 'iq_entries': {'values': [32, 64, 96, 128], 'cost_weight': 0.01},
 'lsq_entries': {'values': [16, 32, 48, 64, 96], 'cost_weight': 0.01},
 'l1i_kb': {'values': [16, 32, 64], 'cost_weight': 0.02},
 'l1d_kb': {'values': [16, 32, 64], 'cost_weight': 0.02},
 'l2_kb': {'values': [256, 512, 1024, 2048], 'cost_weight': 0.005},
 'l2_assoc': {'values': [4, 8, 16], 'cost_weight': 0.05}}

OPTIONAL_PARAMETERS = {'l3_kb': {'values': [0, 2048, 4096, 8192, 16384], 'cost_weight': 0.001},
 'l3_assoc': {'values': [8, 16, 32], 'cost_weight': 0.02},
 'bp_global_size': {'values': [1024, 2048, 4096, 8192, 16384], 'cost_weight': 5e-05},
 'btb_entries': {'values': [512, 1024, 2048, 4096, 8192], 'cost_weight': 0.0001},
 'ras_size': {'values': [8, 16, 32, 64], 'cost_weight': 0.005},
 'prefetcher': {'values': [0, 1, 2], 'cost_weight': 0.1},
 'prefetch_degree': {'default': 0, 'values': [0, 1, 2, 4], 'cost_weight': 0.025}}

COST_WEIGHTS = {'width': 0.4,
 'rob_entries': 0.01,
 'iq_entries': 0.01,
 'lsq_entries': 0.01,
 'l1i_kb': 0.02,
 'l1d_kb': 0.02,
 'l2_kb': 0.005,
 'l2_assoc': 0.05,
 'l3_kb': 0.001,
 'l3_assoc': 0.02,
 'bp_global_size': 5e-05,
 'btb_entries': 0.0001,
 'ras_size': 0.005,
 'prefetcher': 0.1,
 'prefetch_degree': 0.025}

REPAIRS = [{'kind': 'cap_to_allowed',
  'target': 'iq_entries',
  'bound': 'rob_entries',
  'reason': 'iq_entries_lte_rob_entries'},
 {'kind': 'cap_to_allowed',
  'target': 'lsq_entries',
  'bound': 'rob_entries',
  'reason': 'lsq_entries_lte_rob_entries'},
 {'kind': 'sum_floor_to_allowed',
  'target': 'l2_kb',
  'sources': ['l1i_kb', 'l1d_kb'],
  'reason': 'l2_kb_gte_l1_sum'},
 {'kind': 'floor_to_allowed',
  'target': 'l3_kb',
  'bound': 'l2_kb',
  'skip_values': [0],
  'reason': 'l3_kb_gte_l2_kb'},
 {'kind': 'set_when',
  'target': 'prefetch_degree',
  'when_parameter': 'prefetcher',
  'when_value': 0,
  'skip_values': [0],
  'value': 0,
  'reason': 'prefetch_degree_zero_without_prefetcher'},
 {'kind': 'set_when',
  'target': 'prefetch_degree',
  'when_parameter': 'prefetcher',
  'when_not_value': 0,
  'repair_values': [0],
  'value': 1,
  'reason': 'prefetch_degree_one_with_enabled_prefetcher'}]

FINGERPRINT_FIELDS = ['cache_format',
 'method',
 'benchmark',
 'seed',
 'budget_index',
 'gem5_binary',
 'config_script',
 'timeout_seconds',
 'warmup_insts',
 'maxinsts',
 'cacheline_size',
 'architecture',
 'benchmark_binary',
 'benchmark_args',
 'run_cwd',
 'stdin',
 'mem_size',
 'isolate_cwd',
 'environment']

INTEGER_FINGERPRINT_FIELDS = ['seed', 'budget_index', 'timeout_seconds', 'warmup_insts', 'maxinsts', 'cacheline_size']
