package core.qualityindicator;

import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.List;

public class EpsilonIndicator<S extends Solution<?>> extends QualityIndicator<S> {
    public EpsilonIndicator(int nrObjectives) {
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
    }

    public double calculateEpsilon(List<? extends S> population) {
        List<S> casted = (List<S>) population; // safe due to generic contract

        double[][] normalizedFront = normalizeFront(casted);
        Epsilon eps = new Epsilon(this.referenceFront);

        return eps.compute(normalizedFront);
    }
}
