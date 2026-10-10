package core.network.client.gem5;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class Gem5IntegrationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runsRealGem5AndParsesStats() {
        String executable = System.getProperty("gem5.executable");
        String configScript = System.getProperty("gem5.configScript");
        String benchmark = System.getProperty("gem5.benchmark", "/bin/true");

        assumeTrue("Set -Dgem5.executable=/path/to/gem5.opt", executable != null);
        assumeTrue("Set -Dgem5.configScript=/path/to/se.py", configScript != null);
        assumeTrue("gem5 executable must exist", Files.isExecutable(Paths.get(executable)));
        assumeTrue("gem5 config script must exist", Files.isRegularFile(Paths.get(configScript)));
        assumeTrue("benchmark must exist", Files.isRegularFile(Paths.get(benchmark)));

        InputData inputData = createInputData(executable, configScript);
        Gem5Simulator simulator = new Gem5Simulator(inputData);
        FadseIndividual individual = new FadseIndividual(inputData, benchmark);

        simulator.performSimulation(individual);

        assertTrue("stats.txt should be produced", new File(simulator.getSimulatorOutputFile()).isFile());
        assertObjectiveIsPositive(individual, "simTicks");
        assertObjectiveIsPositive(individual, "simInsts");
        assertObjectiveIsPositive(individual, "hostSeconds");
    }

    private InputData createInputData(String executable, String configScript) {
        InputData inputData = new InputData();
        inputData.set(CommonSetupParameters.NAME, "gem5");
        inputData.set(CommonSetupParameters.PROBLEM_CONFIG, createProblemConfig(executable, configScript));
        inputData.set(CommonSetupParameters.DESIGN_VARIABLES, new ProblemParameter<?>[0]);
        inputData.set(CommonSetupParameters.OBJECTIVES, createObjectives());
        return inputData;
    }

    private Map<String, String> createProblemConfig(String executable, String configScript) {
        Map<String, String> problemConfig = new LinkedHashMap<>();
        problemConfig.put(Gem5Config.GEM5_EXECUTABLE, executable);
        problemConfig.put(Gem5Config.GEM5_CONFIG_SCRIPT, configScript);
        problemConfig.put(Gem5Config.GEM5_OUTPUT_DIR, temporaryFolder.getRoot().getAbsolutePath() + "/gem5-#");
        problemConfig.put(Gem5Config.GEM5_STATS_FILE, Gem5Config.DEFAULT_STATS_FILE);
        return problemConfig;
    }

    private static Map<String, Objective> createObjectives() {
        Map<String, Objective> objectives = new LinkedHashMap<>();
        objectives.put("simTicks", new Objective("simTicks", "float", true));
        objectives.put("simInsts", new Objective("simInsts", "float", false));
        objectives.put("hostSeconds", new Objective("hostSeconds", "float", true));
        return objectives;
    }

    private static void assertObjectiveIsPositive(FadseIndividual individual, String objectiveName) {
        for (Objective objective : individual.getObjectives()) {
            if (objectiveName.equals(objective.getName())) {
                assertTrue(objectiveName + " should be positive", objective.getValue() > 0.0);
                return;
            }
        }
        throw new AssertionError("Objective not found: " + objectiveName);
    }
}
