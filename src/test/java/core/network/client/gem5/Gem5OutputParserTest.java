package core.network.client.gem5;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Gem5OutputParserTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parsesConfiguredObjectivesFromGem5StatsFile() throws Exception {
        File statsFile = writeStatsFile(
                "---------- Begin Simulation Statistics ----------\n"
                        + "simTicks                                  34848000 # Number of ticks simulated\n"
                        + "simInsts                                      1234 # Number of instructions simulated\n"
                        + "hostSeconds                               0.000035\n"
                        + "---------- End Simulation Statistics ----------\n"
        );
        InputData inputData = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        simulator.setSimulatorOutputFile(statsFile.getAbsolutePath());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        parser.setObjectives(createObjectives());

        LinkedList<Objective> results = parser.getResults(new FadseIndividual(inputData, "/bin/echo"));

        assertEquals(2, results.size());
        assertObjectiveValue(results, "simTicks", 34848000.0);
        assertObjectiveValue(results, "simInsts", 1234.0);
    }

    @Test
    public void parsesSpecResultPropertiesFile() throws Exception {
        File statsFile = writeStatsFile(
                "status = ok\n"
                        + "cpi = 1.25\n"
                        + "l2_mpki_pos = 3.5\n"
                        + "cost_proxy = 7.75\n"
        );
        InputData inputData = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        simulator.setSimulatorOutputFile(statsFile.getAbsolutePath());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        parser.setObjectives(createSpecObjectives());

        LinkedList<Objective> results = parser.getResults(new FadseIndividual(inputData, "/bin/echo"));

        assertEquals(3, results.size());
        assertObjectiveValue(results, "cpi", 1.25);
        assertObjectiveValue(results, "l2_mpki_pos", 3.5);
        assertObjectiveValue(results, "cost_proxy", 7.75);
    }

    @Test
    public void missingObjectiveStatSetsWorstObjectiveValues() throws Exception {
        File statsFile = writeStatsFile("simTicks 34848000 # simInsts is missing\n");
        InputData inputData = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        simulator.setSimulatorOutputFile(statsFile.getAbsolutePath());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        parser.setObjectives(createObjectives());

        LinkedList<Objective> results = parser.getResults(new FadseIndividual(inputData, "/bin/echo"));

        assertEquals(2, results.size());
        assertObjectiveValue(results, "simTicks", Double.MAX_VALUE);
        assertObjectiveValue(results, "simInsts", Double.MIN_VALUE);
    }

    @Test
    public void missingStatsFileSetsWorstObjectiveValues() {
        InputData inputData = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        simulator.setSimulatorOutputFile(new File(temporaryFolder.getRoot(), "missing-stats.txt").getAbsolutePath());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        parser.setObjectives(createObjectives());

        LinkedList<Objective> results = parser.getResults(new FadseIndividual(inputData, "/bin/echo"));

        assertEquals(2, results.size());
        assertObjectiveValue(results, "simTicks", Double.MAX_VALUE);
        assertObjectiveValue(results, "simInsts", Double.MIN_VALUE);
    }

    @Test
    public void ignoresIncompleteFinalDumpAndUsesLastCompleteDump() throws Exception {
        File stats = writeStatsFile("---------- Begin Simulation Statistics ----------\n"
                + "simTicks 100\nsimInsts 10\n---------- End Simulation Statistics ----------\n"
                + "---------- Begin Simulation Statistics ----------\nsimTicks 999\n");
        InputData input = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(input);
        simulator.setSimulatorOutputFile(stats.toString());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        parser.setObjectives(createObjectives());
        LinkedList<Objective> results = parser.getResults(new FadseIndividual(input, "/bin/echo"));
        assertObjectiveValue(results, "simTicks", 100);
        assertObjectiveValue(results, "simInsts", 10);
    }

    @Test
    public void parsesVectorCounterNamesWithDoubleColons() throws Exception {
        File stats = writeStatsFile("system.l2.overallMisses::total 5 # misses = measured\n");
        InputData input = createInputData();
        Gem5Simulator simulator = new Gem5Simulator(input);
        simulator.setSimulatorOutputFile(stats.toString());
        Gem5OutputParser parser = new Gem5OutputParser(simulator);
        String name = "system.l2.overallMisses::total";
        parser.setObjectives(Map.of(name, new Objective(name, "float", true)));
        assertObjectiveValue(parser.getResults(new FadseIndividual(input, "/bin/echo")), name, 5);
    }

    private File writeStatsFile(String contents) throws IOException {
        File statsFile = temporaryFolder.newFile("stats.txt");
        try (FileWriter writer = new FileWriter(statsFile)) {
            writer.write(contents);
        }
        return statsFile;
    }

    private static InputData createInputData() {
        InputData inputData = new InputData();
        inputData.set(CommonSetupParameters.NAME, "gem5");
        inputData.set(CommonSetupParameters.PROBLEM_CONFIG, createProblemConfig());
        inputData.set(CommonSetupParameters.DESIGN_VARIABLES, createParameters());
        inputData.set(CommonSetupParameters.OBJECTIVES, createObjectives());
        return inputData;
    }

    private static Map<String, String> createProblemConfig() {
        Map<String, String> problemConfig = new LinkedHashMap<>();
        problemConfig.put(Gem5Config.GEM5_EXECUTABLE, "/opt/gem5/build/X86/gem5.opt");
        problemConfig.put(Gem5Config.GEM5_CONFIG_SCRIPT, "/opt/gem5/configs/example/se.py");
        problemConfig.put(Gem5Config.GEM5_OUTPUT_DIR, "/tmp/fadse-gem5-#");
        problemConfig.put(Gem5Config.GEM5_STATS_FILE, Gem5Config.DEFAULT_STATS_FILE);
        return problemConfig;
    }

    private static ProblemParameter<?>[] createParameters() {
        IntegerParameter parameter = new IntegerParameter(1, 4);
        parameter.setName("cpu-clock");
        parameter.setValue(3);
        return new ProblemParameter<?>[] { parameter };
    }

    private static Map<String, Objective> createObjectives() {
        Map<String, Objective> objectives = new LinkedHashMap<>();
        objectives.put("simTicks", new Objective("simTicks", "float", true));
        objectives.put("simInsts", new Objective("simInsts", "float", false));
        return objectives;
    }

    private static Map<String, Objective> createSpecObjectives() {
        Map<String, Objective> objectives = new LinkedHashMap<>();
        objectives.put("cpi", new Objective("cpi", "float", true));
        objectives.put("l2_mpki_pos", new Objective("l2_mpki_pos", "float", true));
        objectives.put("cost_proxy", new Objective("cost_proxy", "float", true));
        return objectives;
    }

    private static void assertObjectiveValue(LinkedList<Objective> objectives, String name, double expectedValue) {
        for (Objective objective : objectives) {
            if (name.equals(objective.getName())) {
                assertEquals(expectedValue, objective.getValue(), 0.000001);
                return;
            }
        }
        throw new AssertionError("Objective not found: " + name);
    }
}
