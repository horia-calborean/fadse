package jmetal.algorithm.multiobjective.mosa.cooling.impl;

import jmetal.algorithm.multiobjective.mosa.cooling.CoolingScheme;

/**
 * Logarithmic cooling scheme used by {@link SimulatedAnnealing} algorithms
 */
public class Logarithmic implements CoolingScheme {

  @Override
  public double updateTemperature(double temperature, int iteration) {
    return Math.log(iteration) / Math.log(iteration + 1D ) * temperature ;
  }
}
