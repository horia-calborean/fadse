package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.utils.MathEvaluator;

public class VirtualParameter extends SimulatorParameter {
    protected String expression;
    protected MathEvaluator evaluator;

    public VirtualParameter(String name, String expression) {
        super(name);
        this.expression = expression;
        evaluator = new MathEvaluator(expression);
    }

    @Override
    public VirtualParameter clone() {
        VirtualParameter clone = new VirtualParameter(name, expression);

        clone.setDescription(description);

        return clone;
    }

    public void addVariable(String name, Double value) {
        evaluator.addVariable(name, value);
    }
}