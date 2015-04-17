/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import ro.ulbsibiu.fadse.extended.qualityIndicator.CoverageOfTwoSets;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
/**
 *
 * @author Horia Calborean
 */
public class CoverageFromTwoFiles {
public static void main(String[] args) throws IOException {

        long StartTime = System.currentTimeMillis();

        String folderPathPop1;
        int nrOfobejctivesPop1 = 2;
        int populationSizePop1 = 100;
        System.out.println("Specify path to FILE for first population");
        folderPathPop1 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        System.out.println("Specify nr of objectives");
        nrOfobejctivesPop1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        System.out.println("Specify the population size");
        populationSizePop1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());

        String folderPathPop2;
        int nrOfobejctivesPop2 = 2;
        int populationSizePop2 = 100;
        System.out.println("Specify path to FILE for second population");
        folderPathPop2 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        System.out.println("Specify nr of objectives");
        nrOfobejctivesPop2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        System.out.println("Specify the population size");
        populationSizePop2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        SolutionSet pop1 = MetricsUtil.readPopulation(folderPathPop1,populationSizePop1,nrOfobejctivesPop1);
        SolutionSet pop2 = MetricsUtil.readPopulation(folderPathPop2,populationSizePop2,nrOfobejctivesPop2);
        CoverageOfTwoSets coverage = new CoverageOfTwoSets();
        System.out.println("coverage pop1 - pop2: "+coverage.computeCoverage(pop1, pop2));
        System.out.println("coverage pop2 - pop1: "+coverage.computeCoverage(pop2, pop1));

    }

   
}
