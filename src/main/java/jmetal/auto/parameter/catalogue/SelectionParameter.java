package jmetal.auto.parameter.catalogue;

import java.util.Comparator;
import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.ea.selection.Selection;
import jmetal.component.catalogue.ea.selection.impl.NaryTournamentSelection;
import jmetal.component.catalogue.ea.selection.impl.PopulationAndNeighborhoodSelection;
import jmetal.component.catalogue.ea.selection.impl.RandomSelection;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.sequencegenerator.SequenceGenerator;

public class SelectionParameter<S extends Solution<?>> extends CategoricalParameter {

  public SelectionParameter(List<String> selectionStrategies) {
    super("selection", selectionStrategies);
  }

  public Selection<S> getParameter(int matingPoolSize, Comparator<S> comparator) {
    Selection<S> result;
    switch (value()) {
      case "tournament":
        int tournamentSize =
            (Integer) findSpecificParameter("selectionTournamentSize").value();

        result = new NaryTournamentSelection<>(
            tournamentSize, matingPoolSize, comparator);

        break;
      case "random":
        result = new RandomSelection<>(matingPoolSize);
        break;
      case "populationAndNeighborhoodMatingPoolSelection":
        double neighborhoodSelectionProbability =
            (double) findSpecificParameter("neighborhoodSelectionProbability").value();
        var neighborhood = (Neighborhood<S>) getNonConfigurableParameter("neighborhood");
        Check.notNull(neighborhood);

        var subProblemIdGenerator = (SequenceGenerator<Integer>) getNonConfigurableParameter(
            "subProblemIdGenerator");
        Check.notNull(subProblemIdGenerator);

        result =
            new PopulationAndNeighborhoodSelection<>(
                matingPoolSize,
                subProblemIdGenerator,
                neighborhood,
                neighborhoodSelectionProbability,
                false);
        break;
      default:
        throw new JMetalException("Selection component unknown: " + value());
    }

    return result;
  }
}
