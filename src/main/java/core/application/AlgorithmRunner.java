package core.application;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import core.algorithm.factory.AlgorithmFactory;
import core.network.ClientsRepository;
import core.problem.application.ProblemFactory;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlgorithmRunner {

    private static final Logger LOGGER = Logger.getLogger(AlgorithmRunner.class.getName());

    protected final InputData inputData;

    public AlgorithmRunner(InputData inputData) {
        this.inputData = inputData;
    }

    public void run() {
        LOGGER.log(Level.INFO, "=== Starting FADSE Distributed Optimization ===");

        // Validate network configuration early (fail fast)
        validateNetworkConfiguration();

        // Create problem
        String problemName = (String) inputData.get(CommonSetupParameters.NAME);
        LOGGER.log(Level.INFO, "Creating problem: " + problemName);
        Problem<IntegerSolution> problem = ProblemFactory.createProblem(problemName, inputData);

        // Create wrapped algorithm
        LOGGER.log(Level.INFO, "Creating algorithm");
        WrappedEvolutionaryAlgorithm<?, ?> wrappedAlgorithm =
                AlgorithmFactory.createAlgorithm(inputData, problem);

        try {
            // Execute algorithm
            LOGGER.log(Level.INFO, "Starting algorithm execution");
            wrappedAlgorithm.run();

            // Extract and validate results
            List<IntegerSolution> resultPopulation = extractResults(wrappedAlgorithm);

            LOGGER.log(Level.INFO, String.format(
                    "=== Optimization Complete: %d solutions found ===",
                    resultPopulation.size()
            ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Algorithm execution failed", e);
            throw new RuntimeException("Optimization failed: " + e.getMessage(), e);
        } finally {
            // Cleanup network resources
            cleanupNetworkResources();
        }
    }

    /**
     * Validates that network clients are properly configured.
     * Fails fast if no clients are available.
     *
     * @throws IllegalStateException if network cannot be initialized or no clients configured
     */
    private void validateNetworkConfiguration() {
        try {
            ClientsRepository clientsRepository = ClientsRepository.getInstance(inputData);

            int clientCount = clientsRepository.getNumberOfClients();

            if (clientCount == 0) {
                throw new IllegalStateException(
                        "No simulation clients configured. " +
                                "Please configure clients in clientsConfig.xml"
                );
            }

            LOGGER.log(Level.INFO, String.format(
                    "Network validated: %d client(s) available for distributed execution",
                    clientCount
            ));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize network", e);
            throw new IllegalStateException(
                    "Failed to initialize network clients: " + e.getMessage(), e
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during network initialization", e);
            throw new IllegalStateException(
                    "Network initialization failed: " + e.getMessage(), e
            );
        }
    }

    /**
     * Extracts and validates results from the algorithm.
     *
     * @param algorithm The wrapped algorithm to extract results from
     * @return Validated list of IntegerSolution results
     * @throws IllegalStateException if results are invalid or wrong type
     */
    private List<IntegerSolution> extractResults(WrappedEvolutionaryAlgorithm<?, ?> algorithm) {
        LOGGER.log(Level.FINE, "Extracting results from algorithm");

        Object result = algorithm.result();

        // Validate result is a List
        if (!(result instanceof List)) {
            throw new IllegalStateException(
                    "Algorithm returned unexpected result type: " +
                            result.getClass().getName() + " (expected List)"
            );
        }

        @SuppressWarnings("unchecked")
        List<?> rawList = (List<?>) result;

        // Handle empty results
        if (rawList.isEmpty()) {
            LOGGER.log(Level.WARNING, "Algorithm returned empty result population");
            return new ArrayList<>();
        }

        // Validate solution type
        Object firstSolution = rawList.get(0);
        if (!(firstSolution instanceof IntegerSolution)) {
            throw new IllegalStateException(
                    "Algorithm returned wrong solution type: " +
                            firstSolution.getClass().getName() + " (expected IntegerSolution)"
            );
        }

        @SuppressWarnings("unchecked")
        List<IntegerSolution> typedResult = (List<IntegerSolution>) rawList;

        LOGGER.log(Level.INFO, String.format(
                "Successfully extracted %d solutions", typedResult.size()
        ));

        return typedResult;
    }

    /**
     * Cleans up network resources after algorithm completion.
     * Best-effort cleanup - logs warnings but doesn't throw exceptions.
     */
    private void cleanupNetworkResources() {
        LOGGER.log(Level.FINE, "Cleaning up network resources");

        try {
            ClientsRepository clientsRepository = ClientsRepository.getInstance(inputData);

            // Ensure all pending simulations are complete
            // This is important to not leave hanging connections
            clientsRepository.join();

            LOGGER.log(Level.INFO, "Network resources cleaned up successfully");

        } catch (Exception e) {
            // Don't rethrow - cleanup is best-effort
            // Log warning so it's visible but doesn't fail the whole run
            LOGGER.log(Level.WARNING, "Failed to cleanup network resources: " + e.getMessage(), e);
        }
    }
}

