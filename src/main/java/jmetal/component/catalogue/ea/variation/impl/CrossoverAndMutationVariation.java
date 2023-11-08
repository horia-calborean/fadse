package jmetal.component.catalogue.ea.variation.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.component.catalogue.ea.variation.Variation;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;

/**
 *
 * @param <S>
 */
public class CrossoverAndMutationVariation<S extends Solution<?>> implements Variation<S> {
  private CrossoverOperator<S> crossover ;
  private MutationOperator<S> mutation ;
  private int matingPoolSize ;
  private int offspringPopulationSize ;

  public CrossoverAndMutationVariation(
      int offspringPopulationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation) {
    this.crossover = crossover ;
    this.mutation = mutation ;
    this.offspringPopulationSize = offspringPopulationSize ;

    this.matingPoolSize = offspringPopulationSize *
        crossover.numberOfRequiredParents() / crossover.numberOfGeneratedChildren();

    int remainder = matingPoolSize % crossover.numberOfRequiredParents();
    if (remainder != 0) {
      matingPoolSize += remainder;
    }
  }

  @Override
  public List<S> variate(List<S> population, List<S> matingPopulation) {
    int numberOfParents = crossover.numberOfRequiredParents();

    checkNumberOfParents(matingPopulation, numberOfParents);

    List<S> offspringPopulation = new ArrayList<>(offspringPopulationSize);
    for (int i = 0; i < matingPoolSize; i += numberOfParents) {
      List<S> parents = new ArrayList<>(numberOfParents);
      for (int j = 0; j < numberOfParents; j++) {
        parents.add(matingPopulation.get(i + j));
      }

      List<S> offspring = crossover.execute(parents);

      for (S s : offspring) {
        mutation.execute(s);
        offspringPopulation.add(s);
        if (offspringPopulation.size() == offspringPopulationSize) {
          break;
        }
      }
    }

    Check.that(
            offspringPopulation.size() == offspringPopulationSize,
            "The size of the"
                    + "offspring population is not correct: "
                    + offspringPopulation.size()
                    + " instead of "
                    + offspringPopulationSize);

    return offspringPopulation;
  }

  /**
   * A crossover operator is applied to a number of parents, and it assumed that the population contains
   * a valid number of population. This method checks that.
   *
   * @param population
   * @param numberOfParentsForCrossover
   */
  private void checkNumberOfParents(List<S> population, int numberOfParentsForCrossover) {
    if ((population.size() % numberOfParentsForCrossover) != 0) {
      throw new JMetalException("Wrong number of parents: the remainder if the " +
          "population size (" + population.size() + ") is not divisible by " +
          numberOfParentsForCrossover);
    }
  }

  @Override
  public int getMatingPoolSize() {
    return matingPoolSize ;
  }

  @Override
  public int getOffspringPopulationSize() {
    return offspringPopulationSize ;
  }
}
