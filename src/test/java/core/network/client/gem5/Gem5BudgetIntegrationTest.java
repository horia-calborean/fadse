package core.network.client.gem5;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.network.Message;
import core.network.client.ClientSimulatorRunner;
import core.network.client.ResultsSender;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Exercises Java -> Python requests and physical-launch accounting without gem5. */
public class Gem5BudgetIntegrationTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test(timeout = 20000)
    @SuppressWarnings("unchecked")
    public void sharesOneQuotaAcrossWorkersAndServesCacheWithoutAnotherLaunch() throws Exception {
        Path root = temporary.getRoot().toPath();
        Path executable = root.resolve("fake-gem5.py");
        Files.writeString(executable, "#!/usr/bin/env python3\n"
                + "import pathlib, sys\n"
                + "out = pathlib.Path(next(a.split('=', 1)[1] for a in sys.argv if a.startswith('--outdir=')))\n"
                + "(out / 'stats.txt').write_text('---------- Begin Simulation Statistics ----------\\n"
                + "system.cpu.numCycles 1000\\nsimInsts 100\\nsystem.l2.overallMisses::total 2\\n"
                + "---------- End Simulation Statistics ----------\\n')\n");
        assertTrue(executable.toFile().setExecutable(true));
        Path config = Files.createDirectories(root.resolve("config")).resolve("o3_fadse_se.py");
        Files.writeString(config, "# unused by fake simulator\n");
        Map<String, String> settings = new LinkedHashMap<>();
        settings.put("gem5_executable", executable.toString());
        settings.put("gem5_config_script", config.toString());
        settings.put("gem5_runner_script", "scripts/run_gem5_candidate.py");
        settings.put("gem5_output_dir", root.resolve("candidates").toString());
        settings.put("gem5_run_root", root.resolve("runs").toString());
        settings.put("gem5_launch_budget", "1");
        settings.put("gem5_timeout_seconds", "10");
        settings.put("gem5_warmup_insts", "0");
        settings.put("gem5_repairs_log", root.resolve("repairs.csv").toString());
        InputData input = new InputData();
        input.set(CommonSetupParameters.NAME, "gem5");
        input.set(CommonSetupParameters.PROBLEM_CONFIG, settings);
        Map<String, Objective> objectives = new LinkedHashMap<>();
        for (String name : List.of("cpi", "l2_mpki_pos", "cost_proxy")) {
            objectives.put(name, new Objective(name, "float", true));
        }
        input.set(CommonSetupParameters.OBJECTIVES, objectives);
        List<ProblemParameter<?>> parameters = new ArrayList<>();
        Map.of("width", 4, "rob_entries", 128, "iq_entries", 64, "lsq_entries", 32,
                "l1i_kb", 32, "l1d_kb", 32, "l2_kb", 512, "l2_assoc", 8).forEach((name, value) -> {
            IntegerParameter parameter = new IntegerParameter(value, value);
            parameter.setName(name);
            parameter.setValue(value);
            parameters.add(parameter);
        });
        input.set(CommonSetupParameters.DESIGN_VARIABLES, parameters.toArray(ProblemParameter<?>[]::new));
        Gem5RunContext.initialize(input);

        for (String benchmark : List.of("/bin/true", "/bin/true", "/bin/false")) {
            Gem5Simulator worker = new Gem5Simulator(input);
            FadseIndividual individual = new FadseIndividual(input, benchmark);
            if (benchmark.equals("/bin/false")) {
                assertFailureIsSent(worker, individual);
                assertFalse(individual.isFeasible());
                assertTrue(individual.getObjectives().stream().allMatch(o -> o.getValue() == Double.MAX_VALUE));
            } else {
                worker.performSimulation(individual);
                assertTrue(individual.isFeasible());
                assertEquals(10, individual.getObjectives().getFirst().getValue(), 1e-12);
                var result = new ObjectMapper().readTree(Path.of(worker.getSimulatorOutputFile())
                        .resolveSibling("fadse_result.json").toFile());
                assertEquals("Java and Python must share cache identity", result.path("candidate_id").asText(),
                        result.path("fingerprint").asText());
            }
        }
        Map<String, String> configured = (Map<String, String>) input.get(CommonSetupParameters.PROBLEM_CONFIG);
        var ledger = new ObjectMapper().readTree(Path.of(configured.get("gem5_budget_file")).toFile());
        assertEquals(1, ledger.path("used").asInt());
        assertEquals(1, ledger.path("launches").size());
        try (var paths = Files.walk(root.resolve("candidates"))) {
            assertTrue("Per-request snapshots must be removed after parsing",
                    paths.noneMatch(path -> path.getFileName().toString().startsWith("request-")));
        }
        List<String> records = Files.readAllLines(Path.of(configured.get("gem5_evaluated_csv")));
        assertEquals(4, records.size());
        assertTrue(records.getLast().contains("budget_exhausted"));
        assertTrue(records.stream().skip(1).allMatch(row -> row.contains(configured.get("gem5_run_id"))));
    }

    private static void assertFailureIsSent(Gem5Simulator worker, FadseIndividual individual) {
        List<FadseIndividual> responses = new ArrayList<>();
        Message request = new Message();
        request.setMessageId("exhausted-budget-" + java.util.UUID.randomUUID());
        request.setIndividual(individual);
        new ClientSimulatorRunner(individual, worker, request) {
            @Override protected ResultsSender createResultsSender() {
                return new ResultsSender() {
                    @Override public void send(FadseIndividual result, Message message) {
                        assertSame(request, message);
                        responses.add(result);
                    }
                };
            }
        }.run();
        assertEquals("Published failures must reach ResultsSender", List.of(individual), responses);
    }
}
