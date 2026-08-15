package core.network.client.gem5;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class Gem5WorkloadManifestTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void resolvesSpecInvocationMetadata() throws Exception {
        File manifest = temporaryFolder.newFile("spec2026.local.json");
        Files.writeString(
                manifest.toPath(),
                "{\n"
                        + "  \"entries\": [{\n"
                        + "    \"id\": \"821.gcc_s__000\",\n"
                        + "    \"benchmark\": \"821.gcc_s\",\n"
                        + "    \"binary\": \"/spec/gcc_s\",\n"
                        + "    \"args\": \"input.c -O2\",\n"
                        + "    \"cwd\": \"/spec/run/gcc\",\n"
                        + "    \"stdin\": \"/spec/run/gcc/input.txt\",\n"
                        + "    \"memory_size\": \"64GB\",\n"
                        + "    \"isolate_cwd\": true,\n"
                        + "    \"environment\": {\"OMP_NUM_THREADS\": \"1\"}\n"
                        + "  }]\n"
                        + "}\n"
        );

        Gem5WorkloadManifest.Entry entry = Gem5WorkloadManifest.resolve(
                manifest.getAbsolutePath(),
                "821.gcc_s__000"
        );

        assertEquals("821.gcc_s", entry.getBenchmark());
        assertEquals("/spec/gcc_s", entry.getBinary());
        assertEquals("input.c -O2", entry.getArguments());
        assertEquals("/spec/run/gcc", entry.getCwd());
        assertEquals("/spec/run/gcc/input.txt", entry.getStdin());
        assertEquals("64GB", entry.getMemorySize());
        assertEquals(true, entry.isIsolateCwd());
        assertEquals(Map.of("OMP_NUM_THREADS", "1"), entry.getEnvironment());
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsUnknownInvocation() throws Exception {
        File manifest = temporaryFolder.newFile("spec2026.local.json");
        Files.writeString(manifest.toPath(), "{\"entries\": []}\n");

        Gem5WorkloadManifest.resolve(manifest.getAbsolutePath(), "missing");
    }

    @Test
    public void rejectsNonTextArgumentsInsteadOfSilentlyDroppingThem() throws Exception {
        File manifest = temporaryFolder.newFile("array-args.json");
        Files.writeString(manifest.toPath(), "{\"entries\":[{"
                + "\"id\":\"entry\",\"benchmark\":\"bench\",\"binary\":\"/bin/bench\","
                + "\"args\":[\"input\",\"-O2\"]}]}\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> Gem5WorkloadManifest.resolve(manifest.getAbsolutePath(), "entry")
        );
        assertEquals(true, error.getMessage().contains("args"));
    }

    @Test
    public void reportsMissingRequiredFieldsAsMissing() throws Exception {
        File manifest = temporaryFolder.newFile("missing-binary.json");
        Files.writeString(manifest.toPath(),
                "{\"entries\":[{\"id\":\"entry\",\"benchmark\":\"bench\"}]}\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> Gem5WorkloadManifest.resolve(manifest.getAbsolutePath(), "entry")
        );
        assertEquals(true, error.getMessage().contains("Missing required"));
        assertEquals(true, error.getMessage().contains("binary"));
    }

    @Test
    public void validatesMemoryGeometryForEveryEntry() throws Exception {
        File manifest = temporaryFolder.newFile("invalid-memory.json");
        Files.writeString(manifest.toPath(), "{\"entries\":["
                + "{\"id\":\"good\",\"benchmark\":\"bench\",\"binary\":\"/bin/bench\"},"
                + "{\"id\":\"bad\",\"benchmark\":\"bench\",\"binary\":\"/bin/bench\","
                + "\"memory_size\":\"6GB\"}]}");
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Gem5WorkloadManifest.validate(manifest.getAbsolutePath()));
        assertEquals(true, error.getMessage().contains("power-of-two"));
    }
}
