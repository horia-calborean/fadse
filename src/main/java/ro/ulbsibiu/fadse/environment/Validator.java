/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.environment;

import java.util.List;

import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.environment.rule.Rule;

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


        Parameter[] params = new Parameter[paramsLength];
        System.arraycopy(ind.getParameters(), 0, params, 0, paramsLength);
        if (ind.getEnvironment().getInputDocument().getVirtualParameters() != null) {
            virtualParamsLength = ind.getEnvironment().getInputDocument().getVirtualParameters().length;
            Parameter[] virtualParams = ind.getEnvironment().getInputDocument().getVirtualParameters();
            for (Parameter p : virtualParams) {
                VirtualParameter e = (VirtualParameter) p;
                for (Parameter param : ind.getEnvironment().getInputDocument().getParameters()) {
                    try {
                        e.addVariable(param.getName(), new Double((Integer) param.getValue()));
                    } catch (Exception ex) {
                    }
                }
            }
            Parameter[] paramsTemp = new Parameter[params.length + virtualParamsLength];
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
