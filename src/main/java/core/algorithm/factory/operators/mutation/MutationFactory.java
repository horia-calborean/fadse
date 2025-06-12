package core.algorithm.factory.operators.mutation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.*;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.solution.sequencesolution.impl.CharSequenceSolution;
import org.uma.jmetal.util.grouping.impl.ListGrouping;
import org.uma.jmetal.util.grouping.impl.ListLinearGrouping;
import org.uma.jmetal.util.grouping.impl.ListOrderedGrouping;

import java.util.*;

/**
 * Factory for creating mutation operators.
 * Note: Caller must ensure the selected mutation is compatible with the solution type:
 * - PolynomialMutation: use with DoubleSolution
 * - BitFlipMutation: use with BinarySolution
 * etc
 */
@SuppressWarnings("unchecked")
public class MutationFactory {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final MutationParameters mutationParams;
    private final MutationOperatorType type;

    public MutationFactory(Map<String, Object> metaheuristicData){
        // Expect the map to contain a nested map under key "mutation"
        Object mutationData = metaheuristicData.get("mutation");

        if (!(mutationData instanceof Map)) {
            throw new IllegalArgumentException("Missing or invalid 'mutation' configuration in the input.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> mutationMap = (Map<String, Object>) mutationData;

        this.mutationParams = mapper.convertValue(mutationMap, MutationParameters.class);
        this.type = MutationOperatorType.fromName(mutationParams.operator);
    }

    //TODO: implement RepairDoubleSolution? -> CDGMutation, GroupedAndLinkedPolynomialMutation, IntegerPolynomialMutation,
    //TODO: NonUniformMutation, UniformMutation
    //ALSO: RandomGenerators not implemented
    public MutationOperator<?> create() {
        return switch (type) {
            case NULL_MUTATION -> createSolutionOperator();
            case BIT_FLIP -> createBinarySolutionOperator();
            case INTEGER_POLYNOMIAL -> createIntegerSolutionOperator();
            case  CDG, POLYNOMIAL, GROUPED_POLYNOMIAL, GROUPED_AND_LINKED_POLYNOMIAL,
                  LINKED_POLYNOMIAL, UNIFORM, NON_UNIFORM, SIMPLE_RANDOM -> createDoubleSolutionOperator();
            case COMPOSITE -> createCompositeMutation();
            case  CHAR_SEQUENCE_RANDOM -> createCharSequenceSolution();
            case PERMUTATION_SWAP -> createPermutationSwapMutation();
            default -> throw new IllegalArgumentException("Invalid solution-based operator: " + type);
        };
    }

    public <S extends Solution<?>> MutationOperator<S> createSolutionOperator() {
        return new NullMutation<>();
    }
    public MutationOperator<BinarySolution> createBinarySolutionOperator() {
        return new BitFlipMutation<>(require("probability", mutationParams.probability));
    }

    public MutationOperator<IntegerSolution> createIntegerSolutionOperator() {
        return (mutationParams.probability != null && mutationParams.distributionIndex != null)
        ?  new IntegerPolynomialMutation(mutationParams.probability, mutationParams.distributionIndex)
        :  new IntegerPolynomialMutation();
    }

    public MutationOperator<DoubleSolution> createDoubleSolutionOperator() {
        return switch (type) {
            case CDG -> (mutationParams.probability != null && mutationParams.delta != null)
                    ? new CDGMutation(mutationParams.probability, mutationParams.delta)
                    : new CDGMutation();
            case POLYNOMIAL -> (mutationParams.probability != null && mutationParams.distributionIndex != null)
                     ? new PolynomialMutation(mutationParams.probability, mutationParams.distributionIndex)
                     : new PolynomialMutation();
            case GROUPED_AND_LINKED_POLYNOMIAL -> {
                ListGrouping<Double> grouping = requireGrouping();
                yield (mutationParams.distributionIndex != null)
                        ? new GroupedPolynomialMutation(mutationParams.distributionIndex, grouping)
                        : new GroupedPolynomialMutation(grouping);
            }
            case GROUPED_POLYNOMIAL -> {
                ListGrouping<Double> grouping = requireGrouping();
                yield (mutationParams.distributionIndex != null)
                        ? new GroupedAndLinkedPolynomialMutation(mutationParams.distributionIndex, grouping)
                        : new GroupedAndLinkedPolynomialMutation(grouping);
            }
            case LINKED_POLYNOMIAL -> (mutationParams.probability != null && mutationParams.distributionIndex != null)
                     ? new LinkedPolynomialMutation(mutationParams.probability, mutationParams.distributionIndex)
                     : new LinkedPolynomialMutation();
            case UNIFORM -> new UniformMutation(
                    require("probability", mutationParams.probability),
                    require("perturbation", mutationParams.perturbation)
            );
            case NON_UNIFORM -> new NonUniformMutation(
                    require("probability", mutationParams.probability),
                    require("perturbation", mutationParams.perturbation),
                    require("maxIterations", mutationParams.maxIterations)
            );
            case SIMPLE_RANDOM -> new SimpleRandomMutation(require("probability", mutationParams.probability));
            default -> throw new IllegalArgumentException("Unsupported double-solution mutation type: " + type);
        };
    }

    public MutationOperator<CharSequenceSolution> createCharSequenceSolution() {
        return new CharSequenceRandomMutation(
                require("probability", mutationParams.probability),
                require("alphabet", mutationParams.alphabet).toCharArray()
        );
    }

    public MutationOperator<CompositeSolution> createCompositeMutation() {
        if (mutationParams.mutations == null || mutationParams.mutations.isEmpty()) {
            throw new IllegalArgumentException("CompositeMutation requires a list of mutation definitions.");
        }

        List<MutationOperator<Solution<?>>> operatorList = new ArrayList<>();

        for (Map<String, Object> singleMutationMap : mutationParams.mutations) {
            MutationFactory nestedFactory = new MutationFactory(singleMutationMap);
            operatorList.add((MutationOperator<Solution<?>>) nestedFactory.create());
        }

        return new CompositeMutation(operatorList);
    }

    public <T> MutationOperator<PermutationSolution<T>> createPermutationSwapMutation(){
        return new PermutationSwapMutation<>(require("probability", mutationParams.probability));
    }

    //--- UTILS ---
    private <T> T require(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter '" + name + "' for mutation operator " + type);
        }
        return value;
    }

    private ListGrouping<Double> requireGrouping() {
        if (mutationParams.grouping == null || mutationParams.grouping.type == null || mutationParams.grouping.numberOfGroups == null) {
            throw new IllegalArgumentException("'grouping' configuration is required for operator " + type);
        }

        return switch (mutationParams.grouping.type) {
            case "ListLinearGrouping" -> new ListLinearGrouping<>(mutationParams.grouping.numberOfGroups);
            case "ListOrderedGrouping" -> new ListOrderedGrouping<>(mutationParams.grouping.numberOfGroups);
            default -> throw new IllegalArgumentException("Unsupported grouping type: " + mutationParams.grouping.type);
        };
    }
}
