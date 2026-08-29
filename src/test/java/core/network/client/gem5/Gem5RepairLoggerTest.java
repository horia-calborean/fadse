package core.network.client.gem5;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class Gem5RepairLoggerTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void repairLoggingWaitsForTheSamePosixLockAsPython() throws Exception {
        Path csv = temporary.getRoot().toPath().resolve("repairs.csv");
        Gem5Candidate candidate = Gem5CandidateFactory.create(
                Map.of("gem5_executable", "/unused/gem5", "gem5_config_script", "/unused/config.py"),
                Map.of("rob_entries", 64, "iq_entries", 128), "bfs");
        Process python = new ProcessBuilder("python3", "-c",
                "import fcntl, sys\nwith open(sys.argv[1], 'a') as lock:\n"
                        + " fcntl.lockf(lock, fcntl.LOCK_EX)\n print('locked', flush=True)\n sys.stdin.readline()\n",
                csv + ".lock").start();
        var executor = Executors.newSingleThreadExecutor();
        try {
            assertEquals("locked", python.inputReader().readLine());
            var append = executor.submit(() -> Gem5CandidateRepairLogger.append(csv, candidate));
            assertThrows(TimeoutException.class, () -> append.get(150, TimeUnit.MILLISECONDS));
            var input = python.outputWriter();
            input.write("release\n");
            input.flush();
            append.get(5, TimeUnit.SECONDS);
            assertTrue(python.waitFor(5, TimeUnit.SECONDS));
            assertEquals(0, python.exitValue());
            assertEquals(2, Files.readAllLines(csv).size());
        } finally {
            python.destroyForcibly();
            executor.shutdownNow();
        }
    }

    @Test
    public void deduplicatesRepairsWithinARunButNotAcrossRuns() throws Exception {
        Path csv = temporary.getRoot().toPath().resolve("deduplicated.csv");
        Gem5Candidate candidate = Gem5CandidateFactory.create(
                Map.of("gem5_executable", "/unused/gem5", "gem5_config_script", "/unused/config.py"),
                Map.of("rob_entries", 64, "iq_entries", 128), "bfs");
        Gem5CandidateRepairLogger.append(csv, "run-a", candidate.getCandidateId(),
                candidate.getOriginalArchitecture(), candidate.getArchitecture(), candidate.getRepairs());
        Gem5CandidateRepairLogger.append(csv, "run-a", candidate.getCandidateId(),
                candidate.getOriginalArchitecture(), candidate.getArchitecture(), candidate.getRepairs());
        assertEquals(2, Files.readAllLines(csv).size());
        Gem5CandidateRepairLogger.append(csv, "run-b", candidate.getCandidateId(),
                candidate.getOriginalArchitecture(), candidate.getArchitecture(), candidate.getRepairs());
        assertEquals(3, Files.readAllLines(csv).size());
        assertTrue(Files.readString(csv).startsWith("dedupe_key,run_id,candidate_id"));
    }

    @Test
    public void dedupeKeyUsesSharedSha256AndNormalizesNulls() throws Exception {
        Path csv = temporary.getRoot().toPath().resolve("null-key.csv");
        String reason = "normalized-null-key";
        Gem5CandidateRepairLogger.append(csv, null, null, Map.of(), Map.of(),
                List.of(new Gem5CandidateRepair("width", 3, 4, reason)));

        String key = Files.readAllLines(csv).get(1).split(",", 2)[0];
        assertEquals(Gem5CandidateId.sha256("\0\0" + reason), key);
    }

    @Test
    public void cacheNoticesRowsAppendedByAnotherProcess() throws Exception {
        Path csv = temporary.getRoot().toPath().resolve("external-row.csv");
        String reason = "external-repair";
        Gem5CandidateRepair repair = new Gem5CandidateRepair("width", 3, 4, reason);
        Gem5CandidateRepairLogger.append(csv, "run-a", "candidate", Map.of(), Map.of(),
                List.of(repair));
        String key = Gem5CandidateId.sha256("run-b\0candidate\0" + reason);
        Files.writeString(csv, key + ",run-b,candidate,{},{}," + reason + "\n",
                java.nio.file.StandardOpenOption.APPEND);

        Gem5CandidateRepairLogger.append(csv, "run-b", "candidate", Map.of(), Map.of(),
                List.of(repair));

        assertEquals(3, Files.readAllLines(csv).size());
    }
}
