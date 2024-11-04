/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.simulationIO;

import java.util.List;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special.VirtualParameter;
import ro.ulbsibiu.fadse.simulationIO.rule.Rule;

/**
 *
 * @author Horia
 */
public class Validator {

    public int validate(Individual ind, List<Rule> rules) {
        boolean result = true;
        int failedRules = 0;
        int paramsLength = ind.getParameters().length;
        int virtualParamsLength = 0;


        SimulatorParameter[] params = new SimulatorParameter[paramsLength];
        System.arraycopy(ind.getParameters(), 0, params, 0, paramsLength);
        if (ind.getEnvironment().getDesignSpaceDocument().getVirtualParameters() != null) {
            virtualParamsLength = ind.getEnvironment().getDesignSpaceDocument().getVirtualParameters().length;
            SimulatorParameter[] virtualParams = ind.getEnvironment().getDesignSpaceDocument().getVirtualParameters();
            for (SimulatorParameter p : virtualParams) {
                VirtualParameter e = (VirtualParameter) p;
                for (SimulatorParameter param : ind.getEnvironment().getDesignSpaceDocument().getParameters()) {
                    try {
                        e.addVariable(param.getName(), new Double((Integer) param.getValue()));
                    } catch (Exception ex) {
                    }
                }
            }
            SimulatorParameter[] paramsTemp = new SimulatorParameter[params.length + virtualParamsLength];
            System.arraycopy(params, 0, paramsTemp, 0, params.length);
            System.arraycopy(virtualParams, 0, paramsTemp, params.length, virtualParams.length);
            params = paramsTemp;

        }
        for (Rule r : rules) {
            if (!r.validate(params)) {
                failedRules++;
            }
        }
        return failedRules;
    }
}
