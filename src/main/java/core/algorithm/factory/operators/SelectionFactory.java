package core.algorithm.factory.operators;

import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;

public class SelectionFactory implements SelectionFactoryInterface{
    @Override
    public SelectionOperator<List<DoubleSolution>, DoubleSolution> create(String type) {
        switch (type.trim()) {
            case "BinaryTournament2":
                return new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
            // Add more selection operators as needed
            default:
                throw new IllegalArgumentException("Unsupported selection operator: " + type);
        }
    }
}
