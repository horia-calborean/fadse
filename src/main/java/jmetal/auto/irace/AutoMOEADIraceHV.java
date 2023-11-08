package jmetal.auto.irace;

import static jmetal.core.util.SolutionListUtils.getMatrixWithObjectiveValues;

import java.io.IOException;
import jmetal.auto.autoconfigurablealgorithm.AutoMOEAD;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.VectorUtils;

public class AutoMOEADIraceHV {
  public static void main(String[] args) throws IOException {
    AutoMOEAD autoMOEAD = new AutoMOEAD();
    autoMOEAD.parse(args);

    EvolutionaryAlgorithm<DoubleSolution> mopso = autoMOEAD.create();
    mopso.run();

    String referenceFrontFile = "resources/referenceFrontsCSV/"
        + autoMOEAD.referenceFrontFilenameParameter.value();

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",");
    double[][] front = getMatrixWithObjectiveValues(mopso.result()) ;

    double[][] normalizedReferenceFront = NormalizeUtils.normalize(referenceFront);
    double[][] normalizedFront =
        NormalizeUtils.normalize(
            front,
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    var qualityIndicator = new PISAHypervolume(normalizedReferenceFront) ;
    System.out.println(qualityIndicator.compute(normalizedFront) * -1.0) ;
  }
}
