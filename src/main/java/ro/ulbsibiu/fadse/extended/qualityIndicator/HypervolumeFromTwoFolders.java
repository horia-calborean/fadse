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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Horia Calborean
 */
public class HypervolumeFromTwoFolders {

    public static void main(String[] args) throws IOException {
        String folderPath1;
        int nrOfobejctives1 = 2;
        int populationSize1 = 100;
        System.out.println("Specify path to folder 1");
        folderPath1 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        System.out.println("Specify nr of objectives 1");
//        nrOfobejctives1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        System.out.println("Specify the population size 1");
//        populationSize1 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());

        String folderPath2;
        int nrOfobejctives2 = 2;
        int populationSize2 = 100;
        System.out.println("Specify path to folder 2 ");
        folderPath2 = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        nrOfobejctives2 = nrOfobejctives1;
        System.out.println("Specify the population size 2");
//        populationSize2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());

        LinkedList<File> listOfPopulationFiles1 = MetricsUtil.getListOfFiles(folderPath1, "filled");
        LinkedList<File> listOfPopulationFiles2 = MetricsUtil.getListOfFiles(folderPath2, "filled");
        
        File metricsFolder = new File(folderPath1 + System.getProperty("file.separator") + "metricsComposed" + System.currentTimeMillis());
        if (metricsFolder.mkdir()) {
            LinkedList parsedFiles1 = MetricsUtil.parseFiles(nrOfobejctives1, populationSize1, listOfPopulationFiles1);
            LinkedList parsedFiles2 = MetricsUtil.parseFiles(nrOfobejctives2, populationSize2, listOfPopulationFiles2);
            double[] maxObjectives1 = MetricsUtil.getmaxObjectives(nrOfobejctives1, parsedFiles1);
            double[] maxObjectives2 = MetricsUtil.getmaxObjectives(nrOfobejctives2, parsedFiles2);
            double[] maxObjectives = new double[nrOfobejctives1];
            for(int i = 0; i<nrOfobejctives1;i++){
                if(maxObjectives1[i]>maxObjectives2[i]){
                    maxObjectives[i] = maxObjectives1[i];
                }else{
                    maxObjectives[i] = maxObjectives2[i];
                }
            }
            MetricsUtil.computeHypervolumeAndSevenPoint(nrOfobejctives1, populationSize1, maxObjectives, metricsFolder, "hypervolume1.csv", "7point1.csv", parsedFiles1);
            MetricsUtil.computeHypervolumeAndSevenPoint(nrOfobejctives2, populationSize2, maxObjectives, metricsFolder, "hypervolume2.csv", "7point2.csv", parsedFiles2);
        } else {
            System.out.println("Directory was not created");
        }
    }
}
