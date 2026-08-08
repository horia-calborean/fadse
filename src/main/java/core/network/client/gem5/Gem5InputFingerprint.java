package core.network.client.gem5;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Content identity shared with scripts/candidate_hash.py; never cached by path. */
final class Gem5InputFingerprint {
    private static final int MAX_CACHED_INPUTS = 4096;
    private static final ConcurrentMap<RunFile, String> RUN_HASHES = new ConcurrentHashMap<>();

    private record RunFile(String runId, String path, boolean pythonSources) { }

    private Gem5InputFingerprint() {
    }

    static String[] hashes(String executable, String script, String binary, String cwd, String stdin) {
        return hashes("", executable, script, binary, cwd, stdin);
    }

    static String[] hashes(String runId, String executable, String script, String binary,
            String cwd, String stdin) {
        String input = stdin;
        if (!stdin.isEmpty() && !Path.of(stdin).isAbsolute()) {
            input = Path.of(cwd.isEmpty() ? "." : cwd).resolve(stdin).toString();
        }
        return new String[]{hashForRun(runId, executable, false), hashForRun(runId, script, true),
                hashForRun(runId, binary, false), cwd.isEmpty() ? "absent" : "path_only",
                hashForRun(runId, input, false)};
    }

    private static String hashForRun(String runId, String value, boolean pythonSources) {
        if (runId == null || runId.isBlank()) {
            return hash(value, pythonSources);
        }
        if (RUN_HASHES.size() >= MAX_CACHED_INPUTS) {
            // Clearing can only cause safe duplicate hashing; it cannot change
            // candidate identity. Keep long-lived clients from retaining every run.
            RUN_HASHES.clear();
        }
        return RUN_HASHES.computeIfAbsent(
                new RunFile(runId, value, pythonSources),
                ignored -> hash(value, pythonSources)
        );
    }

    static String hash(String value, boolean pythonSources) {
        if (value.isEmpty()) return "absent";
        Path path = Path.of(value);
        if (!Files.exists(path)) return "missing";
        try {
            if (Files.isRegularFile(path) && !pythonSources) {
                MessageDigest digest = digest();
                try (var input = Files.newInputStream(path)) {
                    byte[] buffer = new byte[65536];
                    int count;
                    while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
                }
                return HexFormat.of().formatHex(digest.digest());
            }
            Path root = pythonSources ? path.toAbsolutePath().getParent() : path.toAbsolutePath();
            Map<String, String> entries = new TreeMap<>();
            visit(root, root, path.toAbsolutePath(), pythonSources, new HashSet<>(), entries);
            MessageDigest digest = digest();
            entries.forEach((name, hash) -> {
                frame(digest, name);
                frame(digest, hash);
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot fingerprint gem5 input: " + path, e);
        }
    }

    private static void visit(Path root, Path directory, Path script, boolean pythonSources,
            Set<Path> ancestors, Map<String, String> entries) throws IOException {
        Path resolved = directory.toRealPath();
        if (!ancestors.add(resolved)) throw new IOException("Symlink cycle: " + directory);
        try (var children = Files.newDirectoryStream(directory)) {
            for (Path child : children) {
                if (Files.isDirectory(child)) {
                    if (!pythonSources || !child.getFileName().toString().equals("__pycache__")) {
                        visit(root, child, script, pythonSources, ancestors, entries);
                    }
                } else if (!pythonSources || child.toString().endsWith(".py") || child.equals(script)) {
                    entries.put(root.relativize(child).toString().replace('\\', '/'), hash(child.toString(), false));
                }
            }
        } finally {
            ancestors.remove(resolved);
        }
    }

    private static void frame(MessageDigest digest, String field) {
        byte[] bytes = field.getBytes(StandardCharsets.UTF_8);
        digest.update((bytes.length + ":").getBytes(StandardCharsets.US_ASCII));
        digest.update(bytes);
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
