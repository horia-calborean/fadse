package core.network.client.gem5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Gem5Candidate {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final String candidateId;
    private final String method;
    private final String benchmark;
    private final String seed;
    private final Path outputDirectory;
    private final Path resultProperties;
    private final Path resultJson;
    private final Map<String, Integer> originalArchitecture;
    private final Map<String, Integer> architecture;
    private final List<Gem5CandidateRepair> repairs;
    private final Double costProxy;
    private final Map<String, Object> gem5;

    Gem5Candidate(
            String candidateId,
            String method,
            String benchmark,
            String seed,
            Path outputDirectory,
            Path resultProperties,
            Path resultJson,
            Map<String, Integer> originalArchitecture,
            Map<String, Integer> architecture,
            List<Gem5CandidateRepair> repairs,
            Double costProxy,
            Map<String, Object> gem5
    ) {
        this.candidateId = candidateId;
        this.method = method;
        this.benchmark = benchmark;
        this.seed = seed;
        this.outputDirectory = outputDirectory;
        this.resultProperties = resultProperties;
        this.resultJson = resultJson;
        this.originalArchitecture = originalArchitecture;
        this.architecture = architecture;
        this.repairs = repairs;
        this.costProxy = costProxy;
        this.gem5 = gem5;
    }

    public String getCandidateId() {
        return candidateId;
    }

    String getRunId() {
        return String.valueOf(gem5.getOrDefault("run_id", ""));
    }

    public String getBenchmark() {
        return benchmark;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getResultProperties() {
        return resultProperties;
    }

    public Path getResultJson() {
        return resultJson;
    }

    List<Gem5CandidateRepair> getRepairs() {
        return repairs;
    }

    public Map<String, Integer> getOriginalArchitecture() {
        return originalArchitecture;
    }

    public Map<String, Integer> getArchitecture() {
        return architecture;
    }

    void writeJson(Path path, List<String> objectives) {
        try {
            JSON_MAPPER.writeValue(path.toFile(), toJsonMap(objectives));
        } catch (IOException e) {
            throw new IllegalStateException("Could not write gem5 candidate JSON: " + path, e);
        }
    }

    private Map<String, Object> toJsonMap(List<String> objectives) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("contract_version", GeneratedGem5Contract.CONTRACT_VERSION);
        root.put("candidate_id", candidateId);
        root.put("run_id", gem5.getOrDefault("run_id", ""));
        root.put("budget_index", gem5.getOrDefault("budget_index", "0"));
        root.put("method", method);
        root.put("benchmark", benchmark);
        root.put("seed", Gem5Config.parseIntegerOrString(seed));
        root.put("original_architecture", originalArchitecture);
        root.put("architecture", architecture);
        if (objectives != null && !objectives.isEmpty()) {
            root.put("objectives", List.copyOf(objectives));
        }
        root.put("gem5", gem5);

        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("output_dir", outputDirectory.toString());
        outputs.put("result_properties", resultProperties.toString());
        outputs.put("result_json", resultJson.toString());
        root.put("outputs", outputs);

        if (costProxy != null) {
            root.put("cost_proxy", costProxy);
        }
        if (!repairs.isEmpty()) {
            root.put("repairs", repairsToJson());
        }
        return root;
    }

    private List<Map<String, Object>> repairsToJson() {
        List<Map<String, Object>> repairMaps = new ArrayList<>();
        for (Gem5CandidateRepair repair : repairs) {
            Map<String, Object> repairMap = new LinkedHashMap<>();
            repairMap.put("parameter", repair.getParameter());
            repairMap.put("original_value", repair.getOriginalValue());
            repairMap.put("repaired_value", repair.getRepairedValue());
            repairMap.put("reason", repair.getReason());
            repairMaps.add(repairMap);
        }
        return repairMaps;
    }

}
