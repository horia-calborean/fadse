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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import jmetal.base.SolutionSet;

/**
 *
 * @author Horia Calborean
 */
public class CoverageFromTwoFolders {

    public static void main(String[] args) throws IOException {
//        String folderPath1;
//        int nrOfobejctives1 = 2;
//        int populationSize1 = 100;
//        System.out.println("Specify path to folder");
//        folderPath1 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
//        System.out.println("Specify nr of objectives");
//        nrOfobejctives1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
//        System.out.println("Specify the population size");
//        populationSize1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
//
//        String folderPath2;
//        int nrOfobejctives2 = 2;
//        int populationSize2 = 100;
//        System.out.println("Specify path to folder");
//        folderPath2 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
//        System.out.println("Specify nr of objectives");
//        nrOfobejctives2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
//        System.out.println("Specify the population size");
//        populationSize2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        int nrObjectives = 2 ;
        int populationSize = 100;
        String folderPath1 = "D:\\Work\\Doctorat\\Output\\FADSE\\ServerSimulator\\RaduResults\\cnsgaii0.1";
        String folderPath2 = "D:\\Work\\Doctorat\\Output\\FADSE\\ServerSimulator\\RaduResults\\cnsgaii0.9";

        LinkedList<File> listOfPopulationFiles1 = MetricsUtil.getListOfFiles(folderPath1, "filled");
        LinkedList<File> listOfPopulationFiles2 = MetricsUtil.getListOfFiles(folderPath2, "filled");

        File metricsFolder = new File(folderPath1 + System.getProperty("file.separator") + "metricsComposed" + System.currentTimeMillis());
        if (metricsFolder.mkdir()) {
            LinkedList parsedFiles1 = MetricsUtil.parseFiles(nrObjectives, populationSize,  listOfPopulationFiles1);
            LinkedList parsedFiles2 = MetricsUtil.parseFiles(nrObjectives, populationSize,  listOfPopulationFiles2);
            int minSize = parsedFiles1.size() < parsedFiles2.size() ? parsedFiles1.size() : parsedFiles2.size();
            String fPath = metricsFolder.getAbsolutePath() + System.getProperty("file.separator");


            FileWriter coverageFile = new FileWriter(fPath + "coverage.csv");
            BufferedWriter outCov = new BufferedWriter(coverageFile);
            outCov.write("Coverage of population 1 ("+folderPath1+") over population 2 ("+folderPath2+"),");
            outCov.write("Coverage of population 2 ("+folderPath2+") over population 1 ("+folderPath1+")");
            outCov.newLine();
            for (int i = 0; i < minSize; i++) {
                SolutionSet pop1 = MetricsUtil.readPopulation(listOfPopulationFiles1.get(i).getAbsolutePath(), populationSize, nrObjectives);
                SolutionSet pop2 = MetricsUtil.readPopulation(listOfPopulationFiles2.get(i).getAbsolutePath(), populationSize, nrObjectives);

                //TODO write result to csv file
                outCov.write(""+CoverageOfTwoSets.computeCoverage(pop1, pop2)+",");
                outCov.write(""+CoverageOfTwoSets.computeCoverage(pop2, pop1)+"");
                outCov.newLine();
                outCov.flush();
            }
            outCov.close();
        } else {
            System.out.println("Directory was not created");
        }
    }
}
