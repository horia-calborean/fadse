package core.qualityindicator;

import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.List;

public class SpreadIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    public SpreadIndicator(int nrObjectives) {
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
    }

    public double calculateSpread(List<? extends S> population) {
        List<S> casted = (List<S>) population; // safe due to generic contract
        Ranking<S> fronts = new FastNonDominatedSortRanking<S>().compute(casted);
        List<S> firstFront = fronts.getSubFront(0);

        double[][] normalizedFront = normalizeFront(firstFront);
        Spread spread = new Spread(this.referenceFront);

        return spread.compute(normalizedFront);
    }
}
