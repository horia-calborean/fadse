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
package ro.ulbsibiu.fadse.extended.base.operator.crossover;

/**
 *
 * @author Horia Calborean
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.base.relation.RelationNode;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;
import jmetal.base.*;
import jmetal.base.variable.*;
import jmetal.base.*;
import jmetal.base.operator.crossover.Crossover;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.Configuration.*;

/**
 * This class allows to apply a Single Point crossover operator using two parent
 * solutions.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single variable.
 */
public class RelationalSinglePointCrossover extends Crossover {

    /**
     * BINARY_SOLUTION represents class jmetal.base.solutionType.RealSolutionType
     */
    private static Class BINARY_SOLUTION;
    /**
     * BINARY_REAL_SOLUTION represents class jmetal.base.solutionType.BinaryRealSolutionType
     */
    private static Class BINARY_REAL_SOLUTION;
    /**
     * INT_SOLUTION represents class jmetal.base.solutionType.IntSolutionType
     */
    private static Class INT_SOLUTION;

    /**
     * Constructor
     * Creates a new instance of the single point crossover operator
     */
    public RelationalSinglePointCrossover() {
        try {
            INT_SOLUTION = Class.forName("jmetal.base.solutionType.IntSolutionType");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // catch
    } // SinglePointCrossover

    /**
     * Constructor
     * Creates a new instance of the single point crossover operator
     */
    public RelationalSinglePointCrossover(Properties properties) {
        this();
    } // SinglePointCrossover

    /**
     * Perform the crossover operation.
     * @param probability Crossover probability
     * @param parent1 The first parent
     * @param parent2 The second parent
     * @return An array containing the two offsprings
     * @throws JMException
     */
    public Solution[] doCrossover(double probability,
            Solution parent1,
            Solution parent2,
            RelationTree relationTree, RelationTree relationTreeCopy) throws JMException {
        Solution[] offSpring = new Solution[2];
        offSpring[0] = new Solution(parent1);
        offSpring[1] = new Solution(parent2);
        try {
            if (PseudoRandom.randDouble() < probability) {
                // Integer representation
                int[] mask1 = relationTree.getActiveNodes(parent1);
                int[] mask2 = relationTreeCopy.getActiveNodes(parent2);
                int mask[] = new int[mask1.length];
                for (int i = 0; i < mask.length; i++) { //perform and AND between the masks to find the valid crossover points
                    mask[i] = mask1[i] * mask2[i];
                }
                boolean validCrossoverPoint = false;
                int crossoverPoint = -1;
                int tries = 0;
                do {
                    crossoverPoint = PseudoRandom.randInt(0, parent1.numberOfVariables() - 1);
                    if (mask[crossoverPoint] == 1) {
                        validCrossoverPoint = true;
                    }
                    tries++;
                } while ((!validCrossoverPoint) || tries < 10000);//avoid deadlocks in case of problems
                if (validCrossoverPoint) {
                    //fill the trees with the current variables
                    for (int i = 0; i < parent1.numberOfVariables(); i++) {
//                    System.out.println("Insert variable" + i + ": " + parent1.getDecisionVariables()[i].getName() + " =" + parent1.getDecisionVariables()[i].getValue());
                        relationTree.insertVariableInTree(i, parent1.getDecisionVariables()[i]);
                    }
                    for (int i = 0; i < parent2.numberOfVariables(); i++) {
                        relationTreeCopy.insertVariableInTree(i, parent2.getDecisionVariables()[i]);
                    }
                    //TODO crossover of the trees
                    //detect the parent node of the selected crossover point
                    //if it is a root node then it is null (the crossover will be performed after the selected node: eg if 2 is the selected point then crossover is performed betweeen root node 2 and the next root node)
//                if (crossoverPoint == 2) {
//                    System.out.println("Crossover point" + crossoverPoint);
//                    relationTree.printToScreen();
//                    relationTreeCopy.printToScreen();
//                    waitForEnter();
//                }
                    RelationNode childNode1 = relationTree.findNode(crossoverPoint);
                    RelationNode parentNode1 = childNode1.getParent();
                    RelationNode childNode2 = relationTreeCopy.findNode(crossoverPoint);
                    RelationNode parentNode2 = childNode2.getParent();
                    if (parentNode1 == null && parentNode2 == null) {
                        //interchange two roots values
//                    double temp = parentNode1.getVariable().getValue();
//                    parentNode1.getVariable().setValue(parentNode2.getVariable().getValue());
//                    parentNode1
                        relationTree.replaceParent(childNode1, childNode2);
                        relationTreeCopy.replaceParent(childNode2, childNode1);
                    } else {
                        childNode1.setParent(parentNode2);
                        childNode2.setParent(parentNode1);
                        parentNode2.replaceChildWith(crossoverPoint, childNode1);
                        parentNode1.replaceChildWith(crossoverPoint, childNode2);
                    }

//                if (crossoverPoint == 2) {
//                    System.out.println("Crossover point" + crossoverPoint);
//                    relationTree.printToScreen();
//                    relationTreeCopy.printToScreen();
//                    waitForEnter();
//                }
                    //serialize in two arrays the variables ordered from both trees
                    // get the values of the variables and set them to the individuals
                    double[] varsTree1 = relationTree.getAllVariablesSortedByPosition(parent1.numberOfVariables());
                    double[] varsTree2 = relationTreeCopy.getAllVariablesSortedByPosition(parent2.numberOfVariables());

                    for (int i = 0; i < parent1.numberOfVariables(); i++) {
                        offSpring[0].getDecisionVariables()[i].setValue(varsTree1[i]);
                    }
                    for (int i = 0; i < parent2.numberOfVariables(); i++) {
                        offSpring[1].getDecisionVariables()[i].setValue(varsTree2[i]);
                    }
                }
            }
        } catch (ClassCastException e1) {
            Configuration.logger_.severe("RelationalSinglePointCrossover.doCrossover: Cannot perfom "
                    + "RelationalSinglePointCrossover");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doCrossover()");
        }
        return offSpring;
    } // doCrossover

    /**
     * Executes the operation
     * @param object An object containing an array of two solutions
     * @return An object containing an array with the offSprings
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution[] parents = (Solution[]) object;

        if (((parents[0].getType().getClass() != INT_SOLUTION)
                || (parents[1].getType().getClass() != INT_SOLUTION))) {

            Configuration.logger_.severe("RelationalSinglePointCrossover.execute: the solutions "
                    + "are not of the right type. The type should be 'Int', but "
                    + parents[0].getType() + " and "
                    + parents[1].getType() + " are obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if

        Double probability = (Double) getParameter("probability");
        Environment environment = (Environment) getParameter("environment");
        if (parents.length < 2) {
            Configuration.logger_.severe("RelationalSinglePointCrossover.execute: operator "
                    + "needs two parents");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } else if (probability == null) {
            Configuration.logger_.severe("RelationalSinglePointCrossover.execute: probability "
                    + "not specified");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        Solution[] offSpring;
        offSpring = doCrossover(probability.doubleValue(),
                parents[0],
                parents[1],
                environment.getInputDocument().getRelationTree1(), environment.getInputDocument().getRelationTree2());

        //-> Update the offSpring solutions
        for (int i = 0; i < offSpring.length; i++) {
            offSpring[i].setCrowdingDistance(0.0);
            offSpring[i].setRank(0);
        }
        return offSpring;//*/
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
} // SinglePointCrossover

