package core.qualityindicator;


import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.List;

public class HypervolumeIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    public HypervolumeIndicator(int nrObjectives){
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
    }

    public double calculateHypervolume(List<? extends S> population) {
        List<S> casted = (List<S>) population; // safe due to generic contract
        Ranking<S> fronts = new FastNonDominatedSortRanking<S>().compute(casted);
        List<S> firstFront = fronts.getSubFront(0);

        double[][] front = normalizeFront(casted);
        WFGHypervolume hypervolume = new WFGHypervolume(this.referenceFront);
        return hypervolume.compute(front);
    }
}
