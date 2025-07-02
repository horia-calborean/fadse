package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import core.algorithm.factory.operators.crossover.CrossoverFactory;
import core.algorithm.factory.operators.crossover.CrossoverOperatorType;
import core.algorithm.factory.operators.mutation.MutationFactory;
import core.algorithm.factory.operators.mutation.MutationOperatorType;
import core.algorithm.factory.operators.selection.SelectionFactory;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.python.icu.impl.Differ;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.binarysolution.BinarySolution;

import java.util.HashMap;
import java.util.List;
public class NSGAIIAlgorithmFactory<S extends Solution<?>> implements AlgorithmFactoryInterface<S, List<S>> {

    @Override
    public WrappedEvolutionaryAlgorithm<S, List<S>> create(InputData inputData, Problem<S>   problem) {

        HashMap<String, Object> metaheuristicData = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC_DATA);
        String cvsPath = (String) inputData.get(GapSetupParameters.OUTPUT_PATH);

        int populationSize = (int) metaheuristicData.get("populationSize");
        int maxEvaluations = (int) metaheuristicData.get("maxEvaluations");

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

        AbstractEvolutionaryAlgorithm<S, List<S>> nsga2 = new NSGAIIBuilder<>(
                problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(maxEvaluations)
                .build();

        return new WrappedEvolutionaryAlgorithm<>(nsga2, cvsPath);
    }
}
