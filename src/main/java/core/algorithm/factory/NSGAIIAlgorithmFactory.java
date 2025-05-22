package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.List;
public class NSGAIIAlgorithmFactory implements AlgorithmFactory {

    @Override
    public WrappedEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> create(InputData inputData, Problem problem) {

        HashMap<String, Object> metaheuristic = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC);
        int populationSize = (int) metaheuristic.get("populationSize");
        int generations = (int) metaheuristic.get("generations");

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        double mutationProbability = 1.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;

        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> nsga2 = new NSGAIIBuilder<>(
                problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(populationSize * generations)
                .build();

        return new WrappedEvolutionaryAlgorithm<>(nsga2);
    }
}
