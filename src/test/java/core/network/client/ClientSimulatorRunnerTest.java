package core.network.client;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.network.Message;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ClientSimulatorRunnerTest {
    @Test
    public void simulationFailurePublishesARetryableWorstValuedIndividual() {
        InputData input = inputData();
        FadseIndividual individual = new FadseIndividual(input, "benchmark");
        Simulator simulator = new Simulator(input) {
            @Override
            public void performSimulation(FadseIndividual ignored) {
                throw new IllegalStateException("candidate creation failed");
            }
        };
        Message message = new Message();
        message.setMessageId("request-1");
        AtomicReference<FadseIndividual> sent = new AtomicReference<>();

        ClientSimulatorRunner runner = new ClientSimulatorRunner(individual, simulator, message) {
            @Override
            protected ResultsSender createResultsSender() {
                return new ResultsSender() {
                    @Override
                    public void send(FadseIndividual value, Message ignored) throws IOException {
                        sent.set(value);
                    }
                };
            }
        };

        runner.run();

        assertSame(individual, sent.get());
        assertFalse(individual.isFeasible());
        assertEquals(Double.MAX_VALUE, individual.getObjectives().get(0).getValue(), 0.0);
        assertTrue(message.isRetryableFailure());
        assertTrue(message.getFailureDescription().contains("candidate creation failed"));
    }

    private static InputData inputData() {
        InputData input = new InputData();
        IntegerParameter parameter = new IntegerParameter(1, 2);
        parameter.setName("parameter");
        input.set(CommonSetupParameters.DESIGN_VARIABLES, new IntegerParameter[] { parameter });
        Map<String, Objective> objectives = new LinkedHashMap<>();
        objectives.put("objective", new Objective("objective", "float", true));
        input.set(CommonSetupParameters.OBJECTIVES, objectives);
        return input;
    }
}
