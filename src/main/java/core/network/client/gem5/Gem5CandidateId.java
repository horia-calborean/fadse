package core.network.client.gem5;

import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/** Builds deterministic gem5 candidate IDs from the generated fingerprint contract. */
public final class Gem5CandidateId {
    private static final com.fasterxml.jackson.databind.ObjectMapper JSON_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();
    public record Input(
            Map<String, String> fields,
            Map<String, Integer> architecture,
            Map<String, String> environment
    ) {
        public Input {
            fields = Map.copyOf(fields);
            architecture = Map.copyOf(architecture);
            environment = Map.copyOf(environment);
        }
    }

    private Gem5CandidateId() {
    }

    public static String of(Input input) {
        Map<String, String> values = values(input);
        return of(values, Gem5InputFingerprint.hashes(
                values.get("gem5_binary"),
                values.get("config_script"),
                values.get("benchmark_binary"),
                values.get("run_cwd"),
                values.get("stdin")
        ));
    }

    static String of(Input input, String[] inputHashes) {
        return of(values(input), inputHashes);
    }

    private static Map<String, String> values(Input input) {
        Map<String, String> values = new LinkedHashMap<>(input.fields());
        for (String name : GeneratedGem5Contract.INTEGER_FINGERPRINT_FIELDS) {
            String value = values.get(name);
            if (value != null && value.matches("[+-]?[0-9]+")) {
                values.put(name, new BigInteger(value).toString());
            }
        }
        values.put("cache_format", GeneratedGem5Contract.CACHE_FORMAT);
        values.put("architecture", architectureJson(input.architecture()));
        try {
            values.put("environment", JSON_MAPPER.writeValueAsString(
                    new TreeMap<>(input.environment())));
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        return values;
    }

    private static String of(Map<String, String> values, String[] inputHashes) {
        StringBuilder fingerprint = new StringBuilder();
        for (String name : GeneratedGem5Contract.FINGERPRINT_FIELDS) {
            String field = values.get(name);
            if (field == null) {
                throw new IllegalArgumentException("Missing gem5 fingerprint field: " + name);
            }
            appendFramed(fingerprint, field);
        }
        if (inputHashes == null || inputHashes.length != 5) {
            throw new IllegalArgumentException("Expected five gem5 input hashes");
        }
        for (String field : inputHashes) {
            appendFramed(fingerprint, field);
        }

        String hash = sha256(fingerprint.toString()).substring(0, 24);
        String safeBenchmark = sanitize(values.get("benchmark"));
        if (safeBenchmark.isEmpty()) {
            safeBenchmark = "benchmark";
        }
        return sanitize(values.get("method"))
                + "_" + safeBenchmark
                + "_seed" + sanitize(values.get("seed"))
                + "_budget" + sanitize(values.get("budget_index"))
                + "_" + hash;
    }

    private static void appendFramed(StringBuilder fingerprint, String field) {
        fingerprint.append(field.getBytes(StandardCharsets.UTF_8).length).append(':').append(field);
    }

    static String architectureJson(Map<String, Integer> architecture) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : new TreeMap<>(architecture).entrySet()) {
            if (!first) {
                json.append(", ");
            }
            json.append('"').append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        return json.append('}').toString();
    }

    static String forDirect(Map<String, Integer> architecture, String benchmark) {
        String benchmarkName = benchmark == null ? "" : benchmark;
        String safeBenchmark = sanitize(benchmarkName);
        if (safeBenchmark.isEmpty()) {
            safeBenchmark = "benchmark";
        }
        String fingerprint = architectureJson(architecture) + "\n" + benchmarkName;
        return "direct_" + safeBenchmark + "_" + sha256(fingerprint).substring(0, 24);
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = text.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (sanitized.length() > 48) {
            return sanitized.substring(0, 48);
        }
        return sanitized;
    }
}
