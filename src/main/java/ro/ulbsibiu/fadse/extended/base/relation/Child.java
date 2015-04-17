package ro.ulbsibiu.fadse.extended.base.relation;

import java.io.Serializable;
import java.util.List;

public class Child implements Serializable{

    private List<Double> deactivationValues;
    private RelationNode child;

    public Child(List<Double> deactivationValues, RelationNode child) {
        this.deactivationValues = deactivationValues;
        this.child = child;
    }

    public List<Double> getDeactivationValues() {
        return deactivationValues;
    }

    public void setDeactivationValues(List<Double> deactivationValues) {
        this.deactivationValues = deactivationValues;
    }

    public RelationNode getChild() {
        return child;
    }

    public void setChild(RelationNode child) {
        this.child = child;
    }

    @Override
    public String toString() {
        return "{" + "child=" + child + "act=" + deactivationValues  + '}';
    }


}
