/**
 * CrossoverFactory.java
 *
 * @author Juanjo Durillo
 * @version 1.1
 */
package jmetal.base.operator.mutation;

import java.util.Properties;

import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.RelationalBitFlipMutation;
import jmetal.base.operator.mutation.Mutation;
import jmetal.util.PropUtils;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * Class implementing a mutation factory.
 */
public class MutationFactory {

    /**
     * Gets a crossover operator through its name.
     * @param name of the operator
     * @return the operator
     * @throws JMException
     */
    public static Mutation getMutationOperator(String name) throws JMException {

        if (name.equalsIgnoreCase("PolynomialMutation")) {
            return new PolynomialMutation(20);
        } else if (name.equalsIgnoreCase("BitFlipMutation")) {
            return new BitFlipMutation();
        } else if (name.equalsIgnoreCase("BitFlipMutationFuzzy")) {
            return new BitFlipMutationFuzzy();
        } else if (name.equalsIgnoreCase("SwapMutation")) {
            return new SwapMutation();
        } else if (name.equalsIgnoreCase("RelationalBitFlipMutation")) {
            return new RelationalBitFlipMutation();
        } else if (name.equalsIgnoreCase("BitFlipMutationFuzzyVirtualParameters")) {
            return new BitFlipMutationFuzzyVirtualParameters();
        } else if (name.equalsIgnoreCase("BitFlipMutationRandomDefuzzifier")) {
            return new BitFlipMutationRandomDefuzzifier();
        } else {
            Configuration.logger_.severe("Operator '" + name + "' not found ");
            Class cls = java.lang.String.class;
            String name2 = cls.getName();
            throw new JMException("Exception in " + name2 + ".getMutationOperator()");
        }
    } // getMutationOperator

    public static Mutation getMutationOperator(String name, Properties properties) throws JMException {

        if (name.equalsIgnoreCase("PolynomialMutation")) {
            return new PolynomialMutation(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("BitFlipMutation")) {
            return new BitFlipMutation(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("BitFlipMutationFuzzy")) {
            return new BitFlipMutationFuzzy(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("SwapMutation")) {
            return new SwapMutation(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("RelationalBitFlipMutation")) {
            return new RelationalBitFlipMutation(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("BitFlipMutationFuzzyVirtualParameters")) {
            return new BitFlipMutationFuzzyVirtualParameters(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else if (name.equalsIgnoreCase("BitFlipMutationRandomDefuzzifier")) {
            return new BitFlipMutationRandomDefuzzifier(PropUtils.getPropertiesWithPrefix(properties, name + "."));
        } else {
            Configuration.logger_.severe("Operator '" + name + "' not found ");
            Class cls = java.lang.String.class;
            String name2 = cls.getName();
            throw new JMException("Exception in " + name2 + ".getMutationOperator()");
        }
    } // getMutationOperator
} // MutationFactory

