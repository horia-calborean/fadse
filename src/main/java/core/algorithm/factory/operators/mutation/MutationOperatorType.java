package core.algorithm.factory.operators.mutation;

public enum MutationOperatorType {
        BIT_FLIP("BitFlipMutation"),
        CDG("CDGMutation"),
        CHAR_SEQUENCE_RANDOM("CharSequenceRandomMutation"),
        COMPOSITE("CompositeMutation"),
        GROUPED_AND_LINKED_POLYNOMIAL("GroupedAndLinkedPolynomialMutation"),
        GROUPED_POLYNOMIAL("GroupedPolynomialMutation"),
        INTEGER_POLYNOMIAL("IntegerPolynomialMutation"),
        LINKED_POLYNOMIAL("LinkedPolynomialMutation"),
        NON_UNIFORM("NonUniformMutation"),
        NULL_MUTATION("NullMutation"),
        PERMUTATION_SWAP("PermutationSwapMutation"),
        POLYNOMIAL("PolynomialMutation"),
        SIMPLE_RANDOM("SimpleRandomMutation"),
        UNIFORM("UniformMutation");

        private final String name;

        MutationOperatorType(String name) {
            this.name = name;
        }

        public static core.algorithm.factory.operators.mutation.MutationOperatorType fromName(String name) {
            for (core.algorithm.factory.operators.mutation.MutationOperatorType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown mutation operator: " + name);
        }

}
