/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.problems.ProblemFactory;
import jMetal.util.JMException;

/**
 *
 * @author Radu
 */
public class SolutionSetHelper {

    public static SolutionSetHelperResult ReadPopulationsFromFolder(String folder, String xmlFileName) throws JMException {

        String environmentConfigFile = folder + System.getProperty("file.separator") + xmlFileName;

        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        String neighborConfig = dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "neighborConfig.xml";

        Environment env = new Environment(environmentConfigFile);
        env.setNeighborsConfigFile(neighborConfig);
        String problemName = env.getInputDocument().getSimulatorName();

        // is a simulator
        Object[] problemParams = {env};
        Problem problem_ = (new ProblemFactory()).getProblem(problemName, problemParams);

        LinkedList<File> files = MetricsUtil.getListOfFiles(folder, "filled");

        List<SolutionSet> solutionSets = new LinkedList<SolutionSet>();
        for (File file : files) {
            try {
                List<Solution> currentPopulations = new LinkedList<Solution>();
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                while ((line = input.readLine()) != null) {
                    Solution sol = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        sol.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                    }
                    for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
                        sol.setObjective(j, Double.valueOf(tokenizer.nextToken()));
                    }
                    currentPopulations.add(sol);
                } //while   

                SolutionSet population = new SolutionSet(currentPopulations.size());
                for (Solution sol : currentPopulations) {
                    population.add(sol);
                }

                solutionSets.add(population);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        SolutionSetHelperResult sshr = new SolutionSetHelperResult();
        sshr.Environment = env;
        sshr.Populations = solutionSets;
        return sshr;
    }

    public static void DumpPopulationToFile(String folderName, String fileName, SolutionSet ss, Environment env) {
        String result = (new Utils()).generateCSVHeadder(env);
        result += (new Utils()).generateCSV(ss);
        try {
            (new File(folderName)).mkdirs();
            BufferedWriter out = new BufferedWriter(new FileWriter(folderName + System.getProperty("file.separator") + fileName));
            out.write(result);
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
