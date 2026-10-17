package core.network;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SimulationStatusTest {
    @Test
    public void inactiveResultCorrelationsSurviveUntilGenerationCleanup() {
        SimulationStatus status = SimulationStatus.getInstance();
        status.clearPerGenerationData();
        try {
            for (int index = 0; index < 9_002; index++) {
                Message message = new Message();
                message.setMessageId("retained-" + index);
                status.addSimulation(message, null, null);
                status.removeSimulation(message.getMessageId());
            }

            assertNotNull(status.getSimulation("retained-0"));
        } finally {
            status.clearPerGenerationData();
        }
    }
}
