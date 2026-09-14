package ro.ulbsibiu.fadse.extended.base.relation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.rule.Rule;
import org.uma.jmetal.solution.Solution;

public class RelationTree implements Serializable {
    private final List<RelationNode> roots;

    public RelationTree() {
        roots = new LinkedList<>();
    }

    public void buildTree(List<Rule> relations, Parameter[] parameter) {
    }

    public void addRootNode(int position) {
        roots.add(new RelationNode(position));
    }

    public void addNode(int childPosition, int parentPosition, double deactivationValue) {
        if (parentPosition < 0) {
            addRootNode(childPosition);
        } else {
            for (RelationNode currentRoot : roots) {
                RelationNode parent;
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
                        List<Double> deactivationValues = new LinkedList<>();
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
        List<RelationNode> children = new LinkedList<>();
        List<Child> subNodes = root.getChildren();
        if (subNodes != null) {
            for (Child c : subNodes) {
                children.add(c.getChild());
                children.addAll(findAllSubNodes(c.getChild()));
            }
        }
        return children;
    }

    public List<RelationNode> findAllActiveSubNodes(RelationNode root, List<?> vars) {
        List<RelationNode> children = new LinkedList<>();
        List<Child> subNodes = root.getChildren();
        if (subNodes != null) {
            for (Child c : subNodes) {
                boolean valid = true;
                for (double deactivate : c.getDeactivationValues()) {
                    double parent_value = (double) vars.get(c.getChild().getParent().getPosition());


                    if (deactivate == parent_value) {//we have found a value for which this should be deactivated
                        valid = false;
                        break;
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

    public <S extends Solution<?>> int[] getActiveNodes(S s) {
        List<?> vars = s.variables();

        int[] activeNodes = new int[vars.size()];
        for(int i = 0; i < vars.size(); i++) activeNodes[i] = 0;

        //todo sort tree??? is it necessary?
        for (RelationNode r : roots) {//mark all the root nodes as active : they need to be present - if they are invalid they invalidate only their children not themselves
            // System.out.println("Marking as active ROOT NODE: " + r.getPosition());
            activeNodes[r.getPosition()] = 1;
            List<RelationNode> activeChildren = findAllActiveSubNodes(r, vars);
            for (RelationNode ac : activeChildren) {
                // System.out.println("Marking as active CHILDREN: " + ac.getPosition());
                activeNodes[ac.getPosition()] = 1;
            }
        }
        return activeNodes;
    }

    public <S extends Solution<?>> int getNumberOfActiveNodes(S s) {
        List<?> vars = s.variables();
        int activeNodes = 0;

        //todo sort tree??? is it necessary?
        for (RelationNode r : roots) {//mark all the root nodes as active : they need to be present - if they are invalid they invalidate only their children not themselves
            activeNodes += 1;
            List<RelationNode> activeChildren = findAllActiveSubNodes(r, null);
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
            } else if (child.getChild().getChildren() != null && !child.getChild().getChildren().isEmpty()) {
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

    public void insertVariableInTree(int position, Object var) {
        RelationNode node = findNode(position);
        node.setVariable(var);
    }

    public double[] getAllVariablesSortedByPosition(int numberOfVariables) {
        double[] vars = new double[numberOfVariables];
        for (RelationNode root : roots) {
            vars[root.getPosition()] = (double) root.getVariable();
            List<RelationNode> allChildren = findAllSubNodes(root);
            for (RelationNode c : allChildren) {
                vars[c.getPosition()] = (double) c.getVariable();
            }
        }
        return vars;
    }

    

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (RelationNode r : this.roots) {
            output.append(r.toString()).append(" \n");
        }
        return "RelationTree{" + output + '}';
    }

    public void printToScreen() {
        RelationTreePrinter.print(roots);
    }

    public static void main(String[] args)  {
        // TODO - I have removed the code from this main method
    }
}