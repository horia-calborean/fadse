/**
 * SMSEMOA_main.java
 *
 * @author Simon Wessing
 * @version 1.0
 *   This implementation of SMS-EMOA makes use of a QualityIndicator object
 *   to obtained the convergence speed of the algorithm.
 *   
 */
package jmetal.metaheuristics.smsemoa;

import jmetal.base.*;
import jmetal.base.operator.crossover.*;
import jmetal.base.operator.mutation.*;
import jmetal.base.operator.selection.*;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.*;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import java.io.IOException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.qualityIndicator.QualityIndicator;

public class SMSEMOA_main {

    public static Logger logger_;      // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException
     * Usage: three options
     *      - jmetal.metaheuristics.smsemoa.SMSEMOA_main
     *      - jmetal.metaheuristics.smsemoa.SMSEMOA_main problemName
     *      - jmetal.metaheuristics.smsemoa.SMSEMOA_main problemName paretoFrontFile
     */
    public static void main(String[] args) throws
            JMException,
            SecurityException,
            IOException,
            ClassNotFoundException {
        Problem problem;         // The problem to solve
        Algorithm algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("SMSEMOA_main.log");
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
            problem = new Kursawe("Real", 3);
        //problem = new Kursawe("BinaryReal", 3);
        //problem = new Water("Real");
        //problem = new ZDT1("ArrayReal", 100);
        //problem = new ConstrEx("Real");
        //problem = new DTLZ1("Real");
        //problem = new OKA2("Real") ;
        } // else

        algorithm = new SMSEMOA(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 100);
        algorithm.setInputParameter("maxEvaluations", 25000);
        algorithm.setInputParameter("offset", 100.0);

        // Mutation and Crossover for Real codification
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
        crossover.setParameter("probability", 0.9);
        crossover.setParameter("distributionIndex", 20.0);

        mutation = MutationFactory.getMutationOperator("PolynomialMutation");
        mutation.setParameter("probability", 1.0 / problem.getNumberOfVariables());
        mutation.setParameter("distributionIndex", 20.0);

        // Selection Operator
        selection = SelectionFactory.getSelectionOperator("RandomSelection");
        // also possible
        //selection = SelectionFactory.getSelectionOperator("BinaryTournament2");

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Add the indicator object to the algorithm
        algorithm.setInputParameter("indicators", indicators);

        // Execute the Algorithm 
        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages
        logger_.info("Total execution time: " + estimatedTime + "ms");
        logger_.info("Variables values have been written to file VAR");
        population.printVariablesToFile("VAR");
        logger_.info("Objectives values have been written to file FUN");
        population.printObjectivesToFile("FUN");

        if (indicators != null) {
            logger_.info("Quality indicators");
            logger_.info("Hypervolume: " + indicators.getHypervolume(population));
            logger_.info("GD         : " + indicators.getGD(population));
            logger_.info("IGD        : " + indicators.getIGD(population));
            logger_.info("Spread     : " + indicators.getSpread(population));
            logger_.info("Epsilon    : " + indicators.getEpsilon(population));

            int evaluations = ((Integer) algorithm.getOutputParameter("evaluations")).intValue();
            logger_.info("Speed      : " + evaluations + " evaluations");
        } // if
    } //main
} // SMSEMOA_main
