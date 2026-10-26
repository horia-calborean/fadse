package core.network.client.gem5;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class Gem5InputFingerprintTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void contentChangesInvalidateIdentityEvenWithUnchangedSizeAndTime() throws Exception {
        Path root = temporary.getRoot().toPath();
        Path config = Files.createDirectory(root.resolve("config"));
        Path inputs = Files.createDirectory(root.resolve("inputs"));
        List<Path> files = List.of(root.resolve("gem5"), root.resolve("binary"), config.resolve("main.py"),
                config.resolve("helper.py"), inputs.resolve("input"));
        for (Path file : files) Files.writeString(file, "before");
        String original = identity(root);
        for (Path file : files) {
            var stamp = Files.getLastModifiedTime(file);
            Files.writeString(file, "after!");
            Files.setLastModifiedTime(file, stamp);
            assertNotEquals(file.toString(), original, identity(root));
            Files.writeString(file, "before");
            assertEquals(original, identity(root));
        }
        Files.createDirectory(config.resolve("__pycache__"));
        Files.writeString(config.resolve("__pycache__/helper.pyc"), "compiled");
        assertEquals(original, identity(root));
        Files.writeString(inputs.resolve("new-input"), "new");
        assertEquals(original, identity(root));
    }

    @Test
    public void treeHashMatchesPythonAndFollowsInputSymlinks() throws Exception {
        Path root = temporary.getRoot().toPath();
        Files.writeString(root.resolve("a"), "abc");
        Files.createDirectory(root.resolve("z"));
        Files.writeString(root.resolve("z/☃"), "data");
        assertEquals("ca1dc8bf41fda5815f6c9f53c753d983fd39921d6a48426e1991af9f1ee844bb",
                Gem5InputFingerprint.hash(root.toString(), false));
        Files.createSymbolicLink(root.resolve("link"), root.resolve("a"));
        String original = Gem5InputFingerprint.hash(root.toString(), false);
        Files.writeString(root.resolve("a"), "xyz");
        assertNotEquals(original, Gem5InputFingerprint.hash(root.toString(), false));
        Files.createSymbolicLink(root.resolve("cycle"), root);
        assertThrows(IllegalStateException.class, () -> Gem5InputFingerprint.hash(root.toString(), false));
    }

    @Test
    public void candidateIdMatchesThePythonGoldenVector() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("method", "nsga2");
        fields.put("benchmark", "bfs");
        fields.put("seed", "0");
        fields.put("budget_index", "1");
        fields.put("gem5_binary", "/missing/gem5");
        fields.put("config_script", "/missing/config.py");
        fields.put("timeout_seconds", "3600");
        fields.put("warmup_insts", "10000000");
        fields.put("maxinsts", "50000000");
        fields.put("cacheline_size", "64");
        fields.put("benchmark_binary", "/missing/bfs");
        fields.put("benchmark_args", "");
        fields.put("run_cwd", "");
        fields.put("stdin", "");
        fields.put("mem_size", "16GB");
        fields.put("isolate_cwd", "false");
        Map<String, Integer> architecture = new LinkedHashMap<>();
        architecture.put("width", 4);
        architecture.put("rob_entries", 128);
        architecture.put("iq_entries", 64);
        architecture.put("lsq_entries", 32);
        architecture.put("l1i_kb", 32);
        architecture.put("l1d_kb", 32);
        architecture.put("l2_kb", 512);
        architecture.put("l2_assoc", 8);
        architecture.put("num_cores", 1);
        assertEquals("nsga2_bfs_seed0_budget1_b1050c088c8cc1f000f70e55",
                Gem5CandidateId.of(new Gem5CandidateId.Input(fields, architecture, Map.of())));
    }

    @Test
    public void inputHashesAreCachedOnlyWithinOneDeclaredRun() throws Exception {
        Path binary = temporary.getRoot().toPath().resolve("gem5");
        Files.writeString(binary, "before");
        String first = Gem5InputFingerprint.hashes(
                "run-a", binary.toString(), "", "", "", "")[0];

        Files.writeString(binary, "after!");

        assertEquals(first, Gem5InputFingerprint.hashes(
                "run-a", binary.toString(), "", "", "", "")[0]);
        assertNotEquals(first, Gem5InputFingerprint.hashes(
                "run-b", binary.toString(), "", "", "", "")[0]);
    }

    @Test
    public void numericSpellingsHaveOneIdentityAcrossJavaAndSerializedPythonRequests() throws Exception {
        Map<String, Integer> architecture = Map.of("width", 4, "rob_entries", 128, "iq_entries", 64,
                "lsq_entries", 32, "l1i_kb", 32, "l1d_kb", 32, "l2_kb", 512, "l2_assoc", 8);
        Map<String, String> config = new LinkedHashMap<>(Map.of(
                "gem5_executable", "/missing/gem5", "gem5_config_script", "/missing/config.py",
                "gem5_seed", "1", "gem5_budget_index", "1", "gem5_timeout_seconds", "3600",
                "gem5_warmup_insts", "0", "gem5_maxinsts", "1000", "gem5_cacheline_size", "64"));
        String canonical = Gem5CandidateFactory.create(config, architecture, "bfs").getCandidateId();
        for (String field : List.of("gem5_seed", "gem5_budget_index", "gem5_timeout_seconds",
                "gem5_warmup_insts", "gem5_maxinsts", "gem5_cacheline_size")) {
            Map<String, String> padded = new LinkedHashMap<>(config);
            padded.put(field, "+000" + config.get(field));
            Gem5Candidate candidate = Gem5CandidateFactory.create(padded, architecture, "bfs");
            assertEquals("Numeric spelling must not change identity: " + field,
                    canonical, candidate.getCandidateId());
            assertPythonIdentity(candidate, field);
        }
        config.put("gem5_seed", "5000000000");
        config.put("gem5_warmup_insts", "5000000000");
        Gem5Candidate large = Gem5CandidateFactory.create(config, architecture, "bfs");
        config.put("gem5_seed", "0005000000000");
        config.put("gem5_warmup_insts", "0005000000000");
        Gem5Candidate paddedLarge = Gem5CandidateFactory.create(config, architecture, "bfs");
        assertEquals(large.getCandidateId(), paddedLarge.getCandidateId());
        assertPythonIdentity(paddedLarge, "large-numbers");
    }

    private void assertPythonIdentity(Gem5Candidate candidate, String name) throws Exception {
        Path request = temporary.getRoot().toPath().resolve(name + ".json");
        candidate.writeJson(request, List.of("cpi"));
        Process python = new ProcessBuilder("python3", "scripts/candidate_hash.py",
                "--candidate-json", request.toString())
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            assertTrue("Python fingerprint computation timed out", python.waitFor(5, TimeUnit.SECONDS));
            assertEquals(0, python.exitValue());
            assertEquals(candidate.getCandidateId(), python.inputReader().readLine());
        } finally {
            if (python.isAlive()) python.destroyForcibly();
        }
    }

    private String identity(Path root) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("method", "nsga2");
        fields.put("benchmark", "bfs");
        fields.put("seed", "0");
        fields.put("budget_index", "0");
        fields.put("gem5_binary", root.resolve("gem5").toString());
        fields.put("config_script", root.resolve("config/main.py").toString());
        fields.put("timeout_seconds", "3600");
        fields.put("warmup_insts", "0");
        fields.put("maxinsts", "1000");
        fields.put("cacheline_size", "64");
        fields.put("benchmark_binary", root.resolve("binary").toString());
        fields.put("benchmark_args", "");
        fields.put("run_cwd", root.resolve("inputs").toString());
        fields.put("stdin", "input");
        fields.put("mem_size", "16GB");
        fields.put("isolate_cwd", "true");
        return Gem5CandidateId.of(new Gem5CandidateId.Input(fields, Map.of("width", 4), Map.of()));
    }
}
