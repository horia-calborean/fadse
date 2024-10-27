package ro.ulbsibiu.fadse;




import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;

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
/**
 *
 * @author Horia Calborean
 */
public class ComputeMetrics {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

/*
 Specify path to folder
C:\Users\Horia\Documents\My Dropbox\joint work\Towards Adaptive Code-Optimization\results of adaptive optimization for GAP\1 Benchmark HW+SW\hwsw-1-auto-qsort results1320316965078
Specify nr of objectives
2
Specify the population size
100
Specify path to input XML
C:\Users\Horia\Documents\My Dropbox\joint work\Towards Adaptive Code-Optimization\results of adaptive optimization for GAP\1 Benchmark HW+SW\combination_9bench_hwsw_1.xml
 */
        String folderPath;
        int nrOfobejctives = 2;
        int populationSize = 100;
        String pathToInputXML;
        if (args.length < 3) {
            System.out.println("Specify path to folder");
            folderPath = (new BufferedReader(new InputStreamReader(System.in))).readLine();
//            folderPath = "D:\\Work\\Doctorat\\Output\\FADSE\\ServerSimulator\\MOCHC Scoala\\1345563456000";
            System.out.println("Specify nr of objectives");
            nrOfobejctives = 2;//Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
            System.out.println("Specify the population size");
            populationSize = 100;//Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
            System.out.println("Specify path to input XML");
            pathToInputXML = "D:\\Work\\Doctorat\\FADSE\\configs\\gapdistsimin_radu.xml";//(new BufferedReader(new InputStreamReader(System.in))).readLine();
           // pathToInputXML = "C:\\Users\\Horia\\Documents\\My Dropbox\\joint work\\Towards Adaptive Code-Optimization\\results of adaptive optimization for GAP\\1 Benchmark HW+SW\\combination_9bench_hwsw_1.xml";
        } else {
            folderPath = args[0];
            nrOfobejctives = Integer.parseInt(args[1]);
            populationSize = Integer.parseInt(args[2]);
            pathToInputXML = args[3];
        }
        LinkedList<File> listOfPopulationFiles = MetricsUtil.getListOfFiles(folderPath, "filled");
        LinkedList<File> listOfOffspringFiles = MetricsUtil.getListOfFiles(folderPath, "offspring");
        MetricsUtil.writeFilenames(folderPath, listOfPopulationFiles);
        File metricsFolder = new File(folderPath + System.getProperty("file.separator") + "metrics" + System.currentTimeMillis());
        if (metricsFolder.mkdir()) {
            LinkedList parsedFiles = MetricsUtil.parseFiles(nrOfobejctives, populationSize, listOfPopulationFiles);
            MetricsUtil.computeMetrics(nrOfobejctives, populationSize, metricsFolder, parsedFiles);
            MetricsUtil.computeUniqueIndividuals(populationSize, listOfPopulationFiles.get(0), listOfOffspringFiles, metricsFolder,"unique.csv");
            MetricsUtil.computeUniqueIndividuals(populationSize, listOfPopulationFiles.get(0), listOfPopulationFiles, metricsFolder,"population_increase.csv");            
//            try {
//                environment.Environment env = new Environment(pathToInputXML);
//                MetricsUtil.computeUniqueIndividualsWithRelations(env, populationSize, listOfPopulationFiles.get(0), listOfOffspringFiles, metricsFolder, "unique_with_relations.csv");
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(ComputeMetrics.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (JMException ex) {
//                Logger.getLogger(ComputeMetrics.class.getName()).log(Level.SEVERE, null, ex);
//            }

        } else {
            System.out.println("Directory was not created");
        }
    }

   
}
