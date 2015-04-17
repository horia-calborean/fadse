/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.environment.rule;

import java.util.List;

import ro.ulbsibiu.fadse.environment.parameters.Parameter;

/**
 *
 * @author Horia
 */
public class AndRule implements Rule{

    String type;
    String description;
    List<Rule> rules;

    public AndRule(String type, String description, List<Rule> rules) {
        this.type = type;
        this.description = description;
        this.rules = rules;
    }
    

     public boolean validate(Parameter[] parameters){
        boolean result = true;
        for(Rule r: rules){
            result = result && r.validate(parameters);
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "\nAndRule: "+type+" "+description+" rules: {";
        for(Rule r: rules){
            result = result +r.toString();
        }
        result += "}";
        return result;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
    
}
