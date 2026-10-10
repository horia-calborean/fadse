package core.network.client;

import core.network.client.gem5.Gem5Simulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SimulatorRunnerCleanupTest {
    @Test public void stoppingAParentAlsoTerminatesAndReapsItsChild() throws Exception {
        assertTreeStopped(false);
    }

    @Test public void watchdogAlsoCleansAParentThatIgnoresTermination() throws Exception {
        assertTreeStopped(true);
    }

    private void assertTreeStopped(boolean ignoreTermination) throws Exception {
        InputData input = new InputData();
        input.set(CommonSetupParameters.PROBLEM_CONFIG, Map.of("gem5_executable", "/unused/gem5", "gem5_config_script", "/unused/se.py", "gem5_output_dir", "/unused/output"));
        SimulatorRunner runner = new SimulatorRunner(new Gem5Simulator(input));
        Process parent = new ProcessBuilder("/bin/sh", "-c",
                ignoreTermination ? "trap '' TERM; sleep 30 & echo $!; wait" : "sleep 30 & child=$!; trap 'kill $child 2>/dev/null; wait $child; exit 143' TERM; echo $child; wait $child").start();
        long childPid = Long.parseLong(parent.inputReader().readLine());
        ProcessHandle child = ProcessHandle.of(childPid).orElseThrow();
        try {
            assertTrue(child.isAlive());
            runner.p = parent;
            runner.stopRunning();
            assertTrue(parent.waitFor(5, TimeUnit.SECONDS));
            assertFalse(parent.isAlive());
            assertFalse(child.isAlive());
        } finally {
            child.destroyForcibly();
            parent.destroyForcibly();
        }
    }
}
