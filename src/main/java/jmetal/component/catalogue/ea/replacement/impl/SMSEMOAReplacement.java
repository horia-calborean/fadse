package jmetal.component.catalogue.ea.replacement.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.core.solution.Solution;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.util.ranking.Ranking;

public class SMSEMOAReplacement<S extends Solution<?>>
    implements Replacement<S> {
  private Ranking<S> ranking;
  private Hypervolume<S> hypervolume ;

  public SMSEMOAReplacement(Ranking<S> ranking) {
    this(ranking,  new PISAHypervolume<>()) ;
  }

  public SMSEMOAReplacement(
          Ranking<S> ranking, Hypervolume<S> hypervolume) {
    this.ranking = ranking;
    this.hypervolume = hypervolume;
  }

  public List<S> replace(List<S> solutionList, List<S> offspringList) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(solutionList);
    jointPopulation.addAll(offspringList);

    ranking.compute(jointPopulation);

    List<S> lastSubFront = ranking.getSubFront(ranking.getNumberOfSubFronts()-1) ;

    lastSubFront = hypervolume.computeHypervolumeContribution(lastSubFront, jointPopulation) ;

    List<S> resultPopulation = new ArrayList<>() ;
    for (int i = 0; i < ranking.getNumberOfSubFronts()-1; i++) {
      resultPopulation.addAll(ranking.getSubFront(i));
    }

    for (int i = 0; i < lastSubFront.size()-1; i++) {
      resultPopulation.add(lastSubFront.get(i)) ;
    }

    return resultPopulation ;
  }
}
