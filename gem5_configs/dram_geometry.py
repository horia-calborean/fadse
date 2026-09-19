"""Capacity helpers for the fixed DDR3_1600_8x8 device organization."""

import re

from generated_gem5_contract import DRAM_RANK_BYTES

DDR3_DEVICE_SIZE = "512MiB"
DDR3_DEVICE_SIZE_BYTES = 512 * 1024 ** 2
DDR3_DEVICES_PER_RANK = 8

MEMORY_UNITS = {
    "B": 1,
    "KB": 1024,
    "KIB": 1024,
    "MB": 1024 ** 2,
    "MIB": 1024 ** 2,
    "GB": 1024 ** 3,
    "GIB": 1024 ** 3,
    "TB": 1024 ** 4,
    "TIB": 1024 ** 4,
}


class MemorySizeFormatError(ValueError):
    """The requested memory size cannot be parsed."""


class DramGeometryError(ValueError):
    """The requested capacity cannot be represented by the DRAM geometry."""


def parse_capacity_bytes(value):
    """Parse an integer memory size with an explicit binary unit."""
    match = re.fullmatch(r"\s*(\d+)\s*([A-Za-z]+)\s*", str(value))
    if not match or match.group(2).upper() not in MEMORY_UNITS:
        raise MemorySizeFormatError(f"invalid memory size: {value}")
    return int(match.group(1)) * MEMORY_UNITS[match.group(2).upper()]


def ranks_for_capacity(capacity_bytes, rank_capacity=DRAM_RANK_BYTES):
    """Return the exact number of ranks needed for the requested capacity."""
    if capacity_bytes <= 0 or rank_capacity <= 0:
        raise DramGeometryError("DRAM capacity and rank geometry must be positive")
    ranks, remainder = divmod(capacity_bytes, rank_capacity)
    if remainder:
        raise DramGeometryError(
            f"memory size {capacity_bytes} is not a multiple of the "
            f"{rank_capacity}-byte DDR3 rank capacity"
        )
    if ranks & (ranks - 1):
        raise DramGeometryError(
            f"DRAM rank count {ranks} is not a power of two; choose a power-of-two "
            f"multiple of the {rank_capacity}-byte rank capacity"
        )
    return ranks
