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

import java.util.LinkedList;
import java.util.List;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.Validator;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.DoubleParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.PermutationParameter;
import ro.ulbsibiu.fadse.utils.Utils;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.base.solutionType.IntRealPermutationSolutionType;
import jmetal.base.solutionType.IntRealSolutionType;
import jmetal.base.solutionType.IntSolutionType;
import jmetal.base.solutionType.RealSolutionType;
import jMetal.util.JMException;

/**
 *
 * @author Horia Calborean <horia.calborean at ulbsibiu.ro>
 */
public abstract class SimulatorWrapper extends Problem {

    /** Class to hold state of application */
    protected Environment environment;
    protected Solution currentSolution;

    public SimulatorWrapper(Environment environment) throws ClassNotFoundException {
        // persistence.DerbyDB.createConnection(environment.getInputDocument());

        this.environment = environment;
        InputDocument input = environment.getInputDocument();
        this.problemName_ = input.getSimulatorName();
        this.numberOfConstraints_ = input.getRules().size();
        this.numberOfObjectives_ = input.getObjectives().values().size();
        this.numberOfVariables_ = input.getParameters().length;
        int numberOfIntParameters = 0;
        int numberOfFloatParameters = 0;
        int numberOfPermutationParameters = 0;
        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int var = 0; var < numberOfVariables_; var++) {
            lowerLimit_[var] = 0;
            upperLimit_[var] = 1.0;
            try {
                lowerLimit_[var] = environment.getInputDocument().getParameters()[var].getVariable().getLowerBound();
                upperLimit_[var] = environment.getInputDocument().getParameters()[var].getVariable().getUpperBound();
            } catch (JMException ex) {
               // Logger.getLogger(SimulatorWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }

        } //for
        for (Parameter p : input.getParameters()) {
            if (p instanceof DoubleParameter) {
                numberOfFloatParameters++;
            } else if (p instanceof PermutationParameter) {
                numberOfPermutationParameters++;
            } else{
                numberOfIntParameters++;
            }
        }
        if(numberOfPermutationParameters!=0){
            this.solutionType_ = new IntRealPermutationSolutionType(this,numberOfIntParameters,numberOfFloatParameters,numberOfPermutationParameters);
        } else if (numberOfIntParameters == 0) {
            this.solutionType_ = new RealSolutionType(this);
        } else if (numberOfFloatParameters == 0) {
            this.solutionType_ = new IntSolutionType(this);
        } else {
            this.solutionType_ = new IntRealSolutionType(this, numberOfIntParameters, numberOfFloatParameters);
        }
        // this.variableType_ - this value is set in the constructor of the IntSolutionType

    }

    /**
     * Method to evaluate an individual
     * @param solution Object to represent individual (variables only)
     * @throws JMException
     */
    @Override
    public void evaluate(Solution solution) throws JMException {
        // find names for the variables provided by solution-object
        currentSolution = solution;
        Validator validator = new Validator();


        LinkedList<String> benchmarks = environment.getInputDocument().getBenchmarks();
        /** for all variables... associate them with a parameter */
        Parameter[] params = Utils.getParameters(solution, environment);
        Individual ind = null;
        boolean[] feasible = new boolean[benchmarks.size()];//not used, but it will be in the future
        for (int i = 0; i < benchmarks.size(); i++) {
            String benchmark = benchmarks.get(i);
//            System.out.println("BENCHMARK: "+benchmark);
            // initialize individual with parameters
            ind = new Individual(environment, benchmark);
            ind.setParameters(params);
            // Validate individual to the rules
            boolean result = 0 == validator.validate(ind, environment.getInputDocument().getRules());

            //if validation passed
            if (result) {
                // Do the simulation!
                performSimulation(ind);
                feasible[i] = ind.isFeasible() ? true : false;
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
                    double value = solution.getObjective(j);
//                    System.out.println("Objective["+j+"] = "+o.getValue());
                    value = (o.getValue() + (i) * value) / (i + 1);//Moving Average
//                    System.out.println("Current Mean value for Objective["+j+"] = "+value);
                    solution.setObjective(j, value);
                    j++;
                }
            } else {
                ind.markAsInfeasibleAndSetBadValuesForObjectives("did not pass validation");
                for (int k = 0; k < solution.numberOfObjectives(); k++) {
                    solution.setObjective(k, Double.MAX_VALUE);
                }
                solution.setNumberOfViolatedConstraint(environment.getInputDocument().getRules().size());
                solution.setOverallConstraintViolation(Integer.MAX_VALUE);//TODO think of a value to put here
            }
        }
//        for (int i = 0; i < ind.getObjectives().size(); i++) {
//            System.out.println("Solution final obj value: " + solution.getObjective(i));
//        }
    }

    public InputDocument getInputDocument() {
        return this.environment.getInputDocument();
    }

    public abstract void performSimulation(Individual individual);

    public abstract void closeSimulation(Individual individual);

    @Override
    public void evaluateConstraints(Solution solution) throws JMException {
        super.evaluateConstraints(solution);
        Validator validator = new Validator();
        Individual ind = new Individual(environment, "");//benchmark is not important in this case
        Parameter[] params = environment.getInputDocument().getParameters();
        ind.setParameters(params);
        solution.setNumberOfViolatedConstraint(validator.validate(ind, environment.getInputDocument().getRules()));
        solution.setOverallConstraintViolation(validator.validate(ind, environment.getInputDocument().getRules()));//TODO think of an importance of a rule??
    }

    @Override
    public int getNumberOfConstraints() {
        return environment.getInputDocument().getRules().size();
    }
}
