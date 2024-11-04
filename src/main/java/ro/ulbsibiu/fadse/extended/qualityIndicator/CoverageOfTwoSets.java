/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.qualityIndicator;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.DominanceComparator;

/**
 *
 * @author Horia Andrei Calborean <horia.calborean@ulbsibiu.ro>
 */
public class CoverageOfTwoSets {

    /**
     * it computes the coverage of pop1 over pop2 the method has to be called also with pop2 and pop1 as parameters
     * @param pop1
     * @param pop2
     * @return
     */
    public static double computeCoverage(SolutionSet pop1, SolutionSet pop2) {
//Case study pop1 = IBEA, pop2 = SPEA2 - more easy to reason about :)
        double dominationCount = 0;
        boolean dominated = false;
        DominanceComparator comaparator = new DominanceComparator();
        for (int i = 0; i<pop2.size(); i++) {
            dominated = false;
            Solution x2 = pop2.get(i); //x2 individual from SPEA2 - expect this one to be better
            for (int j = 0; j<pop1.size();j++){
                Solution x1 = pop1.get(j);//x1 individual from IBEA
               // if (x2.isDominatedBy(x1)) {//x1 domiantes x2
                if(comaparator.compare(x2, x1)>0){//-1, or 0, or 1 if solution1 dominates solution2, both are
                                                //non-dominated, or solution1  is dominated by solution22, respectively.
                    //this if is true if x2 (SPEA2) is dominated by x1 (IBEA) -if IBEA is better
                dominated = true;
                    break;
                }
            }
            if (dominated) {
                dominationCount++;//this number is high if IBEA is better (the first parameter)
            }
        }
        double result = dominationCount / (double)pop2.size();
        //return result>1?result-1:result;//unknown bug, it should not be greater than 1
        return result;//we must find the bug
    }
}
