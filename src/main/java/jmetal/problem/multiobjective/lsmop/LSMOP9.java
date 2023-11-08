package jmetal.problem.multiobjective.lsmop;

import java.util.ArrayList;
import java.util.List;
import jmetal.problem.multiobjective.lsmop.functions.Ackley;
import jmetal.problem.multiobjective.lsmop.functions.Function;
import jmetal.problem.multiobjective.lsmop.functions.Sphere;
import jmetal.core.util.errorchecking.JMetalException;

public class LSMOP9 extends AbstractLSMOP {

  /**
   * Creates a default LSMOP9 problem (7 variables and 3 objectives)
   */
  public LSMOP9() {
    this(5, 300, 3);
  }

  /**
   * Creates a LSMOP6 problem instance
   *
   * @param nk                 Number of subcomponents in each variable group
   * @param numberOfVariables  Number of variables
   * @param numberOfObjectives Number of objective functions
   */


  public LSMOP9(int nk, int numberOfVariables, int numberOfObjectives) throws JMetalException {
    super(nk, numberOfVariables, numberOfObjectives);
    name("LSMOP9");
  }


  @Override
  protected Function getOddFunction() {
    return new Sphere();
  }

  @Override
  protected Function getEvenFunction() {
    return new Ackley();
  }

  @Override
  protected List<Double> evaluate(List<Double> variables) {
    double[] G = new double[numberOfObjectives()];

    for (int i = numberOfObjectives(); i <= numberOfVariables(); i++) {
      double aux = (1.0 + Math.cos((double) i / (double) numberOfVariables() * Math.PI / 2.0))
          * variables.get(i - 1);
      aux = aux - variables.get(0) * 10;
      variables.set(i - 1, aux);
    }

    for (int i = 0; i < numberOfObjectives(); i++) {
      G[i] = 0.0;
    }

    for (int i = 1; i <= numberOfObjectives(); i += 2) {
      for (int j = 1; j <= this.nk; j++) {

        List<Double> x = new ArrayList<>(numberOfVariables());
        for (int k = len.get(i - 1) + numberOfObjectives() - 1 + (j - 1) * subLen.get(i - 1) + 1;
            k <= len.get(i - 1) + numberOfObjectives() - 1 + j * subLen.get(i - 1);
            k++) {
          x.add(variables.get(k - 1));
        }
        G[i - 1] += getOddFunction().evaluate(x);
      }
    }

    for (int i = 2; i <= numberOfObjectives(); i += 2) {
      for (int j = 1; j <= this.nk; j++) {

        List<Double> x = new ArrayList<>(numberOfVariables());

        for (int k = len.get(i - 1) + numberOfObjectives() - 1 + (j - 1) * subLen.get(i - 1) + 1;
            k <= len.get(i - 1) + numberOfObjectives() - 1 + j * subLen.get(i - 1);
            k++) {
          x.add(variables.get(k - 1));
        }
        G[i - 1] += getEvenFunction().evaluate(x);
      }
    }

    double cofficientG = 0.0;
    for (int i = 0; i < G.length; i++) {
      cofficientG += (G[i] / this.nk);
    }
    cofficientG = 1 + cofficientG;

    List<Double> y = new ArrayList<>(numberOfObjectives());
    for (int i = 0; i < numberOfObjectives() - 1; i++) {
      y.add(variables.get(i));
    }

    double sum = 0.0;
    for (int i = 1; i <= numberOfObjectives() - 1; i++) {
      sum += y.get(i - 1) / (1.0 + cofficientG) * (1.0 + Math.sin(3.0 * Math.PI * y.get(i - 1)));
    }

    y.add((1.0 + cofficientG) * (numberOfObjectives() - sum));
    return y;
  }
}
