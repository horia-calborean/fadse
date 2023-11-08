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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.base.variable.Int;
import jmetal.problems.ProblemFactory;
import jMetal.util.JMException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Horia Calborean
 */
public class MetricsUtil {

    public static void computeUniqueIndividuals(int populationSize, File firstPopulation, LinkedList<File> listOfOffspringFiles, File metricsFolder, String fileName) throws IOException {
        Set<String> uniqueInd = new HashSet<String>();

        String fPath = metricsFolder.getAbsolutePath() + System.getProperty("file.separator");
        FileWriter uniqueFile = new FileWriter(fPath + fileName);
        BufferedWriter outUnique = new BufferedWriter(uniqueFile);

        BufferedReader input;
        String line = null; //not declared within while loop
        int lineCounter = 0;
        outUnique.write("Total individuals");
        outUnique.write(",");
        outUnique.write("New individuals");
        outUnique.newLine();
        int previousSize = 0;
        List<File> files = new LinkedList<File>();
        files.add(firstPopulation);
        files.addAll(listOfOffspringFiles);
        for (int i = 0; i < files.size(); i++) {
            input = new BufferedReader(new FileReader(files.get(i)));
            line = null; //not declared within while loop
            lineCounter = 0;
            while ((line = input.readLine()) != null && lineCounter < populationSize) {
                uniqueInd.add(line);
                lineCounter++;
            }
            outUnique.write(String.valueOf(uniqueInd.size()));
            outUnique.write(",");
            outUnique.write(String.valueOf(uniqueInd.size() - previousSize));
            outUnique.newLine();
            previousSize = uniqueInd.size();
        }
        outUnique.flush();
        outUnique.close();
    }

    public static void computeUniqueIndividualsWithRelations(Environment env, int populationSize, File firstPopulation, LinkedList<File> listOfOffspringFiles, File metricsFolder, String fileName) throws FileNotFoundException, IOException, JMException, ClassNotFoundException {
        List<File> files = new LinkedList<File>();
        files.add(firstPopulation);
        files.addAll(listOfOffspringFiles);
        Set<String> uniqueInd = new HashSet<String>();
        String fPath = metricsFolder.getAbsolutePath() + System.getProperty("file.separator");
        FileWriter uniqueFile = new FileWriter(fPath + fileName);
        BufferedWriter outUnique = new BufferedWriter(uniqueFile);
        outUnique.write("Total individuals");
        outUnique.write(",");
        outUnique.write("New individuals");
        outUnique.newLine();
        int previousSize = 0;
        String problemName = env.getInputDocument().getSimulatorName();
        Object[] problemParams = {env};
        Problem problem;
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        String neighborConfig = dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "neighborConfig.xml";
        env.setNeighborsConfigFile(neighborConfig);//this is going to crash someday :)
        problem = (new ProblemFactory()).getProblem(problemName, problemParams);
        for (File file : files) {
            BufferedReader input = new BufferedReader(new FileReader(file));

            String line = null; //not declared within while loop
            line = input.readLine();//skip the headder
            int i = 0;
            while ((line = input.readLine()) != null && i < populationSize) {
                i++;
                Solution solution = new Solution(problem);
//                solution.setDecisionVariables(new Int[env.getInputDocument().getParameters().length]);

                StringTokenizer tokenizer = new StringTokenizer(line, ",");

                for (int j = 0; j < env.getInputDocument().getParameters().length; j++) {
                    Double value = Double.valueOf(tokenizer.nextToken());
                    solution.getDecisionVariables()[j].setValue(value);
                }
                LinkedList<String> benchmarks = env.getInputDocument().getBenchmarks();
                /**
                 * for all variables... associate them with a parameter
                 */
                Parameter[] params = Utils.getParameters(solution, env);
                Individual ind = null;
                ind = new Individual(env, "");
                ind.setParameters(params);
                int[] activeParams = env.getInputDocument().getRelationTree1().getActiveNodes(solution);
                // List for all parameters
                List<String> paralist = new ArrayList<String>();

                // Add benchmark as parameter
                paralist.add("benchmark=" + ind.getBenchmark());

                // Add all parameters
                for (int k = 0; k < ind.getParameters().length; k++) {
                    Parameter p = ind.getParameters()[k];
                    if (activeParams[k] == 1) {
                        paralist.add(p.getName() + "=" + p.getValue());
                    } else {
                        paralist.add(p.getName() + "=N/A");
                    }
                }

                // Sort the parameter list and create string from it
                Collections.sort(paralist);
                String paramString = "";
                String pre = "";
                for (String item : paralist) {
                    paramString += pre + item;
                    pre = "|";
                }
                uniqueInd.add(paramString);
            }

            outUnique.write(String.valueOf(uniqueInd.size()));
            outUnique.write(",");
            outUnique.write(String.valueOf(uniqueInd.size() - previousSize));
            outUnique.newLine();
            previousSize = uniqueInd.size();
        }
        outUnique.flush();
        outUnique.close();
    }

    public static void writeFilenames(String folderPath, LinkedList<File> listOfPopulationFiles) throws IOException {
        FileWriter filesFile = new FileWriter(folderPath + System.getProperty("file.separator") + "files.csv");
        BufferedWriter outFiles = new BufferedWriter(filesFile);
        outFiles.write("Filenames");
        outFiles.newLine();
        for (int i = 0; i < listOfPopulationFiles.size(); i++) {
            outFiles.write(listOfPopulationFiles.get(i).getName().substring(0, listOfPopulationFiles.get(i).getName().length() - 4));
            outFiles.newLine();
        }
        outFiles.close();
    }

    public static void computeMetrics(int nrOfobejctives, int populationSize, File metricsFolder, LinkedList parsedFiles) throws FileNotFoundException, IOException {
        double[] maxObjectives = getmaxObjectives(nrOfobejctives, parsedFiles);
        //we have all the required values
        //compute hypervolume for all the files
        computeHypervolumeAndSevenPoint(nrOfobejctives, populationSize, maxObjectives, metricsFolder, "hypervolume.csv", "7point.csv", parsedFiles);
    }

    public static void computeHypervolumeAndSevenPoint(int nrOfobejctives, int populationSize, double[] maxObjectives, File metricsFolder, String hypervolumeFileName, String sevenPointFileName, LinkedList parsedFiles) {
        long StartTime = System.currentTimeMillis();
        HypervolumeNoTruePareto hypervolume = new HypervolumeNoTruePareto();
        SevenPointAverageDistance sevenPointAverageDistance = new SevenPointAverageDistance();
        String fPath = metricsFolder.getAbsolutePath() + System.getProperty("file.separator");

        try {
            FileWriter hypervolumeFile = new FileWriter(fPath + hypervolumeFileName);
            FileWriter sevenPointFile = new FileWriter(fPath + sevenPointFileName);
            BufferedWriter outHyp = new BufferedWriter(hypervolumeFile);
            BufferedWriter out7P = new BufferedWriter(sevenPointFile);
            outHyp.write("Hypervolume per generation (" + fPath + hypervolumeFileName + "),");
            outHyp.write("Hypervolume for all generated individuals (" + fPath + hypervolumeFileName + ")");
            outHyp.newLine();

            out7P.write("7 Point Average Distance per generation (" + fPath + sevenPointFileName + "),");
            out7P.write("7 Point Average Distance for all generated individuals (" + fPath + sevenPointFileName + ")");
            out7P.newLine();
            double[][] allInd = (double[][]) parsedFiles.get(0);
            repairParetoOptimalSet(allInd, populationSize, nrOfobejctives);
            int progress = 0;
            for (double[][] parsedFile : (LinkedList<double[][]>) parsedFiles) {
                //repairing Pareto optimal set = removing objectives with the value 0 and replacing them with the first individual of the current pop
                repairParetoOptimalSet(parsedFile, populationSize, nrOfobejctives);
                double value = hypervolume.hypervolume(parsedFile, maxObjectives, nrOfobejctives);
                outHyp.write(String.valueOf(value));
                value = sevenPointAverageDistance.compute(parsedFile, maxObjectives, nrOfobejctives);
                out7P.write(String.valueOf(value));
                outHyp.write(",");
                out7P.write(",");
                double temp[][] = new double[allInd.length + parsedFile.length][nrOfobejctives];
                for (int i = 0; i < allInd.length; i++) {
                    System.arraycopy(allInd[i], 0, temp[i], 0, nrOfobejctives);
                }
                for (int i = 0; i < parsedFile.length; i++) {
                    System.arraycopy(parsedFile[i], 0, temp[allInd.length + i], 0, nrOfobejctives);
                }
                allInd = temp;
                System.out.println((progress / (parsedFiles.size() + 0.0)) * 100 + "%");
                value = hypervolume.hypervolume(allInd, maxObjectives, nrOfobejctives);
                outHyp.write(String.valueOf(value));
                value = sevenPointAverageDistance.compute(allInd, maxObjectives, nrOfobejctives);
                out7P.write(String.valueOf(value));
                outHyp.newLine();
                out7P.newLine();
                //generateImage(nrOfobejctives, parsedFile, maxObjectives, fPath);
                progress++;
                outHyp.flush();
                out7P.flush();
            }
            //Close the output stream
            outHyp.close();
            out7P.close();

            FileWriter fstream = new FileWriter(metricsFolder.getAbsolutePath() + System.getProperty("file.separator") + "info.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Number of files: " + parsedFiles.size());


            out.newLine();
            out.write("Time needed to compute the metrics (seconds):" + ((System.currentTimeMillis() - StartTime) / 1000.0));
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void computeHypervolumeTwoSetDifference(int nrOfobejctives, int[] populationSize, double[] maxObjectives, File metricsFolder, LinkedList<LinkedList> parsedFiles) {
        long StartTime = System.currentTimeMillis();
        HypervolumeTwoSetDiference hypervolume = new HypervolumeTwoSetDiference();
        String fPath = metricsFolder.getAbsolutePath() + System.getProperty("file.separator");

        try {
            double progress = 0;

            double fullSize = 1;
            for (int i = 1; i <= parsedFiles.size(); i++) {
                fullSize *= i;
            }

            int count = 0;

            for (int i = 0; i < parsedFiles.size(); i++) {
                for (int j = i + 1; j < parsedFiles.size(); j++) {
                    if (i != j) {
                        String hypervolumeFileName = "hypervolumeTwoSetDifference_" + i + "_" + j + ".csv";
                        FileWriter hypervolumeFile = new FileWriter(fPath + hypervolumeFileName);
                        BufferedWriter outHyp = new BufferedWriter(hypervolumeFile);

                        outHyp.write("Hypervolume per generation (" + fPath + hypervolumeFileName + ")");
                        outHyp.newLine();

                        LinkedList<double[][]> firstLinkedList = parsedFiles.get(i);
                        LinkedList<double[][]> secondLinkedList = parsedFiles.get(j);

                        int minSize = 0;
                        if (firstLinkedList.size() < secondLinkedList.size()) {
                            minSize = firstLinkedList.size();
                        } else {
                            minSize = secondLinkedList.size();
                        }

                        for (int currentFront = 0; currentFront < minSize; currentFront++) {

                            double[][] firstFront = (double[][]) firstLinkedList.get(currentFront);
                            repairParetoOptimalSet(firstFront, populationSize[i], nrOfobejctives);

                            double[][] secondFront = (double[][]) secondLinkedList.get(currentFront);
                            repairParetoOptimalSet(secondFront, populationSize[j], nrOfobejctives);

                            double value = hypervolume.hypervolumeTwoSetDifference(firstFront, secondFront, maxObjectives, nrOfobejctives);
                            double value2 = hypervolume.hypervolumeTwoSetDifference(secondFront, firstFront, maxObjectives, nrOfobejctives);
                            double secondValue = hypervolume.hypervolume(firstFront, maxObjectives, nrOfobejctives);
                            double thirdValue = hypervolume.hypervolume(secondFront, maxObjectives, nrOfobejctives);
                            outHyp.write(String.valueOf(value));
                            outHyp.write("," + String.valueOf(value2));
                            outHyp.write("," + String.valueOf(secondValue));
                            outHyp.write("," + String.valueOf(thirdValue));
                            outHyp.write("\n");
                            progress += ((double) 100 / (minSize * fullSize));
                            System.out.println("Computing: " + (int) progress + "%");
                        }

                        //Close the output stream
                        outHyp.close();
                        count++;
                        progress = (count / fullSize * 100);
                        System.out.println("Computing: " + (int) progress + "%");
                    }
                }

            }


            FileWriter fstream = new FileWriter(metricsFolder.getAbsolutePath() + System.getProperty("file.separator") + "info.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Number of files: " + parsedFiles.size());


            out.newLine();
            out.write("Time needed to compute the metrics (seconds):" + ((System.currentTimeMillis() - StartTime) / 1000.0));
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TwoSetHypervolumeDifferenceResult computeHypervolumeTwoSetDifferenceForTwoSets(SolutionSet s1, SolutionSet s2, int nrObjectives, int nrIndividuals, double[] maxObjectives) {
        HypervolumeTwoSetDiference hypervolume = new HypervolumeTwoSetDiference();

        double[][] firstFront = new double[nrIndividuals][nrObjectives];
        double[][] secondFront = new double[nrIndividuals][nrObjectives];
        int numberIndividuals = s1.size();

        for (int i = 0; i < nrIndividuals; i++) {
            for (int j = 0; j < nrObjectives; j++) {
                double objS1 = s1.get(i).getObjective(j);
                double objS2 = s2.get(i).getObjective(j);
                firstFront[i][j] = objS1;
                secondFront[i][j] = objS2;
                if (objS1 > maxObjectives[j]) {
                    maxObjectives[j] = objS1;
                }
                if (objS2 > maxObjectives[j]) {
                    maxObjectives[j] = objS2;
                }
            }
        }

        repairParetoOptimalSet(firstFront, numberIndividuals, nrObjectives);

        repairParetoOptimalSet(secondFront, numberIndividuals, nrObjectives);

        double combinedHV12 = hypervolume.hypervolumeTwoSetDifference(firstFront, secondFront, maxObjectives, nrObjectives);
        double combinedHV21 = hypervolume.hypervolumeTwoSetDifference(secondFront, firstFront, maxObjectives, nrObjectives);
        double firstHV = hypervolume.hypervolume(firstFront, maxObjectives, nrObjectives);
        double secondHV = hypervolume.hypervolume(secondFront, maxObjectives, nrObjectives);
        TwoSetHypervolumeDifferenceResult result = new TwoSetHypervolumeDifferenceResult();
        result.CombinedHyperVolume12 = combinedHV12;
        result.CombinedHyperVolume21 = combinedHV21;
        result.SecondHyperVolume = secondHV;
        result.FirstHyperVolume = firstHV;
        return result;
    }

    public static LinkedList parseFiles(int nrOfobejctives, int populationSize, List<File> listOfPopulationFiles) throws FileNotFoundException, IOException {
        boolean skipFile = false;
        double[] objectives;
        LinkedList parsedFiles = new LinkedList();
        for (int i = 0; i < listOfPopulationFiles.size(); i++) {
            skipFile = false;
            if (listOfPopulationFiles.get(i).isFile()) {// one file
//                System.out.println("Computing metrics for: " + listOfFiles[i].getName());
                double[][] paretoOptimalSet = new double[populationSize][nrOfobejctives];//TODO
                BufferedReader input = new BufferedReader(new FileReader(listOfPopulationFiles.get(i)));
                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                int lineCounter = 0;
                boolean skipLine = false;
                while ((line = input.readLine()) != null && lineCounter < populationSize) {

                    skipLine = false;
                    objectives = new double[nrOfobejctives];
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    try {
                        while (tokenizer.hasMoreTokens()) {
                            for (int k = 0; k < nrOfobejctives - 1; k++) {
                                objectives[k] = objectives[k + 1];//shifting the objectives values with one position
                            }
                            objectives[nrOfobejctives - 1] = Double.parseDouble(tokenizer.nextToken());//ading at the end of the array the newest value
//                        System.out.println(objectives[nrOfobejctives - 1]);
                            if (objectives[nrOfobejctives - 1] >= 1.7976931348623157E+306) {
                                skipLine = true;
                                System.out.println("Skip line:" + objectives[nrOfobejctives - 1]);
                            }

                        }
                    } catch (NumberFormatException e) {
                        skipFile = true;
                    }
                    for (int k = 0; k < nrOfobejctives; k++) {

                        if (objectives[k] <= 0) {
                            System.out.println("Skip file");
                            skipFile = true;
                        }
                    }
                    //now we shoud have in the objectives the last "nrOfObjectives" values from a line
                    //we have to determine the maximum for each objective
                    if (!skipLine) {

                        //now in objectives we have all the objectives of one line - we now have to add them to a population
//                        System.out.print(Arrays.toString(objectives) + "#");
//                        System.out.println("");
                        paretoOptimalSet[lineCounter] = objectives;
                        lineCounter++;
                    }
                }
                input.close();
                if (!skipFile) {
                    parsedFiles.add(paretoOptimalSet);
                } else {
                    System.out.println("Skiped file " + listOfPopulationFiles.get(i).getName() + " it contained values of 0 for objectives");
                }
            }
        }
        return parsedFiles;
    }

    public static double[] getmaxObjectives(int nrOfObjectives, LinkedList parsedFiles) {
        double[] maxObjectives = new double[nrOfObjectives];
        for (int i = 0; i < parsedFiles.size(); i++) {
            for (double[] objectives : ((double[][]) parsedFiles.get(i))) {
                for (int k = 0; k < nrOfObjectives; k++) {
                    if (objectives[k] > maxObjectives[k]) {
                        maxObjectives[k] = objectives[k];
                    }
                }
            }
        }
        return maxObjectives;
    }

    public static void generateImage(int nrOfobejctives, double[][] parsedFile, double[] maxObjectives, String fPath) throws IOException {
        if (nrOfobejctives == 2) {
            BufferedImage image;
            XYSeries series;
            XYSeriesCollection dataset;
            JFreeChart chart;
            series = new XYSeries("XYGraph");
            for (int i = 0; i < parsedFile.length; i++) {
                series.add(parsedFile[i][0], parsedFile[i][1]);

            }
            series.add(maxObjectives[0], 0);
            series.add(0, maxObjectives[1]);
            dataset = new XYSeriesCollection();
            dataset.addSeries(series);
            chart = ChartFactory.createScatterPlot("XY Chart", // Title
                    "x-axis", // x-axis Label
                    "y-axis", // y-axis Label
                    dataset, // Dataset
                    PlotOrientation.VERTICAL, // Plot Orientation
                    true, // Show Legend
                    true, // Use tooltips
                    false // Configure chart to generate URLs?
                    );
            image = chart.createBufferedImage(700, 500);
            String result = fPath + System.currentTimeMillis() + ".png";
            ChartUtilities.saveChartAsPNG(new File(result), chart, 700, 700);
        }

    }

    /**
     * repairing Pareto optimal set = removing objectives with the value 0 and
     * replacing them with the first individual of the current pop
     *
     * @param paretoOptimalSet
     * @param populationSize
     * @param nrOfobejctives
     */
    public static void repairParetoOptimalSet(double[][] paretoOptimalSet, int populationSize, int nrOfobejctives) {
        for (int k = 0; k < populationSize; k++) {
            for (int l = 0; l < nrOfobejctives; l++) {
                if (paretoOptimalSet[k][l] == 0) {
                    paretoOptimalSet[k][l] = paretoOptimalSet[0][l];
                }
            }
        }
    }

    public static SolutionSet readPopulation(String pathToFile, int populationSize, int nrOfObjectives) throws FileNotFoundException, IOException {
        File filePop1 = new File(pathToFile);
        SolutionSet pop = new SolutionSet(populationSize);
        BufferedReader input = new BufferedReader(new FileReader(filePop1));
        String line = null; //not declared within while loop
        line = input.readLine();//skip the headder
        int lineCounter = 0;
        boolean skipLine = false;
        while ((line = input.readLine()) != null && lineCounter < populationSize) {
            skipLine = false;
            Solution sPop1 = new Solution(nrOfObjectives);
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            try {
                while (tokenizer.hasMoreTokens()) {
                    for (int k = 0; k < nrOfObjectives - 1; k++) {
                        sPop1.setObjective(k, sPop1.getObjective(k + 1));//shifting the objectives values with one position
                    }
                    sPop1.setObjective(nrOfObjectives - 1, Double.parseDouble(tokenizer.nextToken()));//ading at the end of the array the newest value
//                        System.out.println(objectives[nrOfobejctives - 1]);
                    if (sPop1.getObjective(nrOfObjectives - 1) >= 1.7976931348623157E+306) {
                        skipLine = true;
                        System.out.println("Skip line");
                    }
                }
            } catch (NumberFormatException e) {
                skipLine = true;
            }
            for (int k = 0; k < nrOfObjectives; k++) {
                if (sPop1.getObjective(k) <= 0) {
                    System.out.println("Skip file");
                    skipLine = true;
                }
            }
            //now we shoud have in the objectives the last "nrOfObjectives" values from a line
            //we have to determine the maximum for each objective
            if (!skipLine) {
                //now in objectives we have all the objectives of one line - we now have to add them to a population
//                        System.out.print(Arrays.toString(objectives) + "#");
//                        System.out.println("");
                pop.add(sPop1);
            }
        }
        return pop;
    }

    public static LinkedList<File> getListOfFiles(String folderPath, String prefix) {
        File folder = new File(folderPath);
        File[] listOfFilesTemp = folder.listFiles();
        //sort the files
        Arrays.sort(listOfFilesTemp);
        System.out.println(listOfFilesTemp);
        LinkedList<File> listOfPopulationFiles = new LinkedList<File>();
        for (int i = 0; i < listOfFilesTemp.length; i++) {
            if (listOfFilesTemp[i].isFile() && listOfFilesTemp[i].getName().startsWith(prefix) && listOfFilesTemp[i].getName().endsWith(".csv")) {
                listOfPopulationFiles.add(listOfFilesTemp[i]);
            }
        }
        return listOfPopulationFiles;
    }
}
