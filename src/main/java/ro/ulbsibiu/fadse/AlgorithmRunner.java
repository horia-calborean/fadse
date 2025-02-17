package ro.ulbsibiu.fadse;

import org.uma.jmetal.problem.Problem;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.ProblemFactory;

public class AlgorithmRunner {
    public void run(Environment environment) {
        // TODO - When passed as parameter, problem shall be cast (Problem<DoubleSolution>) problem
        Problem<?> problem = ProblemFactory.loadProblem(environment);

        System.out.println("Problem name = " + problem.name());
        System.out.println("Problem numberOfVariables = " + problem.numberOfVariables());
        System.out.println("Problem numberOfObjectives = " + problem.numberOfObjectives());

        // TODO - The rest of logic which includes the creation of the algorithm shall be added (see abstract-XmlInputReader-andrei)

    }
}