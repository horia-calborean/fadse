package core.algorithm.factory.operators;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import core.algorithm.factory.AlgorithmFactoryInterface;
import core.algorithm.factory.operators.crossover.CrossoverFactory;
import core.algorithm.factory.operators.mutation.MutationFactory;
import core.algorithm.factory.operators.selection.SelectionFactory;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.rnsgaii.RNSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.HashMap;
import java.util.List;

public class RNSGAIIAlgorithmFactory<S extends Solution<?>> implements AlgorithmFactoryInterface<S, List<S>> {

    @Override
    public WrappedEvolutionaryAlgorithm<S, List<S>> create(InputData inputData, Problem<S>   problem) {

        HashMap<String, Object> metaheuristicData = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC_DATA);
        String cvsPath = (String) inputData.get(GapSetupParameters.OUTPUT_PATH);

        int populationSize = (int) metaheuristicData.get("populationSize");
        int maxEvaluations = (int) metaheuristicData.get("maxEvaluations");
        double epsilon = (double) metaheuristicData.get("epsilon");
        List<Double> interestPoints = (List<Double>) metaheuristicData.get("interestPoints");

        int matingPoolSize = metaheuristicData.containsKey("matingPoolSize") ?
                (int) metaheuristicData.get("matingPoolSize") : populationSize;
        int offspringPopulationSize = metaheuristicData.containsKey("offspringPopulationSize") ?
                (int) metaheuristicData.get("offspringPopulationSize") : populationSize;

        // FACTORIES
        SelectionFactory selectionFactory = new SelectionFactory(metaheuristicData);
        MutationFactory mutationFactory = new MutationFactory(metaheuristicData);
        CrossoverFactory crossoverFactory = new CrossoverFactory(metaheuristicData);

        SelectionOperator<?, ?> selectionOperator = selectionFactory.create();
        MutationOperator<?> mutationOperator = mutationFactory.create();
        CrossoverOperator<?> crossoverOperator = crossoverFactory.create();

        @SuppressWarnings("unchecked")
        SelectionOperator<List<S>, S> selection =
                (SelectionOperator<List<S>, S>) selectionOperator;

        @SuppressWarnings("unchecked")
        MutationOperator<S> mutation = (MutationOperator<S>) mutationOperator;

        @SuppressWarnings("unchecked")
        CrossoverOperator<S> crossover = (CrossoverOperator<S>) crossoverOperator;

        AbstractEvolutionaryAlgorithm<S, List<S>> rnsga2 = new RNSGAIIBuilder<>(
                problem, crossover, mutation, interestPoints, epsilon)
                .setSelectionOperator(selection)
                .setMaxEvaluations(maxEvaluations)
                .setPopulationSize(populationSize)
                .setMatingPoolSize(matingPoolSize)
                .setOffspringPopulationSize(offspringPopulationSize)
                .build();
        return new WrappedEvolutionaryAlgorithm<>(rnsga2, cvsPath);
    }
}
