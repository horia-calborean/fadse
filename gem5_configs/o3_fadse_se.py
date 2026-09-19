"""gem5 syscall-emulation O3 config for FADSE candidates."""

import argparse
import json
import os
import shlex
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from cache_latency_table import latency_cycles
from dram_geometry import (
    DDR3_DEVICE_SIZE,
    DDR3_DEVICES_PER_RANK,
    parse_capacity_bytes,
    ranks_for_capacity,
)
from simulation_window import simulate_window
from pathlib import Path

import m5
from m5.objects import (
    AddrRange,
    Cache,
    DDR3_1600_8x8,
    DerivO3CPU,
    IQUnit,
    L2XBar,
    MemCtrl,
    Process,
    ReturnAddrStack,
    Root,
    SEWorkload,
    SimpleBTB,
    SrcClockDomain,
    StridePrefetcher,
    System,
    SystemXBar,
    TaggedPrefetcher,
    TournamentBP,
    VoltageDomain,
)

# Prefetcher profiles are categorical. Integer codes keep the FADSE genome
# integer-typed.
PREFETCHER_PROFILES = {0: "none", 1: "stride", 2: "tagged"}


class L1ICache(Cache):
    assoc = 2
    mshrs = 4
    tgts_per_mshr = 20


class L1DCache(Cache):
    assoc = 2
    mshrs = 4
    tgts_per_mshr = 20


class L2Cache(Cache):
    mshrs = 20
    tgts_per_mshr = 12


class L3Cache(Cache):
    # Fixed MSHR count, deliberately high enough not to saturate first
    # and recorded via config.ini.
    mshrs = 32
    tgts_per_mshr = 12


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--binary")
    parser.add_argument("--benchmark-name", default="")
    parser.add_argument("--benchmark-args", default="")
    parser.add_argument("--cmd")
    parser.add_argument("--options", default="")
    parser.add_argument("--cwd", default="",
                        help="working directory for the simulated process "
                             "(SPEC run dirs contain the reference inputs)")
    parser.add_argument("--stdin", default="",
                        help="file connected to the simulated process stdin")
    parser.add_argument("--env-json", default="",
                        help="JSON object of environment variables for the "
                             "simulated process")
    parser.add_argument("--num-cores", type=int, default=1)
    parser.add_argument("--width", type=int, default=4)
    parser.add_argument("--rob-entries", type=int, default=128)
    parser.add_argument("--iq-entries", type=int, default=64)
    parser.add_argument("--lsq-entries", type=int, default=64)
    parser.add_argument("--l1i-kb", type=int, default=32)
    parser.add_argument("--l1d-kb", type=int, default=32)
    parser.add_argument("--l2-kb", type=int, default=512)
    parser.add_argument("--l2-assoc", type=int, default=8)
    parser.add_argument("--l3-kb", type=int, default=0,
                        help="L3 size in kB; 0 disables the L3 level "
                             "(legacy two-level hierarchy)")
    parser.add_argument("--l3-assoc", type=int, default=16)
    parser.add_argument("--bp-global-size", type=int, default=8192,
                        help="TournamentBP global/choice predictor entries")
    parser.add_argument("--btb-entries", type=int, default=4096)
    parser.add_argument("--ras-size", type=int, default=16)
    parser.add_argument("--prefetcher", type=int, default=0,
                        help="L1D prefetcher profile: 0=none 1=stride 2=tagged")
    parser.add_argument("--prefetch-degree", type=int, default=0,
                        help="prefetch degree; ignored when --prefetcher=0")
    parser.add_argument("--cacheline-size", type=int, default=64)
    parser.add_argument("--mem-size", default="16GB")
    parser.add_argument("--warmup-insts", type=int, default=0)
    parser.add_argument("--maxinsts", type=int, default=10000000)
    args = parser.parse_args()
    if not args.binary:
        args.binary = args.cmd
    if not args.benchmark_args:
        args.benchmark_args = args.options
    if not args.binary:
        parser.error("--binary is required")
    if args.prefetcher not in PREFETCHER_PROFILES:
        parser.error(f"--prefetcher must be one of {sorted(PREFETCHER_PROFILES)}")
    return args


def assert_power_of_two_sets(name, size_kb, assoc, line_bytes):
    sets = (size_kb * 1024) // (assoc * line_bytes)
    if sets <= 0 or (sets & (sets - 1)) != 0:
        raise ValueError(
            f"{name}: size {size_kb}kB / assoc {assoc} gives non-power-of-two "
            f"set count {sets}; BaseSetAssoc would assert"
        )


def apply_latencies(cache, level, size_kb, assoc):
    tag, data, response = latency_cycles(level, size_kb, assoc)
    cache.tag_latency = tag
    cache.data_latency = data
    cache.response_latency = response


def make_branch_predictor(args):
    predictor = TournamentBP()
    predictor.globalPredictorSize = args.bp_global_size
    predictor.choicePredictorSize = args.bp_global_size
    btb = SimpleBTB()
    btb.numEntries = args.btb_entries
    ras = ReturnAddrStack()
    ras.numEntries = args.ras_size
    from m5.objects import BranchPredictor
    bp_unit = BranchPredictor()
    bp_unit.conditionalBranchPred = predictor
    bp_unit.btb = btb
    bp_unit.ras = ras
    return bp_unit


def make_l1d_prefetcher(args):
    profile = PREFETCHER_PROFILES[args.prefetcher]
    if profile == "none":
        return None
    if args.prefetch_degree <= 0:
        raise ValueError("enabled prefetchers require --prefetch-degree >= 1")
    degree = args.prefetch_degree
    if profile == "stride":
        return StridePrefetcher(degree=degree)
    return TaggedPrefetcher(degree=degree)


def make_cpu(args):
    cpu = DerivO3CPU()
    cpu.fetchWidth = args.width
    cpu.decodeWidth = args.width
    cpu.renameWidth = args.width
    cpu.dispatchWidth = args.width
    cpu.issueWidth = args.width
    cpu.wbWidth = args.width
    cpu.commitWidth = args.width
    cpu.numROBEntries = args.rob_entries
    cpu.instQueues = [IQUnit(numEntries=args.iq_entries)]
    cpu.LQEntries = args.lsq_entries
    cpu.SQEntries = args.lsq_entries
    cpu.branchPred = make_branch_predictor(args)
    return cpu


def main():
    args = parse_args()
    if args.num_cores != 1:
        raise ValueError("This FADSE gem5 config fixes num_cores=1")

    line = args.cacheline_size
    assert_power_of_two_sets("l1i", args.l1i_kb, 2, line)
    assert_power_of_two_sets("l1d", args.l1d_kb, 2, line)
    assert_power_of_two_sets("l2", args.l2_kb, args.l2_assoc, line)
    if args.l3_kb > 0:
        assert_power_of_two_sets("l3", args.l3_kb, args.l3_assoc, line)

    system = System()
    system.clk_domain = SrcClockDomain(clock="2GHz", voltage_domain=VoltageDomain())
    system.mem_mode = "timing"
    system.mmap_using_noreserve = True
    memory_capacity = parse_capacity_bytes(args.mem_size)
    system.mem_ranges = [AddrRange(memory_capacity)]
    system.cache_line_size = args.cacheline_size

    system.cpu = make_cpu(args)
    system.membus = SystemXBar()
    system.l2bus = L2XBar()

    system.cpu.icache = L1ICache(size=f"{args.l1i_kb}kB")
    apply_latencies(system.cpu.icache, "l1i", args.l1i_kb, 2)
    system.cpu.dcache = L1DCache(size=f"{args.l1d_kb}kB")
    apply_latencies(system.cpu.dcache, "l1d", args.l1d_kb, 2)
    prefetcher = make_l1d_prefetcher(args)
    if prefetcher is not None:
        system.cpu.dcache.prefetcher = prefetcher
    system.l2 = L2Cache(size=f"{args.l2_kb}kB", assoc=args.l2_assoc)
    apply_latencies(system.l2, "l2", args.l2_kb, args.l2_assoc)
    system.mem_ctrl = MemCtrl()
    system.mem_ctrl.dram = DDR3_1600_8x8(
        range=system.mem_ranges[0],
        device_size=DDR3_DEVICE_SIZE,
        devices_per_rank=DDR3_DEVICES_PER_RANK,
        ranks_per_channel=ranks_for_capacity(memory_capacity),
    )

    system.cpu.icache.cpu_side = system.cpu.icache_port
    system.cpu.dcache.cpu_side = system.cpu.dcache_port
    system.cpu.icache.mem_side = system.l2bus.cpu_side_ports
    system.cpu.dcache.mem_side = system.l2bus.cpu_side_ports
    system.cpu.createInterruptController()
    system.cpu.interrupts[0].pio = system.membus.mem_side_ports
    system.cpu.interrupts[0].int_requestor = system.membus.cpu_side_ports
    system.cpu.interrupts[0].int_responder = system.membus.mem_side_ports
    system.l2.cpu_side = system.l2bus.mem_side_ports

    if args.l3_kb > 0:
        system.l3bus = L2XBar()
        system.l3 = L3Cache(size=f"{args.l3_kb}kB", assoc=args.l3_assoc)
        apply_latencies(system.l3, "l3", args.l3_kb, args.l3_assoc)
        system.l2.mem_side = system.l3bus.cpu_side_ports
        system.l3.cpu_side = system.l3bus.mem_side_ports
        system.l3.mem_side = system.membus.cpu_side_ports
    else:
        system.l2.mem_side = system.membus.cpu_side_ports

    system.mem_ctrl.port = system.membus.mem_side_ports

    system.system_port = system.membus.cpu_side_ports

    system.workload = SEWorkload.init_compatible(args.binary)
    process = Process()
    process.cmd = [args.binary] + shlex.split(args.benchmark_args)
    if args.cwd:
        process.cwd = args.cwd
    if args.stdin:
        process.input = args.stdin
    if args.env_json:
        environment = json.loads(args.env_json)
        if not isinstance(environment, dict):
            raise ValueError("--env-json must contain a JSON object")
        process.env = [f"{key}={value}" for key, value in environment.items()]
    system.cpu.workload = process
    system.cpu.createThreads()

    root = Root(full_system=False, system=system)
    m5.instantiate()

    # max_insts_any_thread is consumed at instantiation time; after
    # m5.instantiate() only scheduleInstStop() can bound the simulation.
    exit_event, measurement, exit_code = simulate_window(m5, system.cpu, args.warmup_insts, args.maxinsts)
    measurement["exit_cause"] = exit_event.getCause()
    Path(m5.options.outdir, "measurement.json").write_text(json.dumps(measurement) + "\n")
    print(f"Exiting @ tick {m5.curTick()} because {exit_event.getCause()}")
    if exit_code:
        sys.exit(exit_code)


if __name__ == "__m5_main__":
    main()
