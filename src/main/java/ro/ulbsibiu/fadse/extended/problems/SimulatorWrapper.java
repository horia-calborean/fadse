/*
 *
 *
 * This file is part of the FADSE tool.
 *
 *  Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei Zorila
 *  Copyright (c) 2009-2010
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  The names of its contributors NOT may be used to endorse or promote products
 *  derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *  OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */
package ro.ulbsibiu.fadse.extended.problems;

import jmetal.util.JMException;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.ConstraintHandling;
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

/**
 * @author Horia Calborean <horia.calborean at ulbsibiu.ro>
 */
public abstract class SimulatorWrapper extends AbstractDoubleProblem {

    /**
     * Class to hold state of application
     */
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
            } catch (JMException ex) {
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