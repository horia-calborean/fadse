package ro.ulbsibiu.fadse.extended.problems;

import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.ConstraintHandling;
import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.Validator;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SimulatorWrapper extends AbstractDoubleProblem {
    protected Environment environment;
    protected DoubleSolution currentSolution;

    public SimulatorWrapper(Environment environment) {
        this.environment = environment;
        InputDocument input = environment.getInputDocument();
        name = input.getSimulatorName();
        numberOfConstraints = input.getRules().size();
        numberOfObjectives = input.getObjectives().values().size();

        int numberOfVariables = input.getParameters().length;
        List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
        List<Double> upperLimit = new ArrayList<>(numberOfVariables);
        for (int var = 0; var < numberOfVariables; var++) {
            lowerLimit.set(var, 0.0);
            upperLimit.set(var, 1.0);
            try {
                lowerLimit.set(var, environment.getInputDocument().getParameters()[var].getLowerBound());
                upperLimit.set(var, environment.getInputDocument().getParameters()[var].getUpperBound());
            } catch (JMetalException ex) {
                Logger.getLogger(SimulatorWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        variableBounds(lowerLimit, upperLimit);
    }

    public DoubleSolution evaluate(DoubleSolution solution) {
        currentSolution = solution;
        Validator validator = new Validator();

        LinkedList<String> benchmarks = environment.getInputDocument().getBenchmarks();

        Parameter[] params = Utils.getParameters(solution, environment);

        Individual ind;

        //TODO - not used, but it will be, in the future
        boolean[] feasible = new boolean[benchmarks.size()];

        for (int i = 0; i < benchmarks.size(); i++) {
            String benchmark = benchmarks.get(i);

            // initialize individual with parameters
            ind = new Individual(environment, benchmark);
            ind.setParameters(params);
            // Validate individual to the rules
            boolean result = 0 == validator.validate(ind, environment.getInputDocument().getRules());

            //if validation passed
            if (result) {
                // Do the simulation!
                performSimulation(ind);
                feasible[i] = ind.isFeasible();
            } else {
                //System.err.println("Configuration did not pass validation.");
                feasible[i] = false;
                //need to set some false and bad values for the objectives???
            }
            if (result && ind.isFeasible()) {
                // Get the objectives from the individual and copy values for objectives to solution object
                List<Objective> objs = ind.getObjectives();
                int j = 0;
                for (Objective o : objs) {
                    double value = solution.objectives()[j];
//                    System.out.println("Objective["+j+"] = "+o.getValue());
                    value = (o.getValue() + (i) * value) / (i + 1);//Moving Average
//                    System.out.println("Current Mean value for Objective["+j+"] = "+value);
                    solution.objectives()[j] = value;
                    j++;
                }
            } else {
                ind.markAsInfeasibleAndSetBadValuesForObjectives("did not pass validation");
                for (int k = 0; k < numberOfObjectives(); k++) {
                    solution.objectives()[k] = Double.MAX_VALUE;
                }
                ConstraintHandling.numberOfViolatedConstraints(solution, environment.getInputDocument().getRules().size());
                ConstraintHandling.overallConstraintViolationDegree(solution, Integer.MAX_VALUE); //TODO think of a value to put here
            }
        }

        evaluateConstraints(solution);

        return solution;
    }

    public InputDocument getInputDocument() {
        return this.environment.getInputDocument();
    }

    public abstract void performSimulation(Individual individual);

    public abstract void closeSimulation(Individual individual);

    protected void evaluateConstraints(DoubleSolution solution) {
        Validator validator = new Validator();
        Individual ind = new Individual(environment, "");//benchmark is not important in this case
        Parameter[] params = environment.getInputDocument().getParameters();
        ind.setParameters(params);
        ConstraintHandling.numberOfViolatedConstraints(solution, validator.validate(ind, environment.getInputDocument().getRules()));
        ConstraintHandling.overallConstraintViolationDegree(solution, validator.validate(ind, environment.getInputDocument().getRules())); //TODO think of an importance of a rule??
    }
}