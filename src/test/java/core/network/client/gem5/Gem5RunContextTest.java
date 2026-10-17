package core.network.client.gem5;

import com.fasterxml.jackson.databind.ObjectMapper;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import core.model.objectives.Objective;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Gem5RunContextTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private InputData input(Map<String, String> config) {
        InputData input = new InputData();
        input.set(CommonSetupParameters.NAME, "gem5");
        input.set(CommonSetupParameters.PROBLEM_CONFIG, config);
        input.set(CommonSetupParameters.OBJECTIVES, Map.of(
                "simTicks", new Objective("simTicks", "float", true)));
        return input;
    }

    private Map<String, String> configWith(Map<String, String> values) {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("gem5_executable", "/unused/gem5");
        config.put("gem5_config_script", "/unused/config.py");
        config.put("gem5_output_dir", temporary.getRoot().toString());
        config.putAll(values);
        return config;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> config(InputData input) {
        return (Map<String, String>) input.get(CommonSetupParameters.PROBLEM_CONFIG);
    }

    @Test
    public void createsSeparateLedgersAndPassesQuotaThroughCandidateRequests() throws Exception {
        Map<String, String> settings = configWith(Map.of("gem5_launch_budget", "2",
                "gem5_runner_script", "scripts/run_gem5_candidate.py",
                "gem5_run_root", temporary.getRoot().toString()));
        InputData first = input(settings);
        InputData second = input(settings);
        Gem5RunContext.initialize(first);
        Gem5RunContext.initialize(second);
        Map<String, String> configured = config(first);
        assertNotEquals(configured.get("gem5_run_id"), config(second).get("gem5_run_id"));
        ObjectMapper json = new ObjectMapper();
        var ledger = json.readTree(Path.of(configured.get("gem5_budget_file")).toFile());
        assertEquals(2, ledger.path("limit").asInt());
        assertEquals(0, ledger.path("used").asInt());
        assertEquals(configured.get("gem5_run_id"), ledger.path("run_id").asText());
        assertTrue(ledger.path("launches").isEmpty());
        assertEquals(Path.of(configured.get("gem5_budget_file")).getParent(),
                Path.of(configured.get("gem5_evaluated_csv")).getParent());
        Path request = temporary.getRoot().toPath().resolve("request.json");
        Gem5CandidateFactory.create(configured, Map.of("width", 4), "bfs")
                .writeJson(request, java.util.List.of());
        var candidate = json.readTree(request.toFile());
        assertEquals(configured.get("gem5_run_id"), candidate.path("run_id").asText());
        assertEquals(configured.get("gem5_budget_file"), candidate.path("gem5").path("budget_file").asText());
    }

    @Test
    public void optionalQuotaLeavesUncappedConnectorUsable() {
        InputData input = input(configWith(Map.of()));
        Gem5RunContext.initialize(input);
        assertFalse(config(input).containsKey("gem5_budget_file"));
        assertTrue(config(input).containsKey("gem5_run_id"));
        assertEquals(0, temporary.getRoot().list().length);
    }

    @Test
    public void refusesInvalidQuotasAndDirectConnectorBeforeCreatingFiles() {
        for (String limit : new String[]{"0", "-1", "invalid"}) {
            assertThrows(IllegalArgumentException.class, () -> Gem5RunContext.initialize(input(
                    configWith(Map.of("gem5_launch_budget", limit, "gem5_runner_script", "runner.py")))));
        }
        assertThrows(IllegalArgumentException.class, () -> Gem5RunContext.initialize(input(
                configWith(Map.of("gem5_launch_budget", "2")))));
        assertEquals(0, temporary.getRoot().list().length);
    }

    @Test
    public void rejectsUnsupportedPythonObjectivesBeforeRunInitialization() {
        InputData input = input(configWith(Map.of("gem5_runner_script", "runner.py")));
        input.set(CommonSetupParameters.OBJECTIVES, Map.of(
                "typo", new Objective("l2_mpki_posx", "float", true)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Gem5RunContext.initialize(input));

        assertTrue(error.getMessage().contains("l2_mpki_posx"));
        assertFalse(config(input).containsKey("gem5_run_id"));
    }

    @Test
    public void rejectsInvalidPythonMemoryBeforeRunInitialization() {
        InputData input = input(configWith(Map.of(
                "gem5_runner_script", "runner.py",
                "gem5_memory_size", "12GB")));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Gem5RunContext.initialize(input));
        assertTrue(error.getMessage().contains("power-of-two"));
        assertFalse(config(input).containsKey("gem5_run_id"));
    }

    @Test
    public void legacyDirectConnectorDoesNotParseUnusedWorkloadManifest() {
        InputData input = input(configWith(Map.of(
                "gem5_memory_size", "512MB",
                "gem5_workload_manifest", temporary.getRoot().toPath()
                        .resolve("missing-manifest.json").toString())));

        Gem5RunContext.initialize(input);

        assertTrue(config(input).containsKey("gem5_run_id"));
    }
}
