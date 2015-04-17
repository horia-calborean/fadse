/**
 * SPEA2_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.spea2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import jmetal.base.*;
import jmetal.base.operator.crossover.*;
import jmetal.base.operator.mutation.*;
import jmetal.base.operator.selection.*;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.*;
import jmetal.qualityIndicator.QualityIndicator;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.util.ApplicationConstants;

public class SPEA2_main {

    public static Logger logger_;      // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments. The first (optional) argument
     * specifies the problem to solve.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     * jmetal.metaheuristics.mocell.MOCell_main -
     * jmetal.metaheuristics.mocell.MOCell_main problemName -
     * jmetal.metaheuristics.mocell.MOCell_main problemName ParetoFrontFile
     */
    public static void main(String[] args) throws JMException, IOException, ClassNotFoundException {
        Problem problem;         // The problem to solve
        Algorithm algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("SPEA2.log");
        logger_.addHandler(fileHandler_);

        indicators = null;
        if (args.length == 1) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
        } // if
        else if (args.length == 2) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
            indicators = new QualityIndicator(problem, args[1]);
        } // if
        else { // Default problem
            //problem = new Kursawe("Real", 3); 
            //problem = new Water("Real");
            //problem = new ZDT6("ArrayReal", 1000);
            //problem = new ZDT4("BinaryReal");
            //problem = new WFG1("Real");
            problem = new DTLZ1("Real");
            //problem = new OKA2("Real") ;
        } // else
        List<Problem> problems = new LinkedList<Problem>();
        problems.add(new DTLZ1("Real"));
        problems.add(new DTLZ2("Real"));
        problems.add(new DTLZ3("Real"));
        problems.add(new DTLZ4("Real"));
        problems.add(new DTLZ5("Real"));
        problems.add(new DTLZ6("Real"));
        problems.add(new DTLZ7("Real"));
        problems.add(new ZDT1("Real", 100));
        problems.add(new ZDT2("Real", 100));
        problems.add(new ZDT3("Real", 100));
        problems.add(new ZDT4("Real", 100));
        problems.add(new ZDT6("Real", 100));
        for (int i = 0; i < problems.size(); i++) {
            problem = problems.get(i);
            algorithm = new SPEA2(problem);

            // Algorithm parameters
            algorithm.setInputParameter("populationSize", 100);
            algorithm.setInputParameter("archiveSize", 100);
            algorithm.setInputParameter("maxEvaluations", 20000);

            // Mutation and crossover for real codification
            crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
            crossover.setParameter("probability", 0.9);
            crossover.setParameter("distributionIndex", 20.0);
            mutation = MutationFactory.getMutationOperator("PolynomialMutation");
            mutation.setParameter("probability", 1.0 / problem.getNumberOfVariables());
            mutation.setParameter("distributionIndex", 20.0);

            // Selection operator 
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2");

            // Add the operators to the algorithm
            algorithm.addOperator("crossover", crossover);
            algorithm.addOperator("mutation", mutation);
            algorithm.addOperator("selection", selection);

            boolean outputEveryPopulation = true;
            algorithm.setInputParameter("outputEveryPopulation",
                    outputEveryPopulation);
            String outputPath = "outputs/" + System.currentTimeMillis();
            if (outputEveryPopulation) {
                outputPath = ApplicationConstants.OutputFolder
                        + problem.getName()
                        + "/SPEA2/"
                        + "/";
                boolean created = (new File(outputPath)).mkdirs();
                algorithm.setInputParameter("outputPath", outputPath);
            }

            // Execute the algorithm
            long initTime = System.currentTimeMillis();
            SolutionSet population = algorithm.execute();
            long estimatedTime = System.currentTimeMillis() - initTime;

            // Result messages 
            logger_.info("Total execution time: " + estimatedTime + "ms");
            logger_.info("Objectives values have been writen to file FUN");
            population.printObjectivesToFile("FUN");
            logger_.info("Variables values have been writen to file VAR");
            population.printVariablesToFile("VAR");

            if (indicators != null) {
                logger_.info("Quality indicators");
                logger_.info("Hypervolume: " + indicators.getHypervolume(population));
                logger_.info("GD         : " + indicators.getGD(population));
                logger_.info("IGD        : " + indicators.getIGD(population));
                logger_.info("Spread     : " + indicators.getSpread(population));
                logger_.info("Epsilon    : " + indicators.getEpsilon(population));
            } // if 
        }
    }//main
} // SPEA2_main.java
