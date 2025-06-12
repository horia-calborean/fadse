package core.algorithm.factory.operators.selection;

public enum SelectionOperatorType {
    BEST_SOLUTION("BestSolution"),
    BINARY_TOURNAMENT("BinaryTournament"),
    NARY_TOURNAMENT("NaryTournament"),
    SPATIAL_SPREAD_DEVIATION("SpatialSpreadDeviation"),
    RANDOM("Random"),
    NARY_RANDOM("NaryRandom"),
    RANKING_AND_CROWDING("RankingAndCrowding"),
    RANKING_AND_DIR_SCORE("RankingAndDirScore"),
    RANKING_AND_PREFERENCE("RankingAndPreference"),
    DIFFERENTIAL_EVOLUTION("DifferentialEvolution");

    private final String name;

    SelectionOperatorType(String name) {
        this.name = name;
    }

    public static SelectionOperatorType fromName(String name) {
        for (SelectionOperatorType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown selection operator: " + name);
    }

}
