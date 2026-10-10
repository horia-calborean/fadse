package core.network.client.gem5;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.gap.GAPXmlDataExtractor;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** Opt-in test of XML objectives through the real Python/gem5/Java connector. */
public class Gem5ConfiguredObjectivesIntegrationTest {
    @Test public void readsEveryConfiguredObjectiveFromRealGem5() throws Exception {
        String configPath = System.getProperty("gem5.designSpace");
        assumeTrue("Set -Dgem5.designSpace=/path/to/local-o3-config.xml", configPath != null);
        GAPXmlDataExtractor extractor = new GAPXmlDataExtractor(new XmlInputDocument(configPath));
        Map<String, String> config = extractor.extractGapConfigParameters();
        Map<String, Objective> objectives = extractor.extractObjectives();
        assertFalse("XML must configure objectives", objectives.isEmpty());
        assertTrue("This test requires the O3 Python runner", config.containsKey("gem5_runner_script"));
        assertTrue("gem5 executable must exist", Files.isExecutable(Path.of(config.get("gem5_executable"))));
        var benchmarks = extractor.extractBenchmarksList();
        assertEquals("Use a single workload for this readback test", 1, benchmarks.size());
        String benchmark = benchmarks.getFirst();
        String binary = config.containsKey("gem5_workload_manifest")
                ? Gem5WorkloadManifest.resolve(config.get("gem5_workload_manifest"), benchmark).getBinary()
                : benchmark;
        assertTrue("benchmark binary must exist", Files.isExecutable(Path.of(binary)));
        ProblemParameter<?>[] parameters = extractor.extractParameters();
        for (ProblemParameter<?> parameter : parameters) {
            parameter.setValueFromDouble(((Number) parameter.getLowerBound()).doubleValue());
        }
        InputData input = new InputData();
        input.set(CommonSetupParameters.NAME, extractor.extractName());
        input.set(CommonSetupParameters.PROBLEM_CONFIG, config);
        input.set(CommonSetupParameters.OBJECTIVES, objectives);
        input.set(CommonSetupParameters.DESIGN_VARIABLES, parameters);
        input.set(CommonSetupParameters.BENCHMARKS, benchmarks);
        Gem5Candidate candidate = Gem5CandidateFactory.create(config, parameters, benchmark);
        assertFalse("Use a fresh output directory so this test launches gem5", Files.exists(candidate.getResultJson()));
        Gem5Simulator simulator = new Gem5Simulator(input);
        FadseIndividual individual = new FadseIndividual(input, benchmark);
        simulator.performSimulation(individual);

        ObjectMapper json = new ObjectMapper();
        var result = json.readTree(candidate.getResultJson().toFile());
        assertEquals("ok", result.path("status").asText());
        assertEquals("Java and Python must agree on content identity", candidate.getCandidateId(),
                result.path("fingerprint").asText());
        assertFalse(result.path("cache_hit").asBoolean());
        assertTrue(result.path("measurement").path("measurement_started").asBoolean());
        Path stats = candidate.getOutputDirectory().resolve("stats.txt");
        assertTrue("gem5 must produce nonempty stats", Files.size(stats) > 0);
        Map<String, Double> actual = new LinkedHashMap<>();
        for (Objective objective : individual.getObjectives()) {
            double value = objective.getValue();
            assertTrue(objective.getName() + " must be finite and non-sentinel", Double.isFinite(value)
                    && value > 0 && value != Double.MAX_VALUE && value != Double.MIN_VALUE);
            assertEquals(result.path("objectives").path(objective.getName()).asDouble(), value, 1e-12);
            actual.put(objective.getName(), value);
        }
        assertEquals(objectives.keySet(), actual.keySet());

        // Independently check the final raw dump, including gem5's :: vector names.
        Map<String, Double> raw = new LinkedHashMap<>();
        for (String line : Files.readAllLines(stats)) {
            if (line.startsWith("---------- Begin Simulation Statistics")) raw.clear();
            String[] fields = line.trim().split("\\s+");
            if (fields.length >= 2) {
                try { raw.put(fields[0], Double.parseDouble(fields[1])); }
                catch (NumberFormatException ignored) { }
            }
        }
        var mapping = result.path("counter_mapping");
        double instructions = raw.get(mapping.path("committed_instructions").asText());
        double cycles = raw.get(mapping.path("cycles").asText());
        String missesCounter = mapping.path("l2_misses").asText();
        double misses = "zero_suppressed_l2_misses".equals(missesCounter) ? 0 : raw.get(missesCounter);
        Map<String, Double> expected = Map.of("cpi", cycles / instructions,
                "l2_mpki_pos", 1000 * misses / instructions + 1e-9,
                "cost_proxy", Gem5CandidateValidator.costProxy(candidate.getArchitecture()));
        for (var objective : actual.entrySet()) {
            assertTrue("Unexpected objective for this O3 test: " + objective.getKey(), expected.containsKey(objective.getKey()));
            assertEquals(expected.get(objective.getKey()), objective.getValue(), 1e-12);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "passed");
        report.put("design_space", Path.of(configPath).toAbsolutePath().toString());
        report.put("benchmark", benchmark);
        report.put("architecture", candidate.getArchitecture());
        report.put("objectives_read_by_java", actual);
        report.put("objectives_recomputed", expected);
        report.put("measured_instructions", instructions);
        report.put("measured_cycles", cycles);
        report.put("l2_misses", misses);
        report.put("stats_path", stats.toAbsolutePath().toString());
        report.put("result_properties", candidate.getResultProperties().toAbsolutePath().toString());
        json.writerWithDefaultPrettyPrinter().writeValue(candidate.getOutputDirectory().resolve("objective_readback.json").toFile(), report);
        System.out.println("CONFIGURED_OBJECTIVES_READBACK " + json.writeValueAsString(report));
    }
}
