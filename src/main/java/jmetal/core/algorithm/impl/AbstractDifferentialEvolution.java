package jmetal.core.algorithm.impl;

import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Abstract class representing differential evolution (DE) algorithms
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
public abstract class AbstractDifferentialEvolution<Result> extends AbstractEvolutionaryAlgorithm<DoubleSolution, Result>
{
  public abstract DifferentialEvolutionCrossover getCrossoverOperator() ;
  public abstract DifferentialEvolutionSelection getSelectionOperator() ;
}
