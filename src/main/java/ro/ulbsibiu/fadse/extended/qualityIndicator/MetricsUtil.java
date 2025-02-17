package ro.ulbsibiu.fadse.extended.qualityIndicator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.bounds.Bounds;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.extended.problems.ProblemFactory;
import ro.ulbsibiu.fadse.utils.Utils;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.*;

public class MetricsUtil {

    public static void computeUniqueIndividuals(int populationSize, File firstPopulation, LinkedList<File> listOfOffspringFiles, File metricsFolder, String fileName) throws IOException {
        Set<String> uniqueInd = new HashSet<>();

        String fPath = metricsFolder.getAbsolutePath() + FileSystems.getDefault().getSeparator();
        FileWriter uniqueFile = new FileWriter(fPath + fileName);
        BufferedWriter outUnique = new BufferedWriter(uniqueFile);

        BufferedReader input;
        String line; //not declared within while loop
        int lineCounter = 0;
        outUnique.write("Total individuals");
        outUnique.write(",");
        outUnique.write("New individuals");
        outUnique.newLine();
        int previousSize = 0;
        List<File> files = new LinkedList<>();
        files.add(firstPopulation);
        files.addAll(listOfOffspringFiles);
        for (File file : files) {
            input = new BufferedReader(new FileReader(file));
            //not declared within while loop
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

    public static void computeUniqueIndividualsWithRelations(Environment env, int populationSize, File firstPopulation, LinkedList<File> listOfOffspringFiles, File metricsFolder, String fileName) throws IOException {
        List<File> files = new LinkedList<>();
        files.add(firstPopulation);
        files.addAll(listOfOffspringFiles);
        Set<String> uniqueInd = new HashSet<>();
        String fPath = metricsFolder.getAbsolutePath() + FileSystems.getDefault().getSeparator();
        FileWriter uniqueFile = new FileWriter(fPath + fileName);
        BufferedWriter outUnique = new BufferedWriter(uniqueFile);
        outUnique.write("Total individuals");
        outUnique.write(",");
        outUnique.write("New individuals");
        outUnique.newLine();
        int previousSize = 0;
        DoubleProblem problem;
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir);
        String neighborConfig = dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "neighborConfig.xml";
        env.setNeighborsConfigFile(neighborConfig);//this is going to crash someday :)
        problem = (DoubleProblem) ProblemFactory.<DoubleSolution>loadProblem(env);
        for (File file : files) {
            BufferedReader input = new BufferedReader(new FileReader(file));

            String line; //not declared within while loop
            input.readLine();
            int i = 0;
            while ((line = input.readLine()) != null && i < populationSize) {
                i++;
                DoubleSolution solution = problem.createSolution();
//                solution.setDecisionVariables(new Int[env.getInputDocument().getParameters().length]);

                StringTokenizer tokenizer = new StringTokenizer(line, ",");

                for (int j = 0; j < env.getInputDocument().getParameters().length; j++) {
                    Double value = Double.valueOf(tokenizer.nextToken());
                    solution.variables().set(j, value);
                }

                Parameter[] params = Utils.getParameters(solution, env);
                Individual ind;
                ind = new Individual(env, "");
                ind.setParameters(params);
                int[] activeParams = env.getInputDocument().getRelationTree1().getActiveNodes(solution);
                // List for all parameters
                List<String> paralist = new ArrayList<>();

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
                StringBuilder paramString = new StringBuilder();
                String pre = "";
                for (String item : paralist) {
                    paramString.append(pre).append(item);
                    pre = "|";
                }
                uniqueInd.add(paramString.toString());
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
        FileWriter filesFile = new FileWriter(folderPath + FileSystems.getDefault().getSeparator() + "files.csv");
        BufferedWriter outFiles = new BufferedWriter(filesFile);
        outFiles.write("Filenames");
        outFiles.newLine();
        for (File listOfPopulationFile : listOfPopulationFiles) {
            outFiles.write(listOfPopulationFile.getName().substring(0, listOfPopulationFile.getName().length() - 4));
            outFiles.newLine();
        }
        outFiles.close();
    }

    public static void computeMetrics(int nrOfObjectives, int populationSize, File metricsFolder, LinkedList parsedFiles) {
        double[] maxObjectives = getMaxObjectives(nrOfObjectives, parsedFiles);
        //we have all the required values
        //compute hypervolume for all the files
        computeHypervolumeAndSevenPoint(nrOfObjectives, populationSize, maxObjectives, metricsFolder, "hypervolume.csv", "7point.csv", parsedFiles);
    }

    public static void computeHypervolumeAndSevenPoint(int nrOfObjectives, int populationSize, double[] maxObjectives, File metricsFolder, String hypervolumeFileName, String sevenPointFileName, LinkedList parsedFiles) {
        long StartTime = System.currentTimeMillis();
        HypervolumeNoTruePareto hypervolume = new HypervolumeNoTruePareto();
        SevenPointAverageDistance sevenPointAverageDistance = new SevenPointAverageDistance();
        String fPath = metricsFolder.getAbsolutePath() + FileSystems.getDefault().getSeparator();

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
            repairParetoOptimalSet(allInd, populationSize, nrOfObjectives);
            int progress = 0;
            for (double[][] parsedFile : (LinkedList<double[][]>) parsedFiles) {
                //repairing Pareto optimal set = removing objectives with the value 0 and replacing them with the first individual of the current pop
                repairParetoOptimalSet(parsedFile, populationSize, nrOfObjectives);
                double value = hypervolume.hypervolume(parsedFile, maxObjectives, nrOfObjectives);
                outHyp.write(String.valueOf(value));
                value = sevenPointAverageDistance.compute(parsedFile, maxObjectives, nrOfObjectives);
                out7P.write(String.valueOf(value));
                outHyp.write(",");
                out7P.write(",");
                double[][] temp = new double[allInd.length + parsedFile.length][nrOfObjectives];
                for (int i = 0; i < allInd.length; i++) {
                    System.arraycopy(allInd[i], 0, temp[i], 0, nrOfObjectives);
                }
                for (int i = 0; i < parsedFile.length; i++) {
                    System.arraycopy(parsedFile[i], 0, temp[allInd.length + i], 0, nrOfObjectives);
                }
                allInd = temp;
                System.out.println((progress / (parsedFiles.size() + 0.0)) * 100 + "%");
                value = hypervolume.hypervolume(allInd, maxObjectives, nrOfObjectives);
                outHyp.write(String.valueOf(value));
                value = sevenPointAverageDistance.compute(allInd, maxObjectives, nrOfObjectives);
                out7P.write(String.valueOf(value));
                outHyp.newLine();
                out7P.newLine();
                //generateImage(nrObjectives, parsedFile, maxObjectives, fPath);
                progress++;
                outHyp.flush();
                out7P.flush();
            }
            //Close the output stream
            outHyp.close();
            out7P.close();

            FileWriter fstream = new FileWriter(metricsFolder.getAbsolutePath() + FileSystems.getDefault().getSeparator() + "info.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Number of files: " + parsedFiles.size());


            out.newLine();
            out.write("Time needed to compute the metrics (seconds):" + ((System.currentTimeMillis() - StartTime) / 1000.0));
            out.close();


        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void computeHypervolumeTwoSetDifference(int nrObjectives, int[] populationSize, double[] maxObjectives, File metricsFolder, LinkedList<LinkedList> parsedFiles) {
        long StartTime = System.currentTimeMillis();
        HypervolumeTwoSetDiference hypervolume = new HypervolumeTwoSetDiference();
        String fPath = metricsFolder.getAbsolutePath() + FileSystems.getDefault().getSeparator();

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

                        LinkedList firstLinkedList = parsedFiles.get(i);
                        LinkedList secondLinkedList = parsedFiles.get(j);

                        int minSize;
                        if (firstLinkedList.size() < secondLinkedList.size()) {
                            minSize = firstLinkedList.size();
                        } else {
                            minSize = secondLinkedList.size();
                        }

                        for (int currentFront = 0; currentFront < minSize; currentFront++) {

                            double[][] firstFront = (double[][]) firstLinkedList.get(currentFront);
                            repairParetoOptimalSet(firstFront, populationSize[i], nrObjectives);

                            double[][] secondFront = (double[][]) secondLinkedList.get(currentFront);
                            repairParetoOptimalSet(secondFront, populationSize[j], nrObjectives);

                            double value = hypervolume.hypervolumeTwoSetDifference(firstFront, secondFront, maxObjectives, nrObjectives);
                            double value2 = hypervolume.hypervolumeTwoSetDifference(secondFront, firstFront, maxObjectives, nrObjectives);
                            double secondValue = hypervolume.hypervolume(firstFront, maxObjectives, nrObjectives);
                            double thirdValue = hypervolume.hypervolume(secondFront, maxObjectives, nrObjectives);
                            outHyp.write(String.valueOf(value));
                            outHyp.write("," + value2);
                            outHyp.write("," + secondValue);
                            outHyp.write("," + thirdValue);
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
            e.fillInStackTrace();
        }
    }

    public static <S extends Solution<?>> TwoSetHypervolumeDifferenceResult computeHypervolumeTwoSetDifferenceForTwoSets(List<S> s1, List<S> s2, int nrObjectives, int nrIndividuals, double[] maxObjectives) {
        HypervolumeTwoSetDiference hypervolume = new HypervolumeTwoSetDiference();

        double[][] firstFront = new double[nrIndividuals][nrObjectives];
        double[][] secondFront = new double[nrIndividuals][nrObjectives];
        int numberIndividuals = s1.size();

        for (int i = 0; i < nrIndividuals; i++) {
            for (int j = 0; j < nrObjectives; j++) {
                double objS1 = s1.get(i).objectives()[j];
                double objS2 = s2.get(i).objectives()[j];
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

    public static LinkedList parseFiles(int nrOfObjectives, int populationSize, List<File> listOfPopulationFiles) throws IOException {
        boolean skipFile = false;
        double[] objectives;
        LinkedList parsedFiles = new LinkedList();
        for (File listOfPopulationFile : listOfPopulationFiles) {
            skipFile = false;
            if (listOfPopulationFile.isFile()) {// one file
//                System.out.println("Computing metrics for: " + listOfFiles[i].getName());
                double[][] paretoOptimalSet = new double[populationSize][nrOfObjectives];//TODO
                BufferedReader input = new BufferedReader(new FileReader(listOfPopulationFile));
                String line = null; //not declared within while loop
                input.readLine();
                int lineCounter = 0;
                boolean skipLine = false;
                while ((line = input.readLine()) != null && lineCounter < populationSize) {

                    skipLine = false;
                    objectives = new double[nrOfObjectives];
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    try {
                        while (tokenizer.hasMoreTokens()) {
                            for (int k = 0; k < nrOfObjectives - 1; k++) {
                                objectives[k] = objectives[k + 1];//shifting the objectives values with one position
                            }
                            objectives[nrOfObjectives - 1] = Double.parseDouble(tokenizer.nextToken());//adding at the end of the array the newest value
//                        System.out.println(objectives[nrObjectives - 1]);
                            if (objectives[nrOfObjectives - 1] >= 1.7976931348623157E+306) {
                                skipLine = true;
                                System.out.println("Skip line:" + objectives[nrOfObjectives - 1]);
                            }

                        }
                    } catch (NumberFormatException e) {
                        skipFile = true;
                    }
                    for (int k = 0; k < nrOfObjectives; k++) {

                        if (objectives[k] <= 0) {
                            System.out.println("Skip file");
                            skipFile = true;
                        }
                    }
                    //now we should have in the objectives the last "nrOfObjectives" values from a line
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
                    System.out.println("Skipped file " + listOfPopulationFile.getName() + " it contained values of 0 for objectives");
                }
            }
        }
        return parsedFiles;
    }

    public static double[] getMaxObjectives(int nrOfObjectives, LinkedList parsedFiles) {
        double[] maxObjectives = new double[nrOfObjectives];
        for (Object parsedFile : parsedFiles) {
            for (double[] objectives : ((double[][]) parsedFile)) {
                for (int k = 0; k < nrOfObjectives; k++) {
                    if (objectives[k] > maxObjectives[k]) {
                        maxObjectives[k] = objectives[k];
                    }
                }
            }
        }
        return maxObjectives;
    }

    public static void generateImage(int nrOfObjectives, double[][] parsedFile, double[] maxObjectives, String fPath) throws IOException {
        if (nrOfObjectives == 2) {
            XYSeries series;
            XYSeriesCollection dataset;
            JFreeChart chart;
            series = new XYSeries("XYGraph");
            for (double[] doubles : parsedFile) {
                series.add(doubles[0], doubles[1]);

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
            chart.createBufferedImage(700, 500);
            String result = fPath + System.currentTimeMillis() + ".png";
            ChartUtilities.saveChartAsPNG(new File(result), chart, 700, 700);
        }

    }

    public static void repairParetoOptimalSet(double[][] paretoOptimalSet, int populationSize, int nrOfObjectives) {
        for (int k = 0; k < populationSize; k++) {
            for (int l = 0; l < nrOfObjectives; l++) {
                if (paretoOptimalSet[k][l] == 0) {
                    paretoOptimalSet[k][l] = paretoOptimalSet[0][l];
                }
            }
        }
    }

    public static <S extends Solution<?>> List<S> readPopulation(String pathToFile, int populationSize, int nrOfObjectives) throws IOException {
        File filePop1 = new File(pathToFile);
        List<S> pop = new ArrayList<>(populationSize);
        BufferedReader input = new BufferedReader(new FileReader(filePop1));
        String line; //not declared within while loop
        input.readLine();
        int lineCounter = 0;
        boolean skipLine;
        while ((line = input.readLine()) != null && lineCounter < populationSize) {
            skipLine = false;
            List<Bounds<Double>> boundsList = new ArrayList<>();
            S sPop1 = (S) new DefaultDoubleSolution(boundsList, nrOfObjectives,0);
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            try {
                while (tokenizer.hasMoreTokens()) {
                    for (int k = 0; k < nrOfObjectives - 1; k++) {
                        sPop1.objectives()[k] = sPop1.objectives()[k + 1];//shifting the objectives values with one position
                    }
                    sPop1.objectives()[nrOfObjectives - 1] = Double.parseDouble(tokenizer.nextToken());//adding at the end of the array the newest value
//                        System.out.println(objectives[nrObjectives - 1]);
                    if (sPop1.objectives()[nrOfObjectives - 1] >= 1.7976931348623157E+306) {
                        skipLine = true;
                        System.out.println("Skip line");
                    }
                }
            } catch (NumberFormatException e) {
                skipLine = true;
            }
            for (int k = 0; k < nrOfObjectives; k++) {
                if (sPop1.objectives()[k] <= 0) {
                    System.out.println("Skip file");
                    skipLine = true;
                }
            }
            //now we should have in the objectives the last "nrOfObjectives" values from a line
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
        assert listOfFilesTemp != null;
        Arrays.sort(listOfFilesTemp);
        System.out.println(Arrays.toString(listOfFilesTemp));
        LinkedList<File> listOfPopulationFiles = new LinkedList<>();
        for (File file : listOfFilesTemp) {
            if (file.isFile() && file.getName().startsWith(prefix) && file.getName().endsWith(".csv")) {
                listOfPopulationFiles.add(file);
            }
        }
        return listOfPopulationFiles;
    }
}
