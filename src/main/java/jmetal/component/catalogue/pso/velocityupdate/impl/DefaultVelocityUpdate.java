package jmetal.component.catalogue.pso.velocityupdate.impl;

import java.util.List;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.InertiaWeightComputingStrategy;
import jmetal.component.catalogue.pso.velocityupdate.VelocityUpdate;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Method implementing the standard velocity PSO update strategy
 *
 * @author Antonio J. Nebro
 */
public class DefaultVelocityUpdate implements VelocityUpdate {

  protected double c1Max;
  protected double c1Min;
  protected double c2Max;
  protected double c2Min;

  protected JMetalRandom randomGenerator;

  /**
   * Constructor
   *
   * @param c1Min:     Min value for c1.
   * @param c1Max:     Max value for c1.
   * @param c2Min:     Min value for c2.
   * @param c2Max:     Max value for c2.
   */
  public DefaultVelocityUpdate(double c1Min,
      double c1Max,
      double c2Min,
      double c2Max) {
    this.c1Max = c1Max;
    this.c1Min = c1Min;
    this.c2Max = c2Max;
    this.c2Min = c2Min;

    this.randomGenerator = JMetalRandom.getInstance();
  }

  @Override
  /**
   * Update the velocity of the particle. We assume that r1 and r2 have a random number between 0.0 and 1.0.
   * @param swarm: List of possible solutions.
   * @param speed: Matrix for particle speed.
   * @param localBest: List of local best particles.
   * @param leaders: List of global best particles.
   * @return Updated speed.
   */
  public double[][] update(List<DoubleSolution> swarm, double[][] speed, DoubleSolution[] localBest,
      BoundedArchive<DoubleSolution> leaders, GlobalBestSelection globalBestSelection,
      InertiaWeightComputingStrategy inertiaWeightComputingStrategy) {
    double r1, r2, c1, c2;
    DoubleSolution bestGlobal;

    for (int i = 0; i < swarm.size(); i++) {
      DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
      DoubleSolution bestParticle = (DoubleSolution) localBest[i].copy();

      bestGlobal = globalBestSelection.select(leaders.solutions()) ;

      r1 = randomGenerator.nextDouble(0, 1);
      r2 = randomGenerator.nextDouble(0, 1);
      c1 = randomGenerator.nextDouble(c1Min, c1Max);
      c2 = randomGenerator.nextDouble(c2Min, c2Max);

      double inertiaWeight = inertiaWeightComputingStrategy.compute() ;

      for (int j = 0; j < particle.variables().size(); j++) {
        speed[i][j] = inertiaWeight * speed[i][j]
            + c1 * r1 * (bestParticle.variables().get(j) - particle.variables().get(j))
            + c2 * r2 * (bestGlobal.variables().get(j) - particle.variables().get(j));
      }
    }

    return speed;
  }
}
