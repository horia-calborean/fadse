package core.network.client.gem5;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Gem5GeneratedContractTest {
    @Test
    public void generatedAdaptersMatchTheAuthoritativeContract() throws Exception {
        Process process = new ProcessBuilder(
                "python3", "scripts/generate_gem5_contract.py", "--check"
        ).inheritIO().start();
        assertTrue("Contract check timed out", process.waitFor(10, TimeUnit.SECONDS));
        assertEquals("Generated gem5 contract adapters are stale", 0, process.exitValue());
    }
}
