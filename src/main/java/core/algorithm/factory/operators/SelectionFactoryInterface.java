package core.algorithm.factory.operators;

import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.List;

public interface SelectionFactoryInterface {
    <S extends Solution<?>> SelectionOperator<List<S>, S> create(String type);
}
