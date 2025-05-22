package core.algorithm.factory.operators;

import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.List;

public interface SelectionFactoryInterface {
    SelectionOperator<List<DoubleSolution>, DoubleSolution> create(String type);
}
