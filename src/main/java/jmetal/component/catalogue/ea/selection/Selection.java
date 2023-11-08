package jmetal.component.catalogue.ea.selection;
import java.util.List;
import jmetal.core.solution.Solution;

@FunctionalInterface
public interface Selection<S extends Solution<?>> {
  List<S> select(List<S> solutionList) ;
}
