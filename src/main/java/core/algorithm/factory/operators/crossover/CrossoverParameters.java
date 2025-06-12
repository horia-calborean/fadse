package core.algorithm.factory.operators.crossover;

import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;

import java.util.List;
import java.util.Map;

public class CrossoverParameters {
    public String operator;
    public Double probability;
    public Double distributionIndex;
    public Double alpha;
    public Integer nrCrossovers; //used for NPointCrossover
    public Double cr; //Crossover rate (probability of gene inheritance)
    public Double f; //Scaling factor (amplitude of mutation differential)
    public DifferentialEvolutionCrossover.DE_VARIANT variant; //Strategy for how mutation and crossover are done

    // Only used for CompositeMutation
    public List<Map<String, Object>> crossoversList;

}
