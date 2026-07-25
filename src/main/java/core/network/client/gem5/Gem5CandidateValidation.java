package core.network.client.gem5;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Gem5CandidateValidation {
    private final Map<String, Integer> architecture;
    private final List<Gem5CandidateRepair> repairs;
    private final Double costProxy;

    public Gem5CandidateValidation(
            Map<String, Integer> architecture,
            List<Gem5CandidateRepair> repairs,
            Double costProxy
    ) {
        this.architecture = Collections.unmodifiableMap(architecture);
        this.repairs = Collections.unmodifiableList(repairs);
        this.costProxy = costProxy;
    }

    public Map<String, Integer> getArchitecture() {
        return architecture;
    }

    public List<Gem5CandidateRepair> getRepairs() {
        return repairs;
    }

    public Double getCostProxy() {
        return costProxy;
    }
}
