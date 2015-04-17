/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.SolutionSet;

/**
 *
 * @author Radu
 */
public class HypervolumeHelper {

    public static HypervolumeHelperResult ReadDirectories() throws IOException {
        int n = 2;
        System.out.println("Specify number of folders");
        n = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        int nrOfobejctives = 0;
        int[] populationSizeN = new int[n];
        String[] folderPathN = new String[n];
        LinkedList<LinkedList<File>> listOfPopulationFilesN = new LinkedList<LinkedList<File>>();
        System.out.println("Specify nr of objectives ");
        nrOfobejctives = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        System.out.println("Specify the population size ");
        int populationSizeN_temp = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        for (int i = 0; i < n; i++) {
            System.out.println("Specify path to folder " + (i + 1));
            folderPathN[i] = (new BufferedReader(new InputStreamReader(System.in))).readLine();
//                System.out.println("Specify the population size " + (i+1));
            populationSizeN[i] = populationSizeN_temp;
            listOfPopulationFilesN.add(MetricsUtil.getListOfFiles(folderPathN[i], "filled"));
        }

        File metricsFolder = new File(folderPathN[0] + System.getProperty("file.separator") + "metricsComposed" + System.currentTimeMillis());
        if (metricsFolder.mkdir()) {
            LinkedList<LinkedList> parsedFilesN = new LinkedList<LinkedList>();
            LinkedList<double[]> maxObjectivesN = new LinkedList<double[]>();
            for (int i = 0; i < n; i++) {
                parsedFilesN.add(MetricsUtil.parseFiles(nrOfobejctives, populationSizeN[i], listOfPopulationFilesN.get(i)));
                System.out.println("Files found for folder " + i + ":" + parsedFilesN.get(i).size());
                maxObjectivesN.add(MetricsUtil.getmaxObjectives(nrOfobejctives, parsedFilesN.get(i)));
            }
            double[] maxObjectives = new double[nrOfobejctives];
            for (int i = 0; i < nrOfobejctives; i++) {
                maxObjectives[i] = max(maxObjectivesN, i);
            }

            HypervolumeHelperResult result = new HypervolumeHelperResult();

            result.MaxObjectives = maxObjectives;
            result.MetricsFolder = metricsFolder;
            result.NrFolders = n;
            result.NrObjectives = nrOfobejctives;
            result.PopulationSizeN = populationSizeN;
            result.ParsedFilesN = parsedFilesN;

            return result;

        } else {
            //System.out.println("Directory was not created");
            return null;
        }
    }        

    public static double max(LinkedList<double[]> maxObjectivesN, int i) {
        double max = 0;
        for (double[] currentVec : maxObjectivesN) {
            if (currentVec[i] > max) {
                max = currentVec[i];
            }
        }
        return max;
    }        
    
    public static void main(String[] args){
        String folder = "D:\\Work\\Doctorat\\Output\\FADSE\\ServerSimulator\\MOCHC Scoala\\1345563456000";
        LinkedList<File> files = MetricsUtil.getListOfFiles(folder, "filled");
        LinkedList<SolutionSet> solutions = new LinkedList<SolutionSet>();
        for(File file : files){
            try {
                SolutionSet set =  MetricsUtil.readPopulation(file.getAbsolutePath(), 100, 2);
                solutions.add(set);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(HypervolumeHelper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(HypervolumeHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        double[] maxObjectives = new double[2];
        int hyperVolumecount = 0;
        for(int i = 1;i<solutions.size();i++){                         
            TwoSetHypervolumeDifferenceResult p = MetricsUtil.computeHypervolumeTwoSetDifferenceForTwoSets(solutions.get(i-1), solutions.get(i), 2, 100, maxObjectives);
            DecimalFormat df = new DecimalFormat(".################");             
            System.out.println(df.format(p.CombinedHyperVolume21));
            if(p.CombinedHyperVolume21 < p.FirstHyperVolume/1000){
                hyperVolumecount++;
            }
            else{
                hyperVolumecount = 0;
            }
            if(hyperVolumecount > 5){
                System.out.println("Do the Cataclysmic");
                hyperVolumecount = 0;
            }
            System.out.println("12: "+p.CombinedHyperVolume12 + "21: "+p.CombinedHyperVolume21 + "   2: "+ p.FirstHyperVolume + "  3:" + p.SecondHyperVolume);
        }
    }
}
