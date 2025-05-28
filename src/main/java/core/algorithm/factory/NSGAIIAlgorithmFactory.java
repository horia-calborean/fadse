package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import core.algorithm.factory.operators.CrossoverFactory;
import core.algorithm.factory.operators.MutationFactory;
import core.algorithm.factory.operators.SelectionFactory;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.HashMap;
import java.util.List;
public class NSGAIIAlgorithmFactory<S extends Solution<?>> implements AlgorithmFactoryInterface<S, List<S>> {

    @Override
    public WrappedEvolutionaryAlgorithm<S, List<S>> create(InputData inputData, Problem<S>   problem) {

        HashMap<String, Object> metaheuristicData = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC_DATA);

        int populationSize = Integer.parseInt( (String) metaheuristicData.get("populationSize_") );
        int maxEvaluations = Integer.parseInt( (String) metaheuristicData.get("maxEvaluations_") );

        double crossoverProbability = Double.parseDouble( (String) metaheuristicData.get("crossoverProbability_") );
        double crossoverDistributionIndex =  Double.parseDouble( (String) metaheuristicData.get("crossoverDistributionIndex_") );

        double mutationProbability =  Double.parseDouble( (String) metaheuristicData.get("mutationProbability_"));
        double mutationDistributionIndex =  Double.parseDouble( (String) metaheuristicData.get("mutationDistributionIndex_"));

        String selectionOperator = (String) metaheuristicData.get("selectionOperator_");
        String mutationoperator = (String) metaheuristicData.get("mutationOperator_");
        String crossoverOperator = (String) metaheuristicData.get("crossoverOperator_");

        // FACTORIES
        SelectionFactory selectionFactory = new SelectionFactory();
        MutationFactory mutationFactory = new MutationFactory();
        CrossoverFactory crossoverFactory = new CrossoverFactory();

        //TODO: Factory that returns crossover/mutation operator depending on the Solution type
        SelectionOperator<List<S>, S> selection =
                selectionFactory.create(selectionOperator);
        MutationOperator<S> mutation = mutationFactory.create(
                mutationoperator, mutationProbability, mutationDistributionIndex);
        CrossoverOperator<S> crossover = crossoverFactory.create(
                crossoverOperator, crossoverProbability, crossoverDistributionIndex);


        AbstractEvolutionaryAlgorithm<S, List<S>> nsga2 = new NSGAIIBuilder<>(
                problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(maxEvaluations)
                .build();

        return new WrappedEvolutionaryAlgorithm<>(nsga2);
    }
}
