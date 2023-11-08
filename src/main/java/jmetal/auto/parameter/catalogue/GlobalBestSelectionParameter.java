package jmetal.auto.parameter.catalogue;

import java.util.Comparator;
import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.component.catalogue.pso.globalbestselection.impl.BinaryTournamentGlobalBestSelection;
import jmetal.component.catalogue.pso.globalbestselection.impl.NaryTournamentGlobalBestSelection;
import jmetal.component.catalogue.pso.globalbestselection.impl.RandomGlobalBestSelection;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

public class GlobalBestSelectionParameter extends CategoricalParameter {
  public GlobalBestSelectionParameter(List<String> selectionStrategies) {
    super("globalBestSelection", selectionStrategies) ;
  }

  public GlobalBestSelection getParameter(Comparator<DoubleSolution> comparator) {
    GlobalBestSelection result ;
    switch(value()) {
      case "tournament":
        int tournamentSize =
            (Integer) findSpecificParameter("selectionTournamentSize").value();

        result = new NaryTournamentGlobalBestSelection(tournamentSize, comparator) ;
        break ;
      case "random":
        result = new RandomGlobalBestSelection();
        break ;
      default:
        throw new JMetalException("Global Best Selection component unknown: " + value()) ;
    }

    return result ;
  }
}
