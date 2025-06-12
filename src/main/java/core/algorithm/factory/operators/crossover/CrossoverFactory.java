package core.algorithm.factory.operators.crossover;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.*;
import org.uma.jmetal.operator.mutation.impl.*;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.grouping.impl.ListGrouping;
import org.uma.jmetal.util.grouping.impl.ListLinearGrouping;
import org.uma.jmetal.util.grouping.impl.ListOrderedGrouping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating crossover operators.
 *
 * Note:
 * - SBXCrossover is intended for DoubleSolution
 * - SinglePointCrossover is intended for BinarySolution
 *
 * The caller must ensure the correct combination of operator and solution type.
 */
@SuppressWarnings("unchecked")
public class CrossoverFactory {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final CrossoverParameters crossoverParams;
    private final CrossoverOperatorType type;

    public CrossoverFactory(Map<String, Object> metaheuristicData){
        // Expect the map to contain a nested map under key "mutation"
        Object crossoverData = metaheuristicData.get("crossover");

        if (!(crossoverData instanceof Map)) {
            throw new IllegalArgumentException("Missing or invalid 'mutation' configuration in the input.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> crossoverMap = (Map<String, Object>) crossoverData;

        this.crossoverParams = mapper.convertValue(crossoverMap, CrossoverParameters.class);
        this.type = CrossoverOperatorType.fromName(crossoverParams.operator);
    }

    //TODO: implement RepairDoubleSolution? -> BLXAlphaCrossover,
    //ALSO: RandomGenerators not implemented
    //TODO: default value, for example for DifferentialEvolutionCrossover setting cr but not f or variant
    public CrossoverOperator<?> create() {
        return switch (type) {
            case N_POINT, TWO_POINT -> createGenericSolutionOperator();
            case NULL_CROSSOVER -> createSolutionOperator();
            case BLX_ALPHA, DIFFERENTIAL_EVOLUTION, SBX, WHOLE_ARITHMETIC -> createDoubleSolutionOperator();
            case INTEGER_SBX -> createIntegerSolutionOperator();
            case HUX, SINGLE_POINT, UNIFORM -> createBinarySolutionOperator();
            case PMX -> createIntegerPermutationSolutionOperator();
            case COMPOSITE -> createCompositeCrossover();
            default -> throw new UnsupportedOperationException("Unsupported crossover type: " + type);
        };
    }
    public <T> CrossoverOperator<Solution<T>> createGenericSolutionOperator() {
        return switch (type) {
            case N_POINT -> (crossoverParams.probability != null)
                    ? new NPointCrossover<>(crossoverParams.probability, require("nrCrossovers", crossoverParams.nrCrossovers))
                    : new NPointCrossover<>(require("nrCrossovers", crossoverParams.nrCrossovers));
            case TWO_POINT -> new TwoPointCrossover<>(require("probability", crossoverParams.probability));
            default -> throw new IllegalArgumentException("Unsupported generic crossover type: " + type);
        };
    }

    public <S extends Solution<?>> CrossoverOperator<S> createSolutionOperator() {
        return new NullCrossover<>();
    }

    public CrossoverOperator<BinarySolution> createBinarySolutionOperator() {
        double probability = require("probability", crossoverParams.probability);

        return switch (type) {
            case HUX -> new HUXCrossover<>(probability);
            case SINGLE_POINT -> new SinglePointCrossover<>(probability);
            case UNIFORM -> new UniformCrossover<>(probability);
            default -> throw new IllegalArgumentException("Unsupported BinarySolution crossover type: " + type);
        };
    }

    public CrossoverOperator<IntegerSolution> createIntegerSolutionOperator() {
        return new IntegerSBXCrossover(
                require("probability", crossoverParams.probability),
                require("distributionIndex", crossoverParams.distributionIndex));
    }

    public CrossoverOperator<DoubleSolution> createDoubleSolutionOperator() {
        return switch (type) {
            case BLX_ALPHA -> (crossoverParams.alpha != null)
                    ? new BLXAlphaCrossover(require("probability", crossoverParams.probability) , crossoverParams.alpha)
                    : new BLXAlphaCrossover(require("probability", crossoverParams.probability));
            case DIFFERENTIAL_EVOLUTION -> (crossoverParams.cr != null && crossoverParams.f != null && crossoverParams.variant != null)
                    ? new DifferentialEvolutionCrossover(crossoverParams.cr, crossoverParams.f, crossoverParams.variant)
                    : new DifferentialEvolutionCrossover();
            case SBX -> new SBXCrossover(
                    require("probability", crossoverParams.probability),
                    require("distributionIndex", crossoverParams.distributionIndex));
            case WHOLE_ARITHMETIC -> new WholeArithmeticCrossover(require("probability", crossoverParams.probability));
            default -> throw new IllegalArgumentException("Unsupported double-solution crossover type: " + type);
        };
    }

    public CrossoverOperator<PermutationSolution<Integer>> createIntegerPermutationSolutionOperator() {
        return new PMXCrossover(require("probability", crossoverParams.probability));
    }

    public CrossoverOperator<CompositeSolution> createCompositeCrossover() {
        if (crossoverParams.crossoversList == null || crossoverParams.crossoversList.isEmpty()) {
            throw new IllegalArgumentException("CompositeCrossover requires a list of crossover definitions.");
        }

        List<CrossoverOperator<Solution<?>>> operatorList = new ArrayList<>();

        for (Map<String, Object> singleCrossoverMap : crossoverParams.crossoversList) {
            CrossoverFactory nestedFactory = new CrossoverFactory(singleCrossoverMap);
            operatorList.add((CrossoverOperator<Solution<?>>) nestedFactory.create());
        }

        return new CompositeCrossover(operatorList);
    }

    //--- UTILS ---
    private <T> T require(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter '" + name + "' for mutation operator " + type);
        }
        return value;
    }
}
