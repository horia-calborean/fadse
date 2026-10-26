// Generated from configs/gem5/architecture-contract.json. Do not edit.
package core.network.client.gem5;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GeneratedGem5Contract {
    static final int CONTRACT_VERSION = 2;
    static final String CACHE_FORMAT = "fadse-gem5-v7";
    static final long DRAM_RANK_BYTES = 4294967296L;
    static final Map<String, String> SIMULATION_DEFAULTS = Map.ofEntries(
            Map.entry("method", "nsga2"),
            Map.entry("seed", "0"),
            Map.entry("budget_index", "0"),
            Map.entry("timeout_seconds", "3600"),
            Map.entry("warmup_insts", "10000000"),
            Map.entry("maxinsts", "50000000"),
            Map.entry("cacheline_size", "64"),
            Map.entry("mem_size", "16GB")
    );
    static final List<String> SUPPORTED_OBJECTIVES = List.of("cpi", "l2_mpki_pos", "cost_proxy", "ipc", "sim_seconds", "sim_ticks", "host_seconds", "cycles", "committed_instructions", "sim_insts", "simTicks", "simSeconds", "hostSeconds", "simInsts");
    static final Map<String, Parameter> FIXED_PARAMETERS = fixedParameters();
    static final Map<String, Parameter> REQUIRED_PARAMETERS = requiredParameters();
    static final Map<String, Parameter> OPTIONAL_PARAMETERS = optionalParameters();
    static final Map<String, Double> COST_WEIGHTS = costWeights();
    static final List<Repair> REPAIRS = List.of(
            new Repair("cap_to_allowed", "iq_entries", "rob_entries", List.of(), new int[0], new int[0], null, null, null, 0, "iq_entries_lte_rob_entries"),
            new Repair("cap_to_allowed", "lsq_entries", "rob_entries", List.of(), new int[0], new int[0], null, null, null, 0, "lsq_entries_lte_rob_entries"),
            new Repair("sum_floor_to_allowed", "l2_kb", null, List.of("l1i_kb", "l1d_kb"), new int[0], new int[0], null, null, null, 0, "l2_kb_gte_l1_sum"),
            new Repair("floor_to_allowed", "l3_kb", "l2_kb", List.of(), new int[] { 0 }, new int[0], null, null, null, 0, "l3_kb_gte_l2_kb"),
            new Repair("set_when", "prefetch_degree", null, List.of(), new int[] { 0 }, new int[0], "prefetcher", 0, null, 0, "prefetch_degree_zero_without_prefetcher"),
            new Repair("set_when", "prefetch_degree", null, List.of(), new int[0], new int[] { 0 }, "prefetcher", null, 0, 1, "prefetch_degree_one_with_enabled_prefetcher")
    );
    static final List<String> FINGERPRINT_FIELDS = List.of("cache_format", "method", "benchmark", "seed", "budget_index", "gem5_binary", "config_script", "timeout_seconds", "warmup_insts", "maxinsts", "cacheline_size", "architecture", "benchmark_binary", "benchmark_args", "run_cwd", "stdin", "mem_size", "isolate_cwd", "environment");
    static final List<String> INTEGER_FINGERPRINT_FIELDS = List.of("seed", "budget_index", "timeout_seconds", "warmup_insts", "maxinsts", "cacheline_size");

    record Parameter(int[] values, Integer defaultValue, String invalidReason, double costWeight) { }

    record Repair(String kind, String target, String bound, List<String> sources,
            int[] skipValues, int[] repairValues, String whenParameter, Integer whenValue,
            Integer whenNotValue, int value, String reason) { }

    private static Map<String, Parameter> fixedParameters() {
        Map<String, Parameter> values = new LinkedHashMap<>();
        values.put("num_cores", new Parameter(new int[] { 1 }, 1, "num_cores_must_equal_1", 0.0));
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Parameter> requiredParameters() {
        Map<String, Parameter> values = new LinkedHashMap<>();
        values.put("width", new Parameter(new int[] { 2, 4, 6, 8 }, null, null, 0.4));
        values.put("rob_entries", new Parameter(new int[] { 64, 128, 192, 256 }, null, null, 0.01));
        values.put("iq_entries", new Parameter(new int[] { 32, 64, 96, 128 }, null, null, 0.01));
        values.put("lsq_entries", new Parameter(new int[] { 16, 32, 48, 64, 96 }, null, null, 0.01));
        values.put("l1i_kb", new Parameter(new int[] { 16, 32, 64 }, null, null, 0.02));
        values.put("l1d_kb", new Parameter(new int[] { 16, 32, 64 }, null, null, 0.02));
        values.put("l2_kb", new Parameter(new int[] { 256, 512, 1024, 2048 }, null, null, 0.005));
        values.put("l2_assoc", new Parameter(new int[] { 4, 8, 16 }, null, null, 0.05));
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Parameter> optionalParameters() {
        Map<String, Parameter> values = new LinkedHashMap<>();
        values.put("l3_kb", new Parameter(new int[] { 0, 2048, 4096, 8192, 16384 }, null, null, 0.001));
        values.put("l3_assoc", new Parameter(new int[] { 8, 16, 32 }, null, null, 0.02));
        values.put("bp_global_size", new Parameter(new int[] { 1024, 2048, 4096, 8192, 16384 }, null, null, 5e-05));
        values.put("btb_entries", new Parameter(new int[] { 512, 1024, 2048, 4096, 8192 }, null, null, 0.0001));
        values.put("ras_size", new Parameter(new int[] { 8, 16, 32, 64 }, null, null, 0.005));
        values.put("prefetcher", new Parameter(new int[] { 0, 1, 2 }, null, null, 0.1));
        values.put("prefetch_degree", new Parameter(new int[] { 0, 1, 2, 4 }, 0, null, 0.025));
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Double> costWeights() {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("width", 0.4);
        values.put("rob_entries", 0.01);
        values.put("iq_entries", 0.01);
        values.put("lsq_entries", 0.01);
        values.put("l1i_kb", 0.02);
        values.put("l1d_kb", 0.02);
        values.put("l2_kb", 0.005);
        values.put("l2_assoc", 0.05);
        values.put("l3_kb", 0.001);
        values.put("l3_assoc", 0.02);
        values.put("bp_global_size", 5e-05);
        values.put("btb_entries", 0.0001);
        values.put("ras_size", 0.005);
        values.put("prefetcher", 0.1);
        values.put("prefetch_degree", 0.025);
        return Collections.unmodifiableMap(values);
    }

    private GeneratedGem5Contract() { }
}
