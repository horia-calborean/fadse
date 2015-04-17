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

import jmetal.qualityIndicator.util.MetricsUtil;

/**
 *
 * @author Horia Calborean
 */
/**
 * The “7-Point�? average distance measure is similar to generational distance. For this metric the true Pareto front is not needed. It generates seven points (vectors) in objective space for comparison.
Assuming a bi-objective maximization problem and an (f1, f2) coordinate system with origin at (0,0). First the maximum value in each objective dimension is found (f1max and f2max). The 2 first points from the total of 7 are: (0,f2max) and (f1max,0). Then the next 5 points are computed. These points are: (0,f2max/3), (0,f2max/2), (f1max/3,0), (f1max/2,0) and (0,0).
The “7-Point�? average distance measure is the average Euclidean distances from each of the seven axis points to closest individual of the known Pareto front.
 * @author Horia Calborean
 */
public class SevenPointAverageDistance {

    MetricsUtil util_;

    public SevenPointAverageDistance() {
        util_ = new MetricsUtil();
    }

    /**
     * The “7-Point�? average distance measure is similar to generational distance. For this metric the true Pareto front is not needed. It generates seven points (vectors) in objective space for comparison.
    Assuming a bi-objective maximization problem and an (f1, f2) coordinate system with origin at (0,0). First the maximum value in each objective dimension is found (f1max and f2max). The 2 first points from the total of 7 are: (0,f2max) and (f1max,0). Then the next 5 points are computed. These points are: (0,f2max/3), (0,f2max/2), (f1max/3,0), (f1max/2,0) and (0,0).
    The “7-Point�? average distance measure is the average Euclidean distances from each of the seven axis points to closest individual of the known Pareto front.
     * @param paretoOptimalSet
     * @return
     */
    public double compute(double[][] paretoOptimalSet, double[] maximumValues, int numberOfObjectives) {

        //determine the maximum/minimum of each objective
//        Iterator<Solution> it = paretoOptimalSet.iterator();

    

        double sum = 0;
        for (int i = 0; i < numberOfObjectives; i++) {
            //point max_i
            double[] temp = new double[numberOfObjectives];
            for (int j = 0; j < numberOfObjectives; j++) {
                temp[j] = 0;
            }
            temp[i] = maximumValues[i];
            double result1 = util_.distanceToNearestPoint(temp, paretoOptimalSet);//todo turn this into a for(1:4)
            //point max_i/2
            temp[i] = 0;
            temp[i] = maximumValues[i] / 2;
            double result2 = util_.distanceToNearestPoint(temp, paretoOptimalSet);
            //point max_i/3
            temp[i] = 0;
            temp[i] = maximumValues[i] / 3;
            double result3 = util_.distanceToNearestPoint(temp, paretoOptimalSet);
            //point 0
            temp[i] = 0;
            double result4 = util_.distanceToNearestPoint(temp, paretoOptimalSet);
            //add to sum
            sum += (result1 + result2 + result3 + result4) / 4;
        }
        sum = sum / numberOfObjectives;

        return sum;

    }

    public static void main(String args[]) {

        //Create a new instance of the metric
        SevenPointAverageDistance qualityIndicator = new SevenPointAverageDistance();
        //Read the front from the files
        double[][] solutionFront = qualityIndicator.util_.readFront("outputs/mibench-tele-adpcm-file-decode_obj_oder_date.txt");
        double[] maximumValues = qualityIndicator.util_.getMaximumValues(solutionFront, 2);
        //Obtain delta value
        for (int i = 0; i < solutionFront.length; i++) {

            double[][] partialSolution = new double[i + 1][2];
            for (int j = 0; j <= i; j++) {
                partialSolution[j][0] = solutionFront[j][0];
                partialSolution[j][1] = solutionFront[j][1];
            }

            double value = qualityIndicator.compute(partialSolution,maximumValues, 2);
            System.out.println(value);
        }
    } // main 
}
