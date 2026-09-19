"""Size/associativity-dependent cache latencies for FADSE gem5 configs.

gem5's classic
cache takes tag/data/response latencies as fixed constants. If they stay
fixed while capacity and associativity sweep, the optimizer discovers that
capacity is free and the resulting Pareto front is an artifact.

This table supplies analytical estimates that are deterministic and monotone
non-decreasing in size and associativity. The timings are not physically
calibrated.
This module is the single source of truth for the placeholder latencies.

Latency model (cycles at the fixed 2 GHz baseline clock):

  base[level] + size_steps * per_size_step[level] + assoc_steps * per_assoc_step[level]

where size_steps counts powers of two above the smallest level size and
assoc_steps counts powers of two above the smallest allowed associativity.
"""

import math

# level -> (min_size_kb, min_assoc, base_cycles, per_size_step, per_assoc_step)
_MODEL = {
    "l1i": (16, 2, 2, 1, 0),
    "l1d": (16, 2, 2, 1, 0),
    "l2": (256, 4, 12, 2, 1),
    "l3": (2048, 8, 24, 3, 1),
}


def _steps(value, minimum):
    if value < minimum:
        raise ValueError(f"value {value} below table minimum {minimum}")
    ratio = value / minimum
    steps = int(round(math.log2(ratio)))
    if 2 ** steps * minimum != value:
        raise ValueError(f"value {value} is not a power-of-two multiple of {minimum}")
    return steps


def latency_cycles(level, size_kb, assoc):
    """Return (tag_latency, data_latency, response_latency) in cycles."""
    if level not in _MODEL:
        raise ValueError(f"unknown cache level: {level}")
    min_size, min_assoc, base, per_size, per_assoc = _MODEL[level]
    total = (
        base
        + _steps(size_kb, min_size) * per_size
        + _steps(assoc, min_assoc) * per_assoc
    )
    tag = max(1, total // 2)
    data = total
    # Response latency models the return path; it is not a copy of the hit
    # latency.
    response = max(1, total // 2)
    return tag, data, response


def table_rows(levels_and_sizes):
    """Materialize rows [(level, size_kb, assoc, tag, data, response)]."""
    rows = []
    for level, size_kb, assoc in levels_and_sizes:
        tag, data, response = latency_cycles(level, size_kb, assoc)
        rows.append((level, size_kb, assoc, tag, data, response))
    return rows
