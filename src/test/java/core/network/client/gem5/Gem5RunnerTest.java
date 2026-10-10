package core.network.client.gem5;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.adapters.parameter.numerics.type.DoubleParameter;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class Gem5RunnerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void buildsGem5CommandFromProblemConfigBenchmarkAndDesignVariables() {
        File outputRoot = temporaryFolder.getRoot();
        InputData inputData = createInputData(outputRoot.getAbsolutePath() + "/gem5-#");
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        TestableGem5Runner runner = new TestableGem5Runner(simulator);
        FadseIndividual individual = new FadseIndividual(inputData, "/benchmarks/dijkstra");
        ProblemParameter<?>[] parameters = createParameters();

        runner.setIndividual(individual);
        runner.setParameters(parameters);

        String[] command = runner.commandLine();

        assertEquals("/opt/gem5/build/X86/gem5.opt", command[0]);
        assertTrue(command[1].startsWith("--outdir=" + outputRoot.getAbsolutePath() + "/gem5-"));
        assertFalse(command[1].contains("#"));
        assertEquals("--stats-file=stats.txt", command[2]);
        assertEquals("/opt/gem5/configs/example/se.py", command[3]);
        assertTrue(Arrays.asList(command).contains("--cmd=/benchmarks/dijkstra"));
        assertTrue(Arrays.asList(command).contains("--cpu-clock=3"));
        assertTrue(Arrays.asList(command).contains("--num-cpus=2"));
        assertTrue(simulator.getSimulatorOutputFile().endsWith(File.separator + "stats.txt"));
    }

    @Test
    public void buildsPythonRunnerCommandAndWritesSpecCandidateJson() throws Exception {
        File outputRoot = temporaryFolder.getRoot();
        InputData inputData = createInputData(outputRoot.getAbsolutePath() + "/gem5-#");
        Map<String, String> problemConfig = getProblemConfig(inputData);
        problemConfig.put(Gem5Config.GEM5_RUNNER_SCRIPT, "/repo/scripts/run_gem5_candidate.py");
        problemConfig.put(Gem5Config.GEM5_REPAIRS_LOG, outputRoot.getAbsolutePath() + "/candidate_repairs.csv");
        problemConfig.put(Gem5Config.GEM5_METHOD, "custom_method");
        problemConfig.put(Gem5Config.GEM5_SEED, "42");

        Gem5Simulator simulator = new Gem5Simulator(inputData);
        TestableGem5Runner runner = new TestableGem5Runner(simulator);
        FadseIndividual individual = new FadseIndividual(inputData, "dijkstra");

        runner.setIndividual(individual);
        runner.setParameters(createSpecParameters());

        String[] command = runner.commandLine();

        assertEquals(Gem5Config.DEFAULT_PYTHON, command[0]);
        assertEquals("/repo/scripts/run_gem5_candidate.py", command[1]);
        assertEquals("--candidate-json", command[2]);
        assertEquals("--result-json", command[4]);
        assertTrue(command[5].endsWith(File.separator + Gem5Config.DEFAULT_RESULT_JSON));
        assertEquals("--response-properties", command[6]);
        assertEquals(command[7], simulator.getSimulatorOutputFile());
        assertTrue(Paths.get(command[7]).getFileName().toString().startsWith("request-"));
        assertFalse(Files.exists(Paths.get(command[7])));

        Path candidateJson = Paths.get(command[3]);
        assertTrue(Files.exists(candidateJson));
        String json = Files.readString(candidateJson);
        assertTrue(json.contains("\"candidate_id\""));
        assertTrue(json.contains("\"method\" : \"custom_method\""));
        assertTrue(json.contains("\"benchmark\" : \"dijkstra\""));
        assertTrue(json.contains("\"seed\" : 42"));
        assertTrue(json.contains("\"width\" : 4"));
        assertTrue(json.contains("\"benchmark_binary\" : \"dijkstra\""));
        assertTrue(json.contains("\"cost_proxy\""));
        assertTrue(json.contains("\"repairs\""));
        assertTrue(json.contains("\"objectives\""));
        assertTrue(json.contains("\"simTicks\""));
        assertTrue(json.contains("\"original_architecture\" : {\n    \"width\""));
        assertTrue(json.contains("\"num_cores\" : 1"));

        assertFalse(Files.exists(Paths.get(problemConfig.get(Gem5Config.GEM5_REPAIRS_LOG))));
        int repairsArgument = Arrays.asList(command).indexOf("--repairs-csv");
        assertTrue(repairsArgument > 0);
        assertEquals(problemConfig.get(Gem5Config.GEM5_REPAIRS_LOG), command[repairsArgument + 1]);
    }

    @Test
    public void rejectsMissingRequiredGem5Executable() {
        InputData inputData = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        getProblemConfig(inputData).remove(Gem5Config.GEM5_EXECUTABLE);

        try {
            new Gem5Simulator(inputData);
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains(Gem5Config.GEM5_EXECUTABLE));
            return;
        }

        throw new AssertionError("Expected Gem5Simulator to reject missing gem5_executable");
    }

    @Test
    public void acceptsOwnedFailuresButRejectsMissingMalformedAndNonWorstResponses() throws Exception {
        TestableGem5Runner runner = pythonRunner();
        String[] command = runner.commandLine();
        Path response = Paths.get(command[7]);
        for (int exitCode : new int[] {2, 124}) {
            Files.writeString(response, "status = timeout\nsimTicks = " + Double.MAX_VALUE + "\n");
            runner.validateProcessExit(exitCode);
            assertThrows(IllegalStateException.class, () -> runner.validateProcessExit(0));
        }
        for (String invalid : new String[] {"", "status=timeout\n", "status=timeout\nsimTicks=NaN\n",
                "status=timeout\nsimTicks=12\n", "status=ok\nsimTicks=12\n"}) {
            Files.writeString(response, invalid);
            assertThrows(IllegalStateException.class, () -> runner.validateProcessExit(2));
        }
    }

    @Test
    public void treatsPublishedWorkerLaunchFailuresAsRetryable() throws Exception {
        TestableGem5Runner runner = pythonRunner();
        String[] command = runner.commandLine();
        Path response = Paths.get(command[7]);
        Files.writeString(response,
                "status = failed_launch\nsimTicks = " + Double.MAX_VALUE + "\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class, () -> runner.validateProcessExit(2));
        assertTrue(error.getMessage().contains("failed_launch"));
    }

    @Test
    public void rejectsSharedResultsAfterLockTimeoutAndUsesDistinctResponseForEveryRequest() throws Exception {
        TestableGem5Runner runner = pythonRunner();
        String[] first = runner.commandLine();
        Path firstResponse = Paths.get(first[7]);
        Files.writeString(firstResponse, "status=ok\nsimTicks=12\n");
        runner.validateProcessExit(0);
        Files.writeString(firstResponse.resolveSibling(Gem5Config.DEFAULT_RESULT_PROPERTIES),
                "status=ok\nsimTicks=12\n");

        String[] second = runner.commandLine();
        assertFalse(first[7].equals(second[7]));
        assertThrows(IllegalStateException.class, () -> runner.validateProcessExit(124));
        assertThrows(IllegalStateException.class, () -> runner.validateProcessExit(0));
    }

    @Test
    public void directConnectorStillRejectsNonzeroExitWithLeftoverStats() throws Exception {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        Gem5Simulator simulator = new Gem5Simulator(input);
        TestableGem5Runner runner = new TestableGem5Runner(simulator);
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        runner.commandLine();
        Files.writeString(Paths.get(simulator.getSimulatorOutputFile()), "simTicks 12\n");
        assertThrows(IllegalStateException.class, () -> runner.validateProcessExit(2));
    }

    @Test
    public void fadseO3DialectRepairsMissingPrefetchDegreeOnDirectConnector() throws Exception {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        Map<String, String> config = getProblemConfig(input);
        config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
        config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, Gem5Config.ARGUMENT_DIALECT_FADSE_O3);
        Path repairs = temporaryFolder.getRoot().toPath().resolve("direct-repairs.csv");
        config.put(Gem5Config.GEM5_REPAIRS_LOG, repairs.toString());
        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        ProblemParameter<?>[] parameters = Arrays.copyOf(createSpecParameters(), 9);
        parameters[8] = integerParameter("prefetcher", 0, 2, 1);
        runner.setParameters(parameters);

        List<String> command = Arrays.asList(runner.commandLine());

        assertTrue(command.contains("--prefetch-degree=1"));
        assertTrue(command.contains("--cacheline-size=" + Gem5Config.DEFAULT_CACHELINE_SIZE));
        assertTrue(command.contains("--mem-size=" + Gem5Config.DEFAULT_MEMORY_SIZE));
        assertFalse(command.contains("--cacheline_size=" + Gem5Config.DEFAULT_CACHELINE_SIZE));
        assertTrue(Files.exists(repairs));
        assertTrue(Files.readString(repairs).contains(
                "prefetch_degree_one_with_enabled_prefetcher"));
        assertTrue(Files.readAllLines(repairs).get(1).contains(",direct_"));
    }

    @Test
    public void bundledO3DirectConnectorDefaultsToItsCompatibleDialect() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        Map<String, String> config = getProblemConfig(input);
        config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "gem5_configs/o3_fadse_se.py");
        config.put(Gem5Config.GEM5_REPAIRS_LOG,
                temporaryFolder.getRoot().toPath().resolve("repairs.csv").toString());

        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        runner.setParameters(createSpecParameters());
        List<String> command = Arrays.asList(runner.commandLine());

        assertEquals(Gem5Config.ARGUMENT_DIALECT_FADSE_O3,
                config(input).get(Gem5Config.GEM5_ARGUMENT_DIALECT));
        assertTrue(command.contains("--cacheline-size=" + Gem5Config.DEFAULT_CACHELINE_SIZE));
        assertTrue(command.contains("--warmup-insts=" + Gem5Config.DEFAULT_WARMUP_INSTS));
        assertTrue(command.contains("--mem-size=" + Gem5Config.DEFAULT_MEMORY_SIZE));
    }

    @Test
    public void normalizesWhitespaceInFadseO3DialectAndForwardsMemorySize() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        Map<String, String> config = getProblemConfig(input);
        config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
        config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, "  fadse-o3  ");
        config.put(Gem5Config.GEM5_MEMORY_SIZE, "32GB");

        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        runner.setParameters(createSpecParameters());
        List<String> command = Arrays.asList(runner.commandLine());

        assertEquals(Gem5Config.ARGUMENT_DIALECT_FADSE_O3,
                config(input).get(Gem5Config.GEM5_ARGUMENT_DIALECT));
        assertTrue(command.contains("--cacheline-size=" + Gem5Config.DEFAULT_CACHELINE_SIZE));
        assertFalse(command.contains("--cacheline_size=" + Gem5Config.DEFAULT_CACHELINE_SIZE));
        assertTrue(command.contains("--mem-size=32GB"));
    }

    @Test
    public void rejectsMemoryThatCannotMatchDirectFadseO3DramGeometry() {
        for (String memorySize : List.of("12GB", "garbage")) {
            InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
            Map<String, String> config = getProblemConfig(input);
            config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
            config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, Gem5Config.ARGUMENT_DIALECT_FADSE_O3);
            config.put(Gem5Config.GEM5_MEMORY_SIZE, memorySize);

            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> new Gem5Simulator(input));
            assertTrue(error.getMessage().contains(memorySize));
        }
    }

    @Test
    public void directRepairRowsUseDistinctDeterministicCandidateIds() throws Exception {
        Path repairs = temporaryFolder.getRoot().toPath().resolve("direct-repairs.csv");
        for (String benchmark : List.of("/bin/true", "/bin/false")) {
            InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
            Map<String, String> config = getProblemConfig(input);
            config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
            config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, Gem5Config.ARGUMENT_DIALECT_FADSE_O3);
            config.put(Gem5Config.GEM5_REPAIRS_LOG, repairs.toString());
            TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
            runner.setIndividual(new FadseIndividual(input, benchmark));
            ProblemParameter<?>[] parameters = Arrays.copyOf(createSpecParameters(), 9);
            parameters[8] = integerParameter("prefetcher", 0, 2, 1);
            runner.setParameters(parameters);
            runner.commandLine();
        }

        List<String> lines = Files.readAllLines(repairs);
        assertEquals(5, lines.size());
        String firstId = lines.get(1).split(",", 4)[2];
        String secondId = lines.get(3).split(",", 4)[2];
        assertTrue(firstId.startsWith("direct_"));
        assertFalse(firstId.equals(secondId));
    }

    @Test
    public void legacyDirectConnectorPreservesFractionalDesignVariables() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        DoubleParameter clock = new DoubleParameter(1.5, 3.5);
        clock.setName("clock_ghz");
        clock.setValue(2.5);
        runner.setParameters(new ProblemParameter<?>[] {clock});

        assertTrue(Arrays.asList(runner.commandLine()).contains("--clock-ghz=2.5"));
    }

    @Test
    public void legacyDirectConnectorIgnoresFadseO3MemoryGeometry() {
        for (String memorySize : List.of("512MB", "2GB")) {
            InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
            getProblemConfig(input).put(Gem5Config.GEM5_MEMORY_SIZE, memorySize);
            TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
            runner.setIndividual(new FadseIndividual(input, "/bin/true"));
            runner.setParameters(createParameters());

            assertFalse(Arrays.asList(runner.commandLine()).contains("--mem-size=" + memorySize));
        }
    }

    @Test
    public void fadseO3DirectConnectorRejectsFractionalAndNonFiniteVariables() {
        for (double value : new double[] {2.5, Double.NaN, Double.POSITIVE_INFINITY, 1e20}) {
            InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
            Map<String, String> config = getProblemConfig(input);
            config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
            config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, Gem5Config.ARGUMENT_DIALECT_FADSE_O3);
            Path repairs = temporaryFolder.getRoot().toPath().resolve("fractional-repairs.csv");
            config.put(Gem5Config.GEM5_REPAIRS_LOG, repairs.toString());
            TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
            runner.setIndividual(new FadseIndividual(input, "/bin/true"));
            DoubleParameter width = new DoubleParameter(-Double.MAX_VALUE, Double.MAX_VALUE);
            width.setName("width");
            width.setValue(value);
            runner.setParameters(new ProblemParameter<?>[] {width});

            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, runner::commandLine);
            assertTrue(error.getMessage().contains("width="));
            assertFalse(Files.exists(repairs));
        }
    }

    @Test
    public void fadseO3DirectConnectorAcceptsExactlyIntegralDoubleVariables() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        Map<String, String> config = getProblemConfig(input);
        config.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/renamed/custom-o3.py");
        config.put(Gem5Config.GEM5_ARGUMENT_DIALECT, Gem5Config.ARGUMENT_DIALECT_FADSE_O3);
        DoubleParameter width = new DoubleParameter(2, 8);
        width.setName("width");
        width.setValue(4.0);
        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        runner.setParameters(new ProblemParameter<?>[] {width});

        assertTrue(Arrays.asList(runner.commandLine()).contains("--width=4"));
    }

    @Test
    public void rejectsUnsupportedPythonObjectiveBeforeCreatingARequest() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        getProblemConfig(input).put(Gem5Config.GEM5_RUNNER_SCRIPT, "/repo/scripts/run_gem5_candidate.py");
        input.set(CommonSetupParameters.OBJECTIVES, Map.of(
                "unsupported", new Objective("unsupported", "float", true)
        ));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Gem5Simulator(input)
        );
        assertTrue(error.getMessage().contains("unsupported"));
    }

    private TestableGem5Runner pythonRunner() {
        InputData input = createInputData(temporaryFolder.getRoot().getAbsolutePath());
        getProblemConfig(input).put(Gem5Config.GEM5_RUNNER_SCRIPT, "/repo/scripts/run_gem5_candidate.py");
        getProblemConfig(input).put(Gem5Config.GEM5_REPAIRS_LOG,
                temporaryFolder.getRoot().toPath().resolve("repairs.csv").toString());
        TestableGem5Runner runner = new TestableGem5Runner(new Gem5Simulator(input));
        runner.setIndividual(new FadseIndividual(input, "/bin/true"));
        runner.setParameters(createSpecParameters());
        return runner;
    }

    private static InputData createInputData(String outputDirectory) {
        InputData inputData = new InputData();
        inputData.set(CommonSetupParameters.NAME, "gem5");
        inputData.set(CommonSetupParameters.PROBLEM_CONFIG, createProblemConfig(outputDirectory));
        inputData.set(CommonSetupParameters.DESIGN_VARIABLES, createParameters());
        inputData.set(CommonSetupParameters.OBJECTIVES, createObjectives());
        return inputData;
    }

    private static Map<String, String> createProblemConfig(String outputDirectory) {
        Map<String, String> problemConfig = new LinkedHashMap<>();
        problemConfig.put(Gem5Config.GEM5_EXECUTABLE, "/opt/gem5/build/X86/gem5.opt");
        problemConfig.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/opt/gem5/configs/example/se.py");
        problemConfig.put(Gem5Config.GEM5_OUTPUT_DIR, outputDirectory);
        problemConfig.put(Gem5Config.GEM5_STATS_FILE, Gem5Config.DEFAULT_STATS_FILE);
        return problemConfig;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProblemConfig(InputData inputData) {
        return (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> config(InputData inputData) {
        return (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
    }

    private static ProblemParameter<?>[] createParameters() {
        IntegerParameter cpuClock = new IntegerParameter(1, 4);
        cpuClock.setName("cpu-clock");
        cpuClock.setValue(3);

        IntegerParameter numCpus = new IntegerParameter(1, 8);
        numCpus.setName("num-cpus");
        numCpus.setValue(2);

        return new ProblemParameter<?>[] { cpuClock, numCpus };
    }

    private static ProblemParameter<?>[] createSpecParameters() {
        IntegerParameter width = integerParameter("width", 2, 8, 4);
        IntegerParameter robEntries = integerParameter("rob_entries", 64, 256, 64);
        IntegerParameter iqEntries = integerParameter("iq_entries", 32, 128, 128);
        IntegerParameter lsqEntries = integerParameter("lsq_entries", 32, 96, 64);
        IntegerParameter l1iKb = integerParameter("l1i_kb", 16, 64, 32);
        IntegerParameter l1dKb = integerParameter("l1d_kb", 16, 64, 32);
        IntegerParameter l2Kb = integerParameter("l2_kb", 256, 2048, 512);
        IntegerParameter l2Assoc = integerParameter("l2_assoc", 4, 16, 8);

        return new ProblemParameter<?>[] {
                width, robEntries, iqEntries, lsqEntries,
                l1iKb, l1dKb, l2Kb, l2Assoc
        };
    }

    private static IntegerParameter integerParameter(String name, int min, int max, int value) {
        IntegerParameter parameter = new IntegerParameter(min, max);
        parameter.setName(name);
        parameter.setValue(value);
        return parameter;
    }

    private static Map<String, Objective> createObjectives() {
        Map<String, Objective> objectives = new LinkedHashMap<>();
        objectives.put("simTicks", new Objective("simTicks", "float", true));
        return objectives;
    }

    private static class TestableGem5Runner extends Gem5Runner {
        TestableGem5Runner(Gem5Simulator simulator) {
            super(simulator);
        }

        String[] commandLine() {
            return getCommandLine();
        }
    }
}
