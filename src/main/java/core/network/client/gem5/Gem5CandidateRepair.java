package core.network.client.gem5;

public final class Gem5CandidateRepair {
    private final String parameter;
    private final int originalValue;
    private final int repairedValue;
    private final String reason;

    public Gem5CandidateRepair(String parameter, int originalValue, int repairedValue, String reason) {
        this.parameter = parameter;
        this.originalValue = originalValue;
        this.repairedValue = repairedValue;
        this.reason = reason;
    }

    public String getParameter() {
        return parameter;
    }

    public int getOriginalValue() {
        return originalValue;
    }

    public int getRepairedValue() {
        return repairedValue;
    }

    public String getReason() {
        return reason;
    }
}
