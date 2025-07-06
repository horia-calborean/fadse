package core.application;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import core.algorithm.factory.AlgorithmFactory;
import core.network.ClientsRepository;
import core.problem.application.ProblemFactory;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

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
        Problem<IntegerSolution> problem = ProblemFactory.createProblem(problemName, inputData);


        WrappedEvolutionaryAlgorithm<?, ?> wrappedAlgorithm = AlgorithmFactory.createAlgorithm(inputData, problem);
        wrappedAlgorithm.run();
        List<IntegerSolution> resultPopulation = (List<IntegerSolution>) wrappedAlgorithm.result();
        System.out.println("Algorithm has finished with a result population of count -> " + resultPopulation.size());

    }

}