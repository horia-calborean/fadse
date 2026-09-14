package ro.ulbsibiu.fadse;

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
import ro.ulbsibiu.fadse.algorithm.LegacyWrappedEvolutionaryAlgorithm;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.ProblemFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlgorithmRunner {
    public void run(Environment environment) {
        Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(environment);

        System.out.println("Problem name = " + problem.name());
        System.out.println("Problem numberOfVariables = " + problem.numberOfVariables());
        System.out.println("Problem numberOfObjectives = " + problem.numberOfObjectives());

        String algorithmName = environment.getInputDocument().getMetaheuristicName();
        if (!"NSGAII".equalsIgnoreCase(algorithmName)) {
            // TODO - only NSGA-II is wired to the org.uma.jmetal 6.6 builders so far (matching the
            // level of completeness of core.application.AlgorithmRunner); other configured
            // metaheuristics (SPEA2, SMPSO, OMOPSO, ...) still need their own builder wiring.
            throw new UnsupportedOperationException(
                    "Metaheuristic '" + algorithmName + "' is not yet wired to the org.uma.jmetal 6.6 path");
        }

        Properties properties = loadMetaheuristicProperties(environment);

        double crossoverProbability = Double.parseDouble(properties.getProperty("crossoverProbability_", "0.9"));
        double crossoverDistributionIndex = Double.parseDouble(properties.getProperty("crossoverDistributionIndex_", "20.0"));
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = Double.parseDouble(
                properties.getProperty("mutationProbability_", String.valueOf(1.0 / problem.numberOfVariables())));
        double mutationDistributionIndex = Double.parseDouble(properties.getProperty("mutationDistributionIndex_", "20.0"));
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        int populationSize = Integer.parseInt(properties.getProperty("populationSize_", "100"));
        int maxEvaluations = Integer.parseInt(properties.getProperty("maxEvaluations_", "25000"));

        AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> algorithm =
                new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(maxEvaluations)
                        .build();

        LegacyWrappedEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> wrappedEvolutionaryAlgorithm =
                new LegacyWrappedEvolutionaryAlgorithm<>(algorithm, problem);
        wrappedEvolutionaryAlgorithm.run();

        List<DoubleSolution> resultPopulation = wrappedEvolutionaryAlgorithm.result();
        System.out.println("Algorithm has finished with a result population of count -> " + resultPopulation.size());
    }

    private Properties loadMetaheuristicProperties(Environment environment) {
        Properties properties = new Properties();
        String path = environment.getInputDocument().getMetaheuristicConfigPath();
        try (FileInputStream in = new FileInputStream(System.getProperty("user.dir") + path)) {
            properties.load(in);
        } catch (IOException e) {
            Logger.getLogger(AlgorithmRunner.class.getName()).log(Level.WARNING,
                    "Could not load metaheuristic properties file [" + path + "], using default values", e);
        }
        return properties;
    }
}