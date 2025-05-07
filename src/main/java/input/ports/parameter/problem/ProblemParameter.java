package input.ports.parameter.problem;

import java.io.Serializable;

public abstract class ProblemParameter<T> implements Serializable {
    protected String name;
    protected String description;
    protected T lowerBound;
    protected T upperBound;

    protected T value;

    public ProblemParameter(T lowerBound, T upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public abstract void setValueFromDouble(double value);

    public abstract ProblemParameter<T> clone();
}