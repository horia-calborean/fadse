/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 *
 * @author Radu
 */
public class HypervolumeTwoSetDiference extends HypervolumeNoTruePareto {

    public HypervolumeTwoSetDiference() {
        super();
    }

    /**
     * Returns the hypervolume value of the difference between the combined paretoFront and the paretoFront2. The minimum values are set to 0
     * @param paretoFront1 - First paretoFront
     * @param paretoFront2 - Second paretoFront
     * @param maximumValues Stores the maximum values. Needed for the reference point and for normalization
     * @param numberOfObjectives Number of objectives of the Pareto front
     */
    public double hypervolumeTwoSetDifference(double[][] paretoFront1, double[][] paretoFront2,
            double[] maximumValues,
            int numberOfObjectives) {

        double[][] combinedFront = new double[paretoFront1.length + paretoFront2.length][numberOfObjectives];

        int currentPosition = 0;
        for (int i = 0; i < paretoFront1.length; i++) {
            for (int j = 0; j < numberOfObjectives; j++) {
                combinedFront[currentPosition][j] = paretoFront1[i][j];
            }
            currentPosition++;
        }

        for (int i = 0; i < paretoFront2.length; i++) {
            for (int j = 0; j < numberOfObjectives; j++) {
                combinedFront[currentPosition][j] = paretoFront2[i][j];
            }
            currentPosition++;
        }

        double valueCombined = hypervolume(combinedFront, maximumValues, 2);
        double valueSecond = hypervolume(paretoFront2, maximumValues, 2);

        return valueCombined - valueSecond;
    }

//    public static void main(String args[]) {
//
//        HypervolumeTwoSetDiference qualityIndicator = new HypervolumeTwoSetDiference();
//
//        double[][] solutionFront = qualityIndicator.utils_.readFront("outputs/mibench-tele-adpcm-file-decode_obj_oder_date.txt");
//        double[] maximumValues = qualityIndicator.utils_.getMaximumValues(solutionFront, 2);
//
//        for (int i = 1; i < solutionFront.length; i++) {
//            int halfSize = (i + 1) / 2;
//            int secondHalfSize = i - halfSize;
//            double[][] partialSolutionFront1 = new double[halfSize][2];
//            double[][] partialSolutionFront2 = new double[secondHalfSize][2];
//
//            double[][] combinedSolutionFront = new double[i][2];
//
//            for (int j = 0; j < halfSize; j++) {
//                partialSolutionFront1[j][0] = solutionFront[j][0];
//                partialSolutionFront1[j][1] = solutionFront[j][1];
//            }
//
//            for (int j = 0; j < secondHalfSize; j++) {
//                partialSolutionFront2[j][0] = solutionFront[halfSize + j][0];
//                partialSolutionFront2[j][1] = solutionFront[halfSize + j][1];
//            }
//
//            double value = qualityIndicator.hypervolumeTwoSetDifference(partialSolutionFront1, partialSolutionFront2, maximumValues, 2);
//            System.out.println("i = "+i+": "+value);
//        }
//
//    }
    public static void main(String args[]) {
        try {
            HypervolumeHelperResult result = HypervolumeHelper.ReadDirectories();
            if (result != null) {                
                    MetricsUtil.computeHypervolumeTwoSetDifference(result.NrObjectives, result.PopulationSizeN, result.MaxObjectives, result.MetricsFolder, result.ParsedFilesN);
            }
        } catch (IOException ex) {
            System.out.println("An IOException occured");
        }
    }
}
