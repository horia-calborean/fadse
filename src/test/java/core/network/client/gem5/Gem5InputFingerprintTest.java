package core.network.client.gem5;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        assertEquals("nsga2_bfs_seed0_budget1_aa0eb0f970ed930f7e66d96f",
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
