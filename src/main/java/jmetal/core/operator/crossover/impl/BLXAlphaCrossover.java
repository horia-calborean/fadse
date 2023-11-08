package jmetal.core.operator.crossover.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.RepairDoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.impl.RepairDoubleSolutionWithBoundValue;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;

/**
 * This class allows to apply a BLX-alpha crossover operator to two parent solutions.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class BLXAlphaCrossover implements CrossoverOperator<DoubleSolution> {
  private static final double DEFAULT_ALPHA = 0.5;

  private double crossoverProbability;
  private double alpha ;
  private RepairDoubleSolution solutionRepair ;
  private RandomGenerator<Double> randomGenerator ;

  /** Constructor */
  public BLXAlphaCrossover(double crossoverProbability) {
    this (crossoverProbability, DEFAULT_ALPHA, new RepairDoubleSolutionWithBoundValue()) ;
  }

  /** Constructor */
  public BLXAlphaCrossover(double crossoverProbability, double alpha) {
    this (crossoverProbability, alpha, new RepairDoubleSolutionWithBoundValue()) ;
  }

  /** Constructor */
  public BLXAlphaCrossover(double crossoverProbability, double alpha, RepairDoubleSolution solutionRepair) {
	  this(crossoverProbability, alpha, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
  }

  /** Constructor */
  public BLXAlphaCrossover(double crossoverProbability, double alpha, RepairDoubleSolution solutionRepair, RandomGenerator<Double> randomGenerator) {
    Check.probabilityIsValid(crossoverProbability);
    Check.that(alpha >= 0, "Alpha is negative: " + alpha);

    this.crossoverProbability = crossoverProbability ;
    this.alpha = alpha ;
    this.randomGenerator = randomGenerator ;
    this.solutionRepair = solutionRepair ;
  }

  /* Getters */
  @Override
  public double crossoverProbability() {
    return crossoverProbability;
  }

  public double alpha() {
    return alpha;
  }

  /* Setters */
  public void crossoverProbability(double crossoverProbability) {
    this.crossoverProbability = crossoverProbability;
  }

  public void alpha(double alpha) {
    this.alpha = alpha;
  }

  /** Execute() method */
  @Override
  public List<DoubleSolution> execute(List<DoubleSolution> solutions) {
    Check.notNull(solutions);
    Check.that(solutions.size() == 2, "There must be two parents instead of " + solutions.size());

    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1)) ;
  }

  /** doCrossover method */
  public List<DoubleSolution> doCrossover(
      double probability, DoubleSolution parent1, DoubleSolution parent2) {
    List<DoubleSolution> offspring = new ArrayList<DoubleSolution>(2);

    offspring.add((DoubleSolution) parent1.copy()) ;
    offspring.add((DoubleSolution) parent2.copy()) ;

    int i;
    double random;
    double valueY1;
    double valueY2;
    double valueX1;
    double valueX2;
    double upperBound;
    double lowerBound;

    if (randomGenerator.getRandomValue() <= probability) {
      for (i = 0; i < parent1.variables().size(); i++) {
        Bounds<Double> bounds = parent1.getBounds(i);
        upperBound = bounds.getUpperBound();
        lowerBound = bounds.getLowerBound();
        valueX1 = parent1.variables().get(i);
        valueX2 = parent2.variables().get(i);

        double max;
        double min;
        double range;

        if (valueX2 > valueX1) {
          max = valueX2;
          min = valueX1;
        } else {
          max = valueX1;
          min = valueX2;
        }

        range = max - min;

        double minRange;
        double maxRange;

        minRange = min - range * alpha;
        maxRange = max + range * alpha;

        random = randomGenerator.getRandomValue();
        valueY1 = minRange + random * (maxRange - minRange);

        random = randomGenerator.getRandomValue();
        valueY2 = minRange + random * (maxRange - minRange);

        valueY1 = solutionRepair.repairSolutionVariableValue(valueY1, lowerBound, upperBound) ;
        valueY2 = solutionRepair.repairSolutionVariableValue(valueY2, lowerBound, upperBound) ;

        offspring.get(0).variables().set(i, valueY1);
        offspring.get(1).variables().set(i, valueY2);
      }
    }

    return offspring;
  }

  public int numberOfRequiredParents() {
    return 2 ;
  }

  public int numberOfGeneratedChildren() {
    return 2 ;
  }
}

