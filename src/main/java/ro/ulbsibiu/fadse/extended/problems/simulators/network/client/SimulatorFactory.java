package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.ProblemFactory;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimulatorFactory {
    private static SimulatorWrapper problem = null;
    public static SimulatorWrapper getSimulator(Environment env){
        try {
            problem = (SimulatorWrapper) ProblemFactory.<DoubleSolution>loadProblem(env);
        } catch (Exception ex) {
            Logger.getLogger(SimulatorFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return problem;
    }
}