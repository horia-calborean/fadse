package core.network.client.gem5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

final class Gem5WorkloadManifest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private Gem5WorkloadManifest() {
    }

    static Entry resolve(String manifestPath, String entryId) {
        Path path = Paths.get(manifestPath).toAbsolutePath().normalize();
        try {
            JsonNode root = JSON_MAPPER.readTree(path.toFile());
            JsonNode entries = root.path("entries");
            if (!entries.isArray()) {
                throw new IllegalStateException("gem5 workload manifest has no entries array: " + path);
            }
            for (JsonNode node : entries) {
                if (entryId.equals(node.path("id").asText())) {
                    return Entry.from(node);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read gem5 workload manifest: " + path, exception);
        }
        throw new IllegalStateException(
                "Workload '" + entryId + "' was not found in gem5 workload manifest: " + path
        );
    }

    static void validate(String manifestPath) {
        Path path = Paths.get(manifestPath).toAbsolutePath().normalize();
        try {
            JsonNode root = JSON_MAPPER.readTree(path.toFile());
            JsonNode entries = root.path("entries");
            if (!entries.isArray()) {
                throw new IllegalStateException("gem5 workload manifest has no entries array: " + path);
            }
            for (JsonNode node : entries) {
                Entry.from(node);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read gem5 workload manifest: " + path, exception);
        }
    }

    static final class Entry {
        private final String id;
        private final String benchmark;
        private final String binary;
        private final String arguments;
        private final String cwd;
        private final String stdin;
        private final String memorySize;
        private final boolean isolateCwd;
        private final Map<String, String> environment;

        private Entry(
                String id,
                String benchmark,
                String binary,
                String arguments,
                String cwd,
                String stdin,
                String memorySize,
                boolean isolateCwd,
                Map<String, String> environment
        ) {
            this.id = id;
            this.benchmark = benchmark;
            this.binary = binary;
            this.arguments = arguments;
            this.cwd = cwd;
            this.stdin = stdin;
            this.memorySize = memorySize;
            this.isolateCwd = isolateCwd;
            this.environment = environment;
        }

        private static Entry from(JsonNode node) {
            String id = required(node, "id");
            String benchmark = required(node, "benchmark");
            String binary = required(node, "binary");
            Map<String, String> environment = new LinkedHashMap<>();
            JsonNode environmentNode = node.path("environment");
            if (!environmentNode.isMissingNode() && !environmentNode.isNull()) {
                if (!environmentNode.isObject()) {
                    throw new IllegalStateException(
                            "Workload manifest field 'environment' must be an object"
                    );
                }
                environmentNode.fields().forEachRemaining(
                        field -> environment.put(
                                field.getKey(), requiredText(field.getValue(), "environment." + field.getKey())
                        )
                );
            }
            String memorySize = optionalText(node, "memory_size");
            if (Gem5Config.hasValue(memorySize)) {
                Gem5Config.validateMemorySize(memorySize);
            }
            return new Entry(
                    id,
                    benchmark,
                    binary,
                    optionalText(node, "args"),
                    optionalText(node, "cwd"),
                    optionalText(node, "stdin"),
                    memorySize,
                    optionalBoolean(node, "isolate_cwd"),
                    environment
            );
        }

        private static String required(JsonNode node, String field) {
            JsonNode valueNode = node.path(field);
            if (valueNode.isMissingNode() || valueNode.isNull()) {
                throw new IllegalStateException("Missing required workload manifest field: " + field);
            }
            String value = requiredText(valueNode, field);
            if (value.trim().isEmpty()) {
                throw new IllegalStateException("Missing required workload manifest field: " + field);
            }
            return value;
        }

        private static String requiredText(JsonNode node, String field) {
            if (!node.isTextual()) {
                throw new IllegalStateException(
                        "Workload manifest field '" + field + "' must be text"
                );
            }
            return node.textValue();
        }

        private static String optionalText(JsonNode node, String field) {
            JsonNode value = node.path(field);
            if (value.isMissingNode() || value.isNull()) {
                return "";
            }
            return requiredText(value, field);
        }

        private static boolean optionalBoolean(JsonNode node, String field) {
            JsonNode value = node.path(field);
            if (value.isMissingNode() || value.isNull()) {
                return false;
            }
            if (!value.isBoolean()) {
                throw new IllegalStateException(
                        "Workload manifest field '" + field + "' must be boolean"
                );
            }
            return value.booleanValue();
        }

        String getId() {
            return id;
        }

        String getBenchmark() {
            return benchmark;
        }

        String getBinary() {
            return binary;
        }

        String getArguments() {
            return arguments;
        }

        String getCwd() {
            return cwd;
        }

        String getStdin() {
            return stdin;
        }

        String getMemorySize() {
            return memorySize;
        }

        boolean isIsolateCwd() {
            return isolateCwd;
        }

        Map<String, String> getEnvironment() {
            return environment;
        }
    }
}
