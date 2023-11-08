package jmetal.component.catalogue.pso.globalbestselection.impl;

import java.util.Comparator;
import java.util.List;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.core.operator.selection.impl.NaryTournamentSelection;
import jmetal.core.solution.doublesolution.DoubleSolution;

public class NaryTournamentGlobalBestSelection implements GlobalBestSelection {

  private int tournamentSize;
  private Comparator<DoubleSolution> comparator;

  public NaryTournamentGlobalBestSelection(int tournamentSize, Comparator<DoubleSolution> comparator) {
    this.tournamentSize = tournamentSize;
    this.comparator = comparator ;
  }

  @Override
  public DoubleSolution select(List<DoubleSolution> globalBestList) {
    NaryTournamentSelection<DoubleSolution> tournament = new NaryTournamentSelection<>(
        tournamentSize, comparator);

    DoubleSolution result;
    if (globalBestList.size() < tournamentSize) {
      result = globalBestList.get(0);
    } else {
      result = tournament.execute(globalBestList);
    }

    return result;
  }
}
