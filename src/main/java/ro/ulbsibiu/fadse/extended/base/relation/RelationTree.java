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
package ro.ulbsibiu.fadse.extended.base.relation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.rule.Rule;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.base.variable.Int;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

/**
 *
 * @author Horia Calborean
 */
public class RelationTree implements Serializable {

    private List<RelationNode> roots;

    public RelationTree() {

        roots = new LinkedList<RelationNode>();
    }

    public void buildTree(List<Rule> relations, Parameter[] parameter) {
//        for(Rule r: relations){
//            IfRule rule = (IfRule) r;
//            Rule ifExpr =rule.getIfExpresion();
//            if(ifExpr instanceof AndRule || ifExpr instanceof IfRule){
//                throw new UnsupportedOperationException("Relation: IF can not contain an AND or an IF");
//            } else {
//
//            }
//        }
    }

    public void addRootNode(int position) {
        roots.add(new RelationNode(position));
    }

    public void addNode(int childPosition, int parentPosition, double deactivationValue) {
        if (parentPosition < 0) {
            addRootNode(childPosition);
        } else {
            for (RelationNode currentRoot : roots) {
                RelationNode parent = null;
                if (currentRoot.getPosition() == parentPosition) {
                    parent = currentRoot;
                } else {
                    parent = findParent(parentPosition, currentRoot);
                }
                if (parent != null) {
                    //test if this parent already has a child with the same position
                    boolean foundSameChild = false;
                    for (Child c : parent.getChildren()) {
                        if (c.getChild().getPosition() == childPosition) {
                            foundSameChild = true;
                            c.getDeactivationValues().add(deactivationValue);
                            break;
                        }
                    }
                    if (!foundSameChild) {
                        RelationNode child = new RelationNode(childPosition);
                        child.setParent(parent);
                        List<Double> deactivationValues = new LinkedList<Double>();
                        deactivationValues.add(deactivationValue);
                        parent.addChild(deactivationValues, child);
                    }
                }
            }
        }
    }

    public RelationNode findParent(int parentPosition, RelationNode currentRoot) {
        List<Child> children = currentRoot.getChildren();
        RelationNode parent = null;
        if (children == null) {
            //not found on this branch
        } else {

            for (Child c : children) {
                if (c.getChild().getPosition() == parentPosition) {
                    parent = c.getChild();
                } else {
                    parent = findParent(parentPosition, c.getChild());
                }
            }
        }
        return parent;
    }

    public List<RelationNode> findAllSubNodes(RelationNode root) {
        List<RelationNode> children = new LinkedList<RelationNode>();
        List<Child> subNodes = root.getChildren();
        if (subNodes != null) {
            for (Child c : subNodes) {
                children.add(c.getChild());
                children.addAll(findAllSubNodes(c.getChild()));
            }
        }
        return children;
    }

    public List<RelationNode> findAllActiveSubNodes(RelationNode root, Variable[] vars) throws JMException {
        List<RelationNode> children = new LinkedList<RelationNode>();
        List<Child> subNodes = root.getChildren();
        if (subNodes != null) {
            for (Child c : subNodes) {
                boolean valid = true;
                for (double deact : c.getDeactivationValues()) {
                    double parent_value = vars[c.getChild().getParent().getPosition()].getValue();

                    // System.out.println("deact:" + deact);
                    // System.out.println("child value: " + parent_value);
                    
                    if (deact == parent_value) {//we have found a value for which this should be deactivated
                        valid = false;
                    }
                }
                if (valid) {
                    children.add(c.getChild());
                    //only if this node is active it makes sense to move forward
                    children.addAll(findAllActiveSubNodes(c.getChild(), vars));
                }
            }
        }
        return children;
    }

    public int[] getActiveNodes(Solution s) throws JMException {
        Variable[] vars = s.getDecisionVariables();
        int[] activeNodes = new int[vars.length];
        for(int i = 0; i < vars.length; i++) activeNodes[i] = 0;

        //todo sort tree??? is it necessary?
        for (RelationNode r : roots) {//mark all the root nodes as active : they need to be present - if they are invalid they incvalidate only their children not themseves
            // System.out.println("Marking as active ROOT NODE: " + r.getPosition());
            activeNodes[r.getPosition()] = 1;
            List<RelationNode> activeChildren = findAllActiveSubNodes(r, vars);
            for (RelationNode ac : activeChildren) {
                // System.out.println("Marking as active ACTIVE CHILDREN: " + ac.getPosition());
                activeNodes[ac.getPosition()] = 1;
            }
        }
        return activeNodes;
    }

    public int getNumberOfActiveNodes(Solution s) throws JMException {
        Variable[] vars = s.getDecisionVariables();
        int activeNodes = 0;

        //todo sort tree??? is it necessary?
        for (RelationNode r : roots) {//mark all the root nodes as active : they need to be present - if they are invalid they incvalidate only their children not themseves
            activeNodes += 1;
            List<RelationNode> activeChildren = findAllActiveSubNodes(r, vars);
            activeNodes += activeChildren.size();
        }
        return activeNodes;
    }

    private RelationNode findChildNode(RelationNode root, int position) {
        RelationNode node = null;
        for (Child child : root.getChildren()) {
            if (child.getChild().getPosition() == position) {
                node = child.getChild();
//                System.out.println("FOUND");
                break;
            } else if (child.getChild().getChildren() != null && child.getChild().getChildren().size() > 0) {
                node = findChildNode(child.getChild(), position);
            }
            if (node != null) {
                break;
            }
        }
        return node;
    }

    public RelationNode findNode(int position) {
        RelationNode node = null;
        for (RelationNode root : roots) {
            if (root.getPosition() == position) {
                node = root;
                break;
            } else {
                node = findChildNode(root, position);
            }
            if (node != null) {
                break;
            }
        }
        return node;
    }

    public void replaceParent(RelationNode originalParent, RelationNode replacement) {
        roots.set(roots.indexOf(originalParent), replacement);
    }

    public void insertVariableInTree(int position, Variable var) {
        RelationNode node = findNode(position);
        node.setVariable(var);
    }

    public double[] getAllVariablesSortedByPosition(int numberOfVariables) throws JMException {
        double[] vars = new double[numberOfVariables];
        for (RelationNode root : roots) {
            vars[root.getPosition()] = root.getVariable().getValue();
            List<RelationNode> allChildren = findAllSubNodes(root);
            for (RelationNode c : allChildren) {
                vars[c.getPosition()] = c.getVariable().getValue();
            }
        }
        return vars;
    }

    

    @Override
    public String toString() {
        String output = "";
        for (RelationNode r : this.roots) {
            output += (r.toString() + " \n");
        }
        return "RelationTree{" + output + '}';
    }

    public void printToScreen() {
        RelationTreePrinter.print(roots);
    }

    public static void main(String[] args) throws JMException, ClassNotFoundException {
        //some objects the algorithm will give me
        RelationTree relationTree = new RelationTree();
        Variable[] variables = new Variable[10];
        Object[] problemParams = {"Real"};
        Problem problem = (new ProblemFactory()).getProblem("ZDT1", problemParams);
        Solution s = new Solution(problem, variables);
        for (int i = 0; i < 10; i++) {
            Variable v = new Int(0, 0, 12);
            variables[i] = v;
        }
        //end

        relationTree.addRootNode(0);
        relationTree.addRootNode(1);
        relationTree.addRootNode(2);
        relationTree.addRootNode(7);
        relationTree.addRootNode(9);
        relationTree.addNode(3, 1, 1);
        relationTree.addNode(4, 1, 1);
        relationTree.addNode(4, 1, 133);
        relationTree.addNode(5, 4, 0);
        relationTree.addNode(6, 4, 1);
        relationTree.addNode(8, 7, 0);
        for (RelationNode r : relationTree.roots) {
            System.out.println(r.toString());
        }
        int[] active = relationTree.getActiveNodes(s);

        int active2 = relationTree.getNumberOfActiveNodes(s);
        for (int i = 0; i < active.length; i++) {
            System.out.println((i + 1) + ":" + active[i] + " " + active2);
        }
        relationTree.printToScreen();

    }
}
