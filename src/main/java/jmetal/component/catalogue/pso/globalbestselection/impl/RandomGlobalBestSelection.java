package jmetal.component.catalogue.pso.globalbestselection.impl;

import java.util.List;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class RandomGlobalBestSelection implements GlobalBestSelection {

  @Override
  public DoubleSolution select(List<DoubleSolution> globalBestList) {
    int randomSolution = JMetalRandom.getInstance()
        .nextInt(0, globalBestList.size() - 1);
    return globalBestList.get(randomSolution);
  }
}
