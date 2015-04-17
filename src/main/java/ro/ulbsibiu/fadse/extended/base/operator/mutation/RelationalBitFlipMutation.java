/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
 *   Copyright (c) 2009-2011
 *   All rights reserved.
 * 
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 * 
 *   The names of its contributors NOT may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *   OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package ro.ulbsibiu.fadse.extended.base.operator.mutation;

/**
 * RelationalBitFlipMutation.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.1
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;
import jmetal.base.Solution;
import jmetal.base.variable.*;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.base.operator.mutation.Mutation;

/**
 *
 * @author Horia Calborean
 */
/**
 * This class implements a bit flip mutation operator.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single variable.
 */
public class RelationalBitFlipMutation extends Mutation {

    /**
     * INT_SOLUTION represents class jmetal.base.solutionType.IntSolutionType
     */
    private static Class INT_SOLUTION;

    /**
     * Constructor
     * Creates a new instance of the Bit Flip mutation operator
     */
    public RelationalBitFlipMutation() {
        try {
            INT_SOLUTION = Class.forName("jmetal.base.solutionType.IntSolutionType");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // catch
    } // RelationalBitFlipMutation

    /**
     * Constructor
     * Creates a new instance of the Bit Flip mutation operator
     */
    public RelationalBitFlipMutation(Properties properties) {
        this();
    } // RelationalBitFlipMutation

    /**
     * Perform the mutation operation
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution, RelationTree relationTree) throws JMException {
        try {
            // Integer representation
            int[] mask = relationTree.getActiveNodes(solution);
            for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                if (mask[i] == 1) {
                    if (PseudoRandom.randDouble() < probability) {
                        int value = (int) (PseudoRandom.randInt(
                                (int) solution.getDecisionVariables()[i].getUpperBound(),
                                (int) solution.getDecisionVariables()[i].getLowerBound()));
                        solution.getDecisionVariables()[i].setValue(value);
                    } // if
                }
            }
        } catch (ClassCastException e1) {
            Configuration.logger_.severe("RelationalBitFlipMutation.doMutation: "
                    + "ClassCastException error" + e1.getMessage());
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
    } // doMutation

    /**
     * Executes the operation
     * @param object An object containing a solution to mutate
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if ((solution.getType().getClass() != INT_SOLUTION)) {
            Configuration.logger_.severe("RelationalBitFlipMutation.execute: the solution "
                    + "is not of the right type. The type should be "
                    + "'Int', but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if
        Environment environment  = (Environment) getParameter("environment");
        Double probability = (Double) getParameter("probability");
        if (probability == null) {
            Configuration.logger_.severe("RelationalBitFlipMutation.execute: probability not "
                    + "specified");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }
        doMutation(probability.doubleValue(), solution, environment.getInputDocument().getRelationTree1());
        return solution;
    } // execute
     public void waitForEnter() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String str = "";
            System.out.print("> prompt ");
            str = in.readLine();
        } catch (IOException e) {
        }
    }
} // RelationalBitFlipMutation

