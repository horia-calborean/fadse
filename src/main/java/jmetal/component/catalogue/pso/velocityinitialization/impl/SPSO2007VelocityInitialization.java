package jmetal.component.catalogue.pso.velocityinitialization.impl;

import java.util.List;
import jmetal.component.catalogue.pso.velocityinitialization.VelocityInitialization;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.PseudoRandomGenerator;

/**
 * Class that initializes the velocity of the particles according to the standard PSO 2007 (SPSO 2007)
 * Source: Maurice Clerc. Standard Particle Swarm Optimisation. 15 pages. 2012. <hal-00764996>
 *
 * @author Antonio J. Nebro
 */
public class SPSO2007VelocityInitialization implements VelocityInitialization {
  @Override
  /**
   * Initialize the velocity of the particles.
   * @param swarm: List of possible solutions.
   * @return A matrix with the initial velocity of the particles
   */
  public double[][] initialize(List<DoubleSolution> swarm) {
    Check.notNull(swarm);
    Check.that(!swarm.isEmpty(), "The swarm size is empty: " + swarm.size());

    int numberOfVariables = swarm.get(0).variables().size() ;
    double[][] speed = new double[swarm.size()][numberOfVariables] ;
    PseudoRandomGenerator randomGenerator = JMetalRandom.getInstance().getRandomGenerator();

    for (int i = 0 ; i < speed.length; i++) {
      DoubleSolution particle = swarm.get(i) ;
      for (int j = 0; j < numberOfVariables; j++) {
        Bounds<Double> bounds = particle.getBounds(j) ;
        speed[i][j] = (randomGenerator.nextDouble(bounds.getLowerBound(), bounds.getUpperBound())
            - particle.variables().get(j)) / 2.0;
      }
    }

    return speed;
  }
}
