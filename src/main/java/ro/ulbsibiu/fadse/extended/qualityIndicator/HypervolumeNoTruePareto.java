/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei Zorila
 *   Copyright (c) 2009-2010
 *   All rights reserved.
 * 
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 * 
 *   The names of its contributors NOT may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *   OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package ro.ulbsibiu.fadse.extended.qualityIndicator;

import jmetal.qualityIndicator.Hypervolume;

/**
 *
 * @author Horia Calborean
 */
public class HypervolumeNoTruePareto {

    jmetal.qualityIndicator.util.MetricsUtil utils_;

    /**
     * Constructor
     * Creates a new instance of MultiDelta
     */
    public HypervolumeNoTruePareto() {
        utils_ = new jmetal.qualityIndicator.util.MetricsUtil();
    } // Hypervolume

    /*
    returns true if 'point1' dominates 'points2' with respect to the
    to the first 'noObjectives' objectives
     */
    boolean dominates(double point1[], double point2[], int noObjectives) {
        int i;
        int betterInAnyObjective;

        betterInAnyObjective = 0;
        for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++) {
            if (point1[i] > point2[i]) {
                betterInAnyObjective = 1;
            }
        }

        return ((i >= noObjectives) && (betterInAnyObjective > 0));
    } //Dominates

    void swap(double[][] front, int i, int j) {
        double[] temp;

        temp = front[i];
        front[i] = front[j];
        front[j] = temp;
    } // Swap


    /* all nondominated points regarding the first 'noObjectives' dimensions
    are collected; the points referenced by 'front[0..noPoints-1]' are
    considered; 'front' is resorted, such that 'front[0..n-1]' contains
    the nondominated points; n is returned */
    int filterNondominatedSet(double[][] front, int noPoints, int noObjectives) {
        int i, j;
        int n;

        n = noPoints;
        i = 0;
        while (i < n) {
            j = i + 1;
            while (j < n) {
                if (dominates(front[i], front[j], noObjectives)) {
                    /* remove point 'j' */
                    n--;
                    swap(front, j, n);
                } else if (dominates(front[j], front[i], noObjectives)) {
                    /* remove point 'i'; ensure that the point copied to index 'i'
                    is considered in the next outer loop (thus, decrement i) */
                    n--;
                    swap(front, i, n);
                    i--;
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }
        return n;
    } // FilterNondominatedSet


    /* calculate next value regarding dimension 'objective'; consider
    points referenced in 'front[0..noPoints-1]' */
    double surfaceUnchangedTo(double[][] front, int noPoints, int objective) {
        int i;
        double minValue, value;

        if (noPoints < 1) {
            System.err.println("run-time error");
        }

        minValue = front[0][objective];
        for (i = 1; i < noPoints; i++) {
            value = front[i][objective];
            if (value < minValue) {
                minValue = value;
            }
        }
        return minValue;
    } // SurfaceUnchangedTo

    /* remove all points which have a value <= 'threshold' regarding the
    dimension 'objective'; the points referenced by
    'front[0..noPoints-1]' are considered; 'front' is resorted, such that
    'front[0..n-1]' contains the remaining points; 'n' is returned */
    int reduceNondominatedSet(double[][] front, int noPoints, int objective,
            double threshold) {
        int n;
        int i;

        n = noPoints;
        for (i = 0; i < n; i++) {
            if (front[i][objective] <= threshold) {
                n--;
                swap(front, i, n);
            }
        }

        return n;
    } // ReduceNondominatedSet

    double calculateHypervolume(double[][] front, int noPoints, int noObjectives) {
        int n;
        double volume, distance;

        volume = 0;
        distance = 0;
        n = noPoints;
        while (n > 0) {
            int noNondominatedPoints;
            double tempVolume, tempDistance;

            noNondominatedPoints = filterNondominatedSet(front, n, noObjectives - 1);
            tempVolume = 0;
            if (noObjectives < 3) {
                if (noNondominatedPoints < 1) {
                    System.err.println("run-time error");
                }

                tempVolume = front[0][0];
            } else {
                tempVolume = calculateHypervolume(front,
                        noNondominatedPoints,
                        noObjectives - 1);
            }

            tempDistance = surfaceUnchangedTo(front, n, noObjectives - 1);
            volume += tempVolume * (tempDistance - distance);
            distance = tempDistance;
            n = reduceNondominatedSet(front, n, noObjectives - 1, distance);
        }
        return volume;
    } // CalculateHypervolume


    /* merge two fronts */
    double[][] mergeFronts(double[][] front1, int sizeFront1,
            double[][] front2, int sizeFront2, int noObjectives) {
        int i, j;
        int noPoints;
        double[][] frontPtr;

        /* allocate memory */
        noPoints = sizeFront1 + sizeFront2;
        frontPtr = new double[noPoints][noObjectives];
        /* copy points */
        noPoints = 0;
        for (i = 0; i < sizeFront1; i++) {
            for (j = 0; j < noObjectives; j++) {
                frontPtr[noPoints][j] = front1[i][j];
            }
            noPoints++;
        }
        for (i = 0; i < sizeFront2; i++) {
            for (j = 0; j < noObjectives; j++) {
                frontPtr[noPoints][j] = front2[i][j];
            }
            noPoints++;
        }

        return frontPtr;
    } // MergeFronts

    /**
     * Returns the hypervolume value of the paretoFront. The minimum values are set to 0
     * @param paretoFront The Pareto front
     * @param maximumValues Stores the maximum values. Needed for the reference point and for normalization
     * @param numberOfObjectives Number of objectives of the Pareto front
     */
    public double hypervolume(double[][] paretoFront,
            double[] maximumValues,
            int numberOfObjectives) {
        double[] minimumValues = new double[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            minimumValues[i] = 0;
        }
        return hypervolume(paretoFront, maximumValues, minimumValues, numberOfObjectives);
    }

    /**
     * Returns the hypervolume value of the paretoFront. This method call to the
     * calculate hipervolume one
     * @param paretoFront The Pareto front
     * @param maximumValues Stores the maximum values. Needed for the reference point and for normalization
     * @param minimumValues  Stores the minimum values. Needed for the reference point and for normalization
     * @param numberOfObjectives Number of objectives of the Pareto front
     */
    public double hypervolume(double[][] paretoFront,
            double[] maximumValues, double[] minimumValues,
            int numberOfObjectives) {



        /**
         * Stores the normalized front.
         */
        double[][] normalizedFront;

        /**
         * Stores the inverted front. Needed for minimization problems
         */
        double[][] invertedFront;


        // STEP 2. Get the normalized front
        normalizedFront = utils_.getNormalizedFront(paretoFront,
                maximumValues,
                minimumValues);

        // STEP 3. Inverse the pareto front. This is needed because of the original
        //metric by Zitzler is for maximization problems
        invertedFront = utils_.invertedFront(normalizedFront);

        // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
        return this.calculateHypervolume(invertedFront, invertedFront.length, numberOfObjectives);
    }// hypervolume

    public static void main(String args[]) {

        //Create a new instance of the metric
        HypervolumeNoTruePareto qualityIndicator = new HypervolumeNoTruePareto();
        //Read the front from the files
        double[][] solutionFront = qualityIndicator.utils_.readFront("outputs/mibench-tele-adpcm-file-decode_obj_oder_date.txt");
        double[] maximumValues = qualityIndicator.utils_.getMaximumValues(solutionFront, 2);
        //Obtain delta value
        for (int i = 0; i < solutionFront.length; i++) {

            double[][] partialSolution = new double[i+1][2];
            for(int j =0; j<=i;j++){
                partialSolution[j][0] = solutionFront[j][0];
                partialSolution[j][1] = solutionFront[j][1];
            }

            double value = qualityIndicator.hypervolume(partialSolution, maximumValues, 2);
            System.out.println(value);
        }
    } // main
} // Hypervolume

