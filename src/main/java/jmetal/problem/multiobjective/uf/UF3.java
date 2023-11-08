package jmetal.problem.multiobjective.uf;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem CEC2009_UF3
 */
@SuppressWarnings("serial")
public class UF3 extends AbstractDoubleProblem {
    
 /** 
  * Constructor.
  * Creates a default instance of problem CEC2009_UF3 (30 decision variables)
  */
  public UF3() {
    this(30);
  }
  
 /**
  * Creates a new instance of problem CEC2009_UF3.
  * @param numberOfVariables Number of variables.
  */
  public UF3(int numberOfVariables) {
    numberOfObjectives(2) ;
    numberOfConstraints(0) ;
    name("UF3") ;

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables) ;
    List<Double> upperLimit = new ArrayList<>(numberOfVariables) ;

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0.0);
      upperLimit.add(1.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /** Evaluate() method */
  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] x = new double[numberOfVariables()];
    for (int i = 0; i < solution.variables().size(); i++) {
      x[i] = solution.variables().get(i) ;
    }


  	int count1, count2;
		double sum1, sum2, prod1, prod2, yj, pj;
		sum1   = sum2   = 0.0;
		count1 = count2 = 0;
 		prod1  = prod2  = 1.0;

    
    for (int j = 2 ; j <= numberOfVariables(); j++) {
			yj = x[j-1]-Math.pow(x[0],0.5*(1.0+3.0*(j-2.0)/(numberOfVariables()-2.0)));
			pj = Math.cos(20.0*yj*Math.PI/Math.sqrt(j));
			if (j % 2 == 0) {
				sum2  += yj*yj;
				prod2 *= pj;
				count2++;
			} else {
				sum1  += yj*yj;
				prod1 *= pj;
				count1++;
			}
    }
    
    solution.objectives()[0] = x[0] + 2.0*(4.0*sum1 - 2.0*prod1 + 2.0) / (double)count1;
    solution.objectives()[1] = 1.0 - Math.sqrt(x[0]) + 2.0*(4.0*sum2 - 2.0*prod2 + 2.0) / (double)count2;

    return solution ;
  }
}
