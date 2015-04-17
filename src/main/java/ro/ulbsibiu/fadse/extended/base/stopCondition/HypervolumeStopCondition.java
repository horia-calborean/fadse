/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.base.stopCondition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.extended.qualityIndicator.HypervolumeNoTruePareto;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;

/**
 *
 * @author Horia
 */
public class HypervolumeStopCondition extends StopCondition {

    public HypervolumeStopCondition(Environment environment) {
        super(environment);
    }

    public boolean stopConditionFulfilled(List<File> listOfPopulationFiles) {
        boolean result = false;
        InputDocument id = environment.getInputDocument();
        //***********INITIALIZATION*************
        //TODO 
        int populationSize = 100;
        int nrOfobejctives = id.getObjectives().size();

        int X = 20;
        double treshold = 0.0015;
        //***********END INITIALIZATION*************


        HypervolumeNoTruePareto hypervolume = new HypervolumeNoTruePareto();

        try {
            LinkedList<Double> hypValues = new LinkedList<Double>();
            LinkedList parsedFiles = MetricsUtil.parseFiles(nrOfobejctives, populationSize, listOfPopulationFiles);//TODO repalce 100 with real size of pop
            double[] maxObjectives = MetricsUtil.getmaxObjectives(nrOfobejctives, parsedFiles);
            for (double[][] parsedFile : (LinkedList<double[][]>) parsedFiles) {
                //repairing Pareto optimal set = removing objectives with the value 0 and replacing them with the first individual of the current pop
                MetricsUtil.repairParetoOptimalSet(parsedFile, populationSize, nrOfobejctives);
                double value = hypervolume.hypervolume(parsedFile, maxObjectives, nrOfobejctives);
                hypValues.add(0, value);
//                System.out.println(hypValues.get(0));
            }
            double sum = 0;
            if (hypValues.size() > X) {
                for (int i = 1; i <= X; i++) {
                    sum += hypValues.get(0) - hypValues.get(i);
                }
            } else {
                sum = Integer.MAX_VALUE;
            }
//            System.out.println("SUM: " + sum);
            result = sum < treshold;
        } catch (FileNotFoundException ex) {
           Logger.getLogger(HypervolumeStopCondition.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HypervolumeStopCondition.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static void main(String[] args) {
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        String xmlFileName = "falsesimin.xml";
        Environment env = new Environment(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + xmlFileName);
        env.setResultsFolder(currentdir + System.getProperty("file.separator") + "test");
        HypervolumeStopCondition condition = new HypervolumeStopCondition(env);
        String resultsFolder = env.getResultsFolder();
        LinkedList<File> listOfPopulationFiles = MetricsUtil.getListOfFiles(resultsFolder, "filled");
        for (int i = 0; i < listOfPopulationFiles.size(); i++) {
            List<File> subLsit = listOfPopulationFiles.subList(0, i);
            if (condition.stopConditionFulfilled(subLsit)) {
                System.out.println("["+i+"]"+"FUllfilled");
            } else {
                System.out.println("["+i+"]"+"NOT fulfiled");
            }
        }



    }
}
