package ro.ulbsibiu.fadse.extended.qualityIndicator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;

import java.util.List;

public class CoverageOfTwoSets {

    /**
     * it computes the coverage of pop1 over pop2 the method has to be called also with pop2 and pop1 as parameters
     */
    public static <S extends Solution<?>> double computeCoverage(List<? extends Solution<?>> pop1, List<? extends Solution<?>> pop2) {
//Case study pop1 = IBEA, pop2 = SPEA2 - easier to reason about :)
        double dominationCount = 0;
        boolean dominated;
        DominanceComparator<S> comparator = new DefaultDominanceComparator<>();
        for (Solution<?> s : pop2) {
            dominated = false;
            //x1 individual from IBEA
            for (Solution<?> x1 : pop1) {
                // if (x2.isDominatedBy(x1)) {//x1 domiantes x2
                if (comparator.compare((S) s, (S) x1) > 0) {//-1, or 0, or 1 if solution1 dominates solution2, both are
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
        //return result>1?result-1:result;//unknown bug, it should not be greater than 1
        return dominationCount / (double)pop2.size();//we must find the bug
    }
}