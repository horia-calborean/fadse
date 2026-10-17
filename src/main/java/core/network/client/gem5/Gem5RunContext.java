package core.network.client.gem5;

import com.fasterxml.jackson.databind.ObjectMapper;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Initializes an optional physical-launch quota shared by all workers in a run. */
public final class Gem5RunContext {
    private static final ObjectMapper JSON = new ObjectMapper();

    private Gem5RunContext() {
    }

    @SuppressWarnings("unchecked")
    public static void initialize(InputData input) {
        if (!"gem5".equalsIgnoreCase(String.valueOf(input.get(CommonSetupParameters.NAME)))) {
            return;
        }
        Map<String, String> original = (Map<String, String>) input.get(CommonSetupParameters.PROBLEM_CONFIG);
        Map<String, String> config = new LinkedHashMap<>(original == null ? Map.of() : original);
        input.set(CommonSetupParameters.PROBLEM_CONFIG, config);
        config = new LinkedHashMap<>(Gem5Simulator.validateConfiguration(input));
        String manifest = config.get(Gem5Config.GEM5_WORKLOAD_MANIFEST);
        if (Gem5Config.hasValue(config.get(Gem5Config.GEM5_RUNNER_SCRIPT))
                && Gem5Config.hasValue(manifest)) {
            Gem5WorkloadManifest.validate(manifest);
        }
        String runId = UUID.randomUUID().toString();
        config.put(Gem5Config.GEM5_RUN_ID, runId);
        input.set(CommonSetupParameters.PROBLEM_CONFIG, config);
        String budget = config.get(Gem5Config.GEM5_LAUNCH_BUDGET);
        if (budget == null || budget.isBlank()) {
            return;
        }
        int limit = Integer.parseInt(budget);
        if (limit <= 0) {
            throw new IllegalArgumentException("gem5_launch_budget must be positive");
        }
        if (config.getOrDefault(Gem5Config.GEM5_RUNNER_SCRIPT, "").isBlank()) {
            throw new IllegalArgumentException("gem5_launch_budget requires gem5_runner_script");
        }
        Path ledger = Path.of(config.getOrDefault("gem5_run_root", "results/runs"),
                runId, "launches.json").toAbsolutePath().normalize();
        try {
            Files.createDirectories(ledger.getParent());
            JSON.writeValue(ledger.toFile(), Map.of(
                    "run_id", runId, "limit", limit, "used", 0, "launches", List.of()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize gem5 launch ledger: " + ledger, e);
        }
        config.put(Gem5Config.GEM5_BUDGET_FILE, ledger.toString());
        config.putIfAbsent(Gem5Config.GEM5_EVALUATED_CSV,
                ledger.resolveSibling("evaluated_configurations.csv").toString());
        input.set(CommonSetupParameters.PROBLEM_CONFIG, config);
    }
}
