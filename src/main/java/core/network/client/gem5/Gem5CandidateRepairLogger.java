package core.network.client.gem5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;

final class Gem5CandidateRepairLogger {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String HEADER = "dedupe_key,run_id,candidate_id,original_config_json,repaired_config_json,repair_reason";
    private static final Map<Path, KeyCache> KEY_CACHES = new HashMap<>();

    private record FileState(long size, FileTime modifiedTime, Object fileKey) {
        static FileState read(Path path) throws IOException {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            return new FileState(attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
        }
    }

    private record KeyCache(FileState state, Set<String> keys) {
    }

    private Gem5CandidateRepairLogger() {
    }

    static synchronized void append(
            Path csvPath,
            Gem5Candidate candidate
    ) {
        append(csvPath, candidate.getRunId(), candidate.getCandidateId(),
                candidate.getOriginalArchitecture(), candidate.getArchitecture(), candidate.getRepairs());
    }

    static synchronized void append(Path csvPath, String runId, String candidateId,
            java.util.Map<String, Integer> originalArchitecture,
            java.util.Map<String, Integer> architecture,
            List<Gem5CandidateRepair> repairs) {
        if (repairs == null || repairs.isEmpty()) {
            return;
        }

        try {
            csvPath = csvPath.toAbsolutePath().normalize();
            Path parent = csvPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (FileChannel lockChannel = FileChannel.open(
                    Path.of(csvPath + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock ignored = lockChannel.lock()) {
                boolean writeHeader = prepareSchema(csvPath);
                Set<String> existingKeys = loadKeys(csvPath, writeHeader);
                try (BufferedWriter writer = Files.newBufferedWriter(
                        csvPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                )) {
                    if (writeHeader) {
                        writer.write(HEADER);
                        writer.newLine();
                    }
                    for (Gem5CandidateRepair repair : repairs) {
                        String key = dedupeKey(runId, candidateId, repair.getReason());
                        if (!existingKeys.add(key)) {
                            continue;
                        }
                        writer.write(key);
                        writer.write(",");
                        writer.write(csv(runId));
                        writer.write(",");
                        writer.write(csv(candidateId));
                        writer.write(",");
                        writer.write(csv(toJson(originalArchitecture)));
                        writer.write(",");
                        writer.write(csv(toJson(architecture)));
                        writer.write(",");
                        writer.write(csv(repair.getReason()));
                        writer.newLine();
                    }
                }
                KEY_CACHES.put(csvPath, new KeyCache(
                        FileState.read(csvPath), new HashSet<>(existingKeys)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not append gem5 repair log: " + csvPath, e);
        }
    }

    private static boolean prepareSchema(Path csvPath) throws IOException {
        if (!Files.exists(csvPath) || Files.size(csvPath) == 0) {
            return true;
        }
        String existingHeader;
        try (var lines = Files.lines(csvPath, StandardCharsets.UTF_8)) {
            existingHeader = lines.findFirst().orElse("");
        }
        if (HEADER.equals(existingHeader)) {
            return false;
        }
        Path rotated = csvPath.resolveSibling(
                csvPath.getFileName() + ".schema-mismatch-" + UUID.randomUUID() + ".bak");
        Files.move(csvPath, rotated, StandardCopyOption.ATOMIC_MOVE);
        return true;
    }

    private static Set<String> loadKeys(Path csvPath, boolean empty) throws IOException {
        if (empty || !Files.exists(csvPath)) {
            KEY_CACHES.remove(csvPath);
            return new HashSet<>();
        }
        FileState state = FileState.read(csvPath);
        KeyCache cached = KEY_CACHES.get(csvPath);
        if (cached != null && cached.state().equals(state)) {
            return new HashSet<>(cached.keys());
        }
        Set<String> keys = new HashSet<>();
        try (var lines = Files.lines(csvPath, StandardCharsets.UTF_8)) {
            lines.skip(1).forEach(line -> {
                int comma = line.indexOf(',');
                keys.add(comma < 0 ? line : line.substring(0, comma));
            });
        }
        KEY_CACHES.put(csvPath, new KeyCache(state, new HashSet<>(keys)));
        return keys;
    }

    private static String dedupeKey(String runId, String candidateId, String reason) {
        return Gem5CandidateId.sha256(String.join("\0",
                nullToEmpty(runId), nullToEmpty(candidateId), nullToEmpty(reason)));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String toJson(Object value) {
        try {
            return JSON_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize gem5 repair config", e);
        }
    }

    private static String csv(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
