package jmetal.util;

import jmetal.base.operator.mutation.BitFlipMutation;
import jmetal.base.operator.mutation.SwapMutation;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.RelationalBitFlipMutation;

import java.util.Properties;

public class ObjectiveNormalizerFactory {

    /**
     * Gets a crossover operator through its name.
     * @param name of the operator
     * @return the operator
     * @throws JMException
     */
    public static ObjectivesNormalizer getObjectiveNormalizer(String name, int maxValue) throws JMException {

        if (name.equalsIgnoreCase("GapObjectivesNormalizer")) {
            return new GapObjectivesNormalizer();
        }else if (name.equalsIgnoreCase("SyntheticObjectivesNormalizer")) {
            return new SyntheticObjectivesNormalizer(maxValue);
        } else {
            Configuration.logger_.severe("Operator '" + name + "' not found ");
            Class cls = java.lang.String.class;
            String name2 = cls.getName();
            throw new JMException("Exception in " + name2 + ".getMutationOperator()");
        }
    } // getMutationOperator
} // MutationFactory

