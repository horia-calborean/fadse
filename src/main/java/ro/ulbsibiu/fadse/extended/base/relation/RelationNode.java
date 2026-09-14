package ro.ulbsibiu.fadse.extended.base.relation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RelationNode implements Serializable{

    private RelationNode parent;
    private final List<Child> children;
    private int position;
    private Object variable;

    public RelationNode(int position) {
        parent = null;
        this.children = new LinkedList<>();
        this.position = position;
    }

    public void addChild(List<Double> deactivationValues, RelationNode child) {
        this.children.add(new Child(deactivationValues, child));
    }

    public void replaceChildWith(int position, RelationNode node) {
        for (Child c : children) {
            if (c.getChild().getPosition() == position) {
                c.setChild(node);//TODO test this
            }
        }

    }

    public RelationNode getParent() {
        return parent;
    }

    public void setParent(RelationNode parent) {
        this.parent = parent;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<Child> getChildren() {
        return children;
    }

    public Object getVariable() {
        return variable;
    }

    public void setVariable(Object variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        StringBuilder childrenS = new StringBuilder();
        for (Child c : children) {
            childrenS.append(c.getChild().toString()).append(" ");
        }
        return "{" + "pos=" + position + "chi=" + children + '}';
    }
}
