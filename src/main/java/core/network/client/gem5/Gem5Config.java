package core.network.client.gem5;

import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Gem5Config {
    private static final Pattern MEMORY_SIZE = Pattern.compile("\\s*(\\d+)\\s*([A-Za-z]+)\\s*");
    private static final BigInteger DRAM_RANK_BYTES = BigInteger.valueOf(
            GeneratedGem5Contract.DRAM_RANK_BYTES);
    private static final Map<String, BigInteger> MEMORY_UNITS = Map.ofEntries(
            Map.entry("B", BigInteger.ONE),
            Map.entry("KB", BigInteger.valueOf(1024)),
            Map.entry("KIB", BigInteger.valueOf(1024)),
            Map.entry("MB", BigInteger.valueOf(1024L * 1024)),
            Map.entry("MIB", BigInteger.valueOf(1024L * 1024)),
            Map.entry("GB", BigInteger.valueOf(1024L * 1024 * 1024)),
            Map.entry("GIB", BigInteger.valueOf(1024L * 1024 * 1024)),
            Map.entry("TB", BigInteger.valueOf(1024L).pow(4)),
            Map.entry("TIB", BigInteger.valueOf(1024L).pow(4))
    );
    static final String GEM5_EXECUTABLE = "gem5_executable";
    static final String GEM5_CONFIG_SCRIPT = "gem5_config_script";
    static final String GEM5_OUTPUT_DIR = "gem5_output_dir";
    static final String GEM5_STATS_FILE = "gem5_stats_file";
    static final String GEM5_RUNNER_SCRIPT = "gem5_runner_script";
    static final String GEM5_PYTHON = "gem5_python";
    static final String GEM5_TIMEOUT_SECONDS = "gem5_timeout_seconds";
    static final String GEM5_WARMUP_INSTS = "gem5_warmup_insts";
    static final String GEM5_MAX_INSTS = "gem5_maxinsts";
    static final String GEM5_METHOD = "gem5_method";
    static final String GEM5_SEED = "gem5_seed";
    static final String GEM5_BUDGET_INDEX = "gem5_budget_index";
    static final String GEM5_LAUNCH_BUDGET = "gem5_launch_budget";
    static final String GEM5_RUN_ID = "gem5_run_id";
    static final String GEM5_BUDGET_FILE = "gem5_budget_file";
    static final String GEM5_EVALUATED_CSV = "gem5_evaluated_csv";
    static final String GEM5_REPAIRS_LOG = "gem5_repairs_log";
    static final String GEM5_RESULT_PROPERTIES = "gem5_result_properties";
    static final String GEM5_RESULT_JSON = "gem5_result_json";
    static final String GEM5_BENCHMARK_NAME = "gem5_benchmark_name";
    static final String GEM5_BENCHMARK_BINARY = "gem5_benchmark_binary";
    static final String GEM5_BENCHMARK_ARGS = "gem5_benchmark_args";
    static final String GEM5_RUN_CWD = "gem5_run_cwd";
    static final String GEM5_STDIN = "gem5_stdin";
    static final String GEM5_WORKLOAD_MANIFEST = "gem5_workload_manifest";
    static final String GEM5_CACHELINE_SIZE = "gem5_cacheline_size";
    static final String GEM5_MEMORY_SIZE = "gem5_memory_size";
    static final String GEM5_ARGUMENT_DIALECT = "gem5_argument_dialect";
    static final String ARGUMENT_DIALECT_FADSE_O3 = "fadse-o3";

    static final String DEFAULT_STATS_FILE = "stats.txt";
    static final String DEFAULT_PYTHON = "python3";
    static final String DEFAULT_TIMEOUT_SECONDS = defaultValue("timeout_seconds");
    static final String DEFAULT_WARMUP_INSTS = defaultValue("warmup_insts");
    static final String DEFAULT_MAX_INSTS = defaultValue("maxinsts");
    static final String DEFAULT_CACHELINE_SIZE = defaultValue("cacheline_size");
    static final String DEFAULT_MEMORY_SIZE = defaultValue("mem_size");
    static final String DEFAULT_METHOD = defaultValue("method");
    static final String DEFAULT_SEED = defaultValue("seed");
    static final String DEFAULT_BUDGET_INDEX = defaultValue("budget_index");
    static final String DEFAULT_REPAIRS_LOG = "results/datasets/candidate_repairs.csv";
    static final String DEFAULT_RESULT_PROPERTIES = "fadse_result.properties";
    static final String DEFAULT_RESULT_JSON = "fadse_result.json";

    private static String defaultValue(String name) {
        String value = GeneratedGem5Contract.SIMULATION_DEFAULTS.get(name);
        if (value == null) {
            throw new IllegalStateException("Missing generated gem5 default: " + name);
        }
        return value;
    }

    static String require(Map<String, String> problemConfig, String key) {
        String value = problemConfig.get(key);
        if (!hasValue(value)) {
            throw new IllegalArgumentException("Missing required gem5 configuration parameter: " + key);
        }
        return value.trim();
    }

    static boolean isFadseO3(Map<String, String> problemConfig) {
        return ARGUMENT_DIALECT_FADSE_O3.equals(valueOrDefault(
                problemConfig.get(GEM5_ARGUMENT_DIALECT), ""));
    }

    static String validateMemorySize(String value) {
        String normalized = valueOrDefault(value, DEFAULT_MEMORY_SIZE);
        Matcher matcher = MEMORY_SIZE.matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid gem5 memory size: " + normalized);
        }
        BigInteger unit = MEMORY_UNITS.get(matcher.group(2).toUpperCase());
        if (unit == null) {
            throw new IllegalArgumentException("Invalid gem5 memory size: " + normalized);
        }
        BigInteger capacity = new BigInteger(matcher.group(1)).multiply(unit);
        BigInteger[] rankCount = capacity.divideAndRemainder(DRAM_RANK_BYTES);
        if (rankCount[0].signum() <= 0 || rankCount[1].signum() != 0
                || rankCount[0].and(rankCount[0].subtract(BigInteger.ONE)).signum() != 0) {
            throw new IllegalArgumentException(
                    "gem5 memory size must be a power-of-two multiple of "
                            + DRAM_RANK_BYTES + " bytes: " + normalized);
        }
        return normalized;
    }

    static String valueOrDefault(String value, String defaultValue) {
        return hasValue(value) ? value.trim() : defaultValue;
    }

    static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    static Object parseIntegerOrString(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private Gem5Config() {
    }
}
