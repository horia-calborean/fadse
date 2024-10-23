package ro.ulbsibiu.fadse;

import org.ini4j.Wini;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.ProblemFactory;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.client.FadseClient;
import simulation.ListOfSimulationParameters;
import simulation.SimulationParameter;
import wrappers.WrappedEvolutionaryAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FadseClientBoot {

    public static void main(String[] args) {
        // TESTING

        String problemName = "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1";

        Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability,
                crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
                mutationDistributionIndex);

        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());

        org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII<DoubleSolution> algorithm =
                new NSGAIIIBuilder<>(problem)
                        .setCrossoverOperator(crossover)
                        .setMutationOperator(mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(300)
                        .setNumberOfDivisions(12)
                        .build();

        ListOfSimulationParameters simulationParameters = new ListOfSimulationParameters();
        simulationParameters.add(SimulationParameter.OUTPUT_EVERY_POPULATION_PATH, "//test//test");

        WrappedEvolutionaryAlgorithm wrappedAlgorithm = new WrappedEvolutionaryAlgorithm<>(algorithm, simulationParameters);
        wrappedAlgorithm.run();
        // -------

        try {
            FadseClient client;

            if (args.length < 2) {
                client = new FadseClient();
            } else {
                client = new FadseClient(Integer.parseInt(args[1]));
            }

            Thread fadseClientThread = new Thread(client);
            fadseClientThread.setDaemon(true);
            fadseClientThread.start();

            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exception) {
                    Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, null, exception);
                }

                if (!client.isSimulating()) {
                    long millisecondsSinceNoMessage = System.currentTimeMillis() - client.getConnectionStartTime();

                    File currentDirectory = new File(System.getProperty("user.dir"));

                    String fileSeparator = FileSystems.getDefault().getSeparator();
                    Wini iniFile = new Wini(new File(currentDirectory + fileSeparator + "configs" + fileSeparator + "fadseConfig.ini"));

                    int minutesToWait = iniFile.get("Watchdog", "minutes", int.class);
                    long millisecondsToWait = 60000L * minutesToWait;

                    if (millisecondsSinceNoMessage > millisecondsToWait) {
                        fadseClientThread.interrupt();
                        Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, "Watchdog had to stop this client and restart it");
                        System.exit(1);
                    }
                }
            }
        } catch (IOException exception) {
            Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, "FADSE ini config file could not be read", exception);
        }
    }
}