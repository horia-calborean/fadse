package core.application;

import core.network.ClientsRepository;
import core.problem.application.ProblemFactory;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.uma.jmetal.algorithm.Algorithm;
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

import java.util.List;

public class AlgorithmRunner {
    protected InputData inputData;

    public AlgorithmRunner(InputData inputData) {
        this.inputData = inputData;
    }

    public void run() {
        ClientsRepository clientsRepository;
        try {
            clientsRepository = ClientsRepository.getInstance(inputData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String problemName = (String) inputData.get(CommonSetupParameters.NAME);
        Problem<DoubleSolution> problem = ProblemFactory.createProblem(problemName, inputData);

        // TODO -> Read the following data from InputData & create a factory -> George
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        double mutationProbability = 1.0 / (double)problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
        int populationSize = 10;
        int noOfGenerations = 15;

        // TODO -> Instantiate algorithms using WrappedEvolutionaryAlgorithm wrappers -> George
        Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(populationSize * noOfGenerations)
                .build();

        algorithm.run();
        List<DoubleSolution> resultPopulation = algorithm.result();

        System.out.println("Algorithm has finished with a result population of count -> " + resultPopulation.size());
    }
}