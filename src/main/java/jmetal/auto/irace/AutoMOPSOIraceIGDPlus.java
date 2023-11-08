package jmetal.auto.irace;

import static jmetal.core.util.SolutionListUtils.getMatrixWithObjectiveValues;

import java.io.IOException;
import jmetal.auto.autoconfigurablealgorithm.AutoMOPSO;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistancePlus;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.VectorUtils;

public class AutoMOPSOIraceIGDPlus {

  public static void main(String[] args) throws IOException {
    AutoMOPSO mopsoWithParameters = new AutoMOPSO();
    mopsoWithParameters.parse(args);

    ParticleSwarmOptimizationAlgorithm mopso = mopsoWithParameters.create();
    mopso.run();

    String referenceFrontFile = "resources/referenceFrontsCSV/"
        + mopsoWithParameters.referenceFrontFilenameParameter.value();

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",");
    double[][] front = getMatrixWithObjectiveValues(mopso.result());

    double[][] normalizedReferenceFront = NormalizeUtils.normalize(referenceFront);
    double[][] normalizedFront =
        NormalizeUtils.normalize(
            front,
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    var igdPlus = new InvertedGenerationalDistancePlus(normalizedReferenceFront);
    System.out.println(igdPlus.compute(normalizedFront));
  }
}
