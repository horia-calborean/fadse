package core.algorithm.factory.operators.selection;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.algorithm.factory.operators.ComparatorFactory;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.*;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class SelectionFactory {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final SelectionParameters selectionParams;
    private final SelectionOperatorType type;

    public SelectionFactory(HashMap<String, Object> metaheuristicData){
        // Expect the map to contain a nested map under key "selection"
        Object selectionData = metaheuristicData.get("selection");

        if (!(selectionData instanceof Map)) {
            throw new IllegalArgumentException("Missing or invalid 'selection' configuration in the input.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> selectionMap = (Map<String, Object>) selectionData;

        this.selectionParams = mapper.convertValue(selectionMap, SelectionParameters.class);
        this.type = SelectionOperatorType.fromName(selectionParams.operator);
    }

    public SelectionOperator<?, ?> create() {
        return switch (type) {
            case BEST_SOLUTION, BINARY_TOURNAMENT, NARY_TOURNAMENT, SPATIAL_SPREAD_DEVIATION, RANDOM -> createSolutionOperator();
            case NARY_RANDOM, RANKING_AND_CROWDING, RANKING_AND_DIR_SCORE, RANKING_AND_PREFERENCE -> createListOperator();
            case DIFFERENTIAL_EVOLUTION -> createDifferentialEvolutionOperator();
        };
    }

    public <S extends Solution<?>> SelectionOperator<List<S>, S> createSolutionOperator() {
        Comparator<S> comparator = createComparator();

        return switch (type) {
            case BEST_SOLUTION -> new BestSolutionSelection<>(comparator);
            case BINARY_TOURNAMENT -> comparator != null
                    ? new BinaryTournamentSelection<>(comparator)
                    : new BinaryTournamentSelection<>();
            case NARY_TOURNAMENT -> (selectionParams.tournamentSize != null && comparator != null)
                    ? new NaryTournamentSelection<>(selectionParams.tournamentSize, comparator)
                    : new NaryTournamentSelection<>();
            case SPATIAL_SPREAD_DEVIATION -> comparator != null
                    ? new SpatialSpreadDeviationSelection<>(comparator, selectionParams.numberOfTournaments)
                    : new SpatialSpreadDeviationSelection<>(selectionParams.numberOfTournaments);
            case RANDOM -> new RandomSelection<>();
            default -> throw new IllegalArgumentException("Invalid solution-based operator: " + type);
        };
    }

    public <S extends Solution<?>> SelectionOperator<List<S>, List<S>> createListOperator() {
        Comparator<S> comparator = createComparator();

        return switch (type) {
            case NARY_RANDOM -> selectionParams.solutionsToSelect != null
                    ? new NaryRandomSelection<>(selectionParams.solutionsToSelect)
                    : new NaryRandomSelection<>();
            case RANKING_AND_CROWDING -> comparator != null
                    ? new RankingAndCrowdingSelection<>(selectionParams.solutionsToSelect, comparator)
                    : new RankingAndCrowdingSelection<>(selectionParams.solutionsToSelect);
            case RANKING_AND_DIR_SCORE -> {
                String vectors = require("referenceVectors", selectionParams.referenceVectors);
                yield new RankingAndDirScoreSelection<>(selectionParams.solutionsToSelect, comparator, parseReferenceVectors(vectors));
            }
            case RANKING_AND_PREFERENCE -> {
                String interest = require("interestPoints", selectionParams.interestPoints);
                Double epsilon = require("epsilon", selectionParams.epsilon);
                yield new RankingAndPreferenceSelection<>(selectionParams.solutionsToSelect, parseInterestPoints(interest), epsilon);
            }
            default -> throw new IllegalArgumentException("Invalid list-based operator: " + type);
        };
    }

    public <S extends Solution<?>> SelectionOperator<List<DoubleSolution>, List<DoubleSolution>> createDifferentialEvolutionOperator() {
        if (selectionParams.solutionsToSelect != null && selectionParams.selectCurrentSolution != null) {
            return new DifferentialEvolutionSelection(
                    selectionParams.solutionsToSelect,
                    selectionParams.selectCurrentSolution
            );
        }
        return new DifferentialEvolutionSelection();
    }

    //UTILS
    @SuppressWarnings("unchecked")
    private <S extends Solution<?>> Comparator<S> createComparator() {
        if (selectionParams.comparator != null) {
            return (Comparator<S>) ComparatorFactory.create(selectionParams.comparator);
        }
        return null;
    }

    private double[][] parseReferenceVectors(String vectorString) {
        String[] vectors = vectorString.split(";");
        double[][] result = new double[vectors.length][];
        for (int i = 0; i < vectors.length; i++) {
            String[] components = vectors[i].split(",");
            result[i] = new double[components.length];
            for (int j = 0; j < components.length; j++) {
                result[i][j] = Double.parseDouble(components[j]);
            }
        }
        return result;
    }
    private List<Double> parseInterestPoints(String input) {
        List<Double> points = new ArrayList<>();
        if (input != null && !input.isEmpty()) {
            String[] vectors = input.split(";");
            for (String vec : vectors) {
                String[] components = vec.split(",");
                for (String comp : components) {
                    points.add(Double.parseDouble(comp.trim()));
                }
            }
        }
        return points;
    }

//    private double[][] parseReferenceVectors(String vectorString) {
//        String[] vectors = vectorString.split(";");
//        double[][] result = new double[vectors.length][];
//
//        for (int i = 0; i < vectors.length; i++) {
//            String[] components = vectors[i].split(",");
//            result[i] = new double[components.length];
//            for (int j = 0; j < components.length; j++) {
//                result[i][j] = Double.parseDouble(components[j]);
//            }
//        }
//
//        return result;
//    }
//
//    private List<Double> parseInterestPoints(String input) {
//        List<Double> points = new ArrayList<>();
//        if (input != null && !input.isEmpty()) {
//            String[] vectors = input.split(";");
//            for (String vec : vectors) {
//                String[] components = vec.split(",");
//                for (String comp : components) {
//                    points.add(Double.parseDouble(comp.trim()));
//                }
//            }
//        }
//        return points;
//    }

    private <T> T require(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter '" + name + "' for mutation operator " + type);
        }
        return value;
    }
}
