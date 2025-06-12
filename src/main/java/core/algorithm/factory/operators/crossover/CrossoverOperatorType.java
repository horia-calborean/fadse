package core.algorithm.factory.operators.crossover;

public enum CrossoverOperatorType {
    BLX_ALPHA("BLXAlphaCrossover"),
    COMPOSITE("CompositeCrossover"),
    DIFFERENTIAL_EVOLUTION("DifferentialEvolutionCrossover"),
    HUX("HUXCrossover"),
    INTEGER_SBX("IntegerSBXCrossover"),
    N_POINT("NPointCrossover"),
    NULL_CROSSOVER("NullCrossover"),
    PMX("PMXCrossover"),
    SBX("SBXCrossover"),
    SINGLE_POINT("SinglePointCrossover"),
    TWO_POINT("TwoPointCrossover"),
    UNIFORM("UniformCrossover"),
    WHOLE_ARITHMETIC("WholeArithmeticCrossover");

    private final String name;

    CrossoverOperatorType(String name) {
        this.name = name;
    }

    public static core.algorithm.factory.operators.crossover.CrossoverOperatorType fromName(String name) {
        for (core.algorithm.factory.operators.crossover.CrossoverOperatorType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown mutation operator: " + name);
    }
}
