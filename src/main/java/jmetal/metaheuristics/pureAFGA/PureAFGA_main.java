package jmetal.metaheuristics.pureAFGA;

import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ProblemFactory;
import jmetal.problems.ZDT.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.ApplicationConstants;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class PureAFGA_main {
    public static Logger logger_; // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     *                           jmetal.metaheuristics.pureAFGA.PureAFGA_main -
     *                           jmetal.metaheuristics.pureAFGA.PureAFGA_main problemName -
     *                           jmetal.metaheuristics.pureAFGA.PureAFGA_main problemName paretoFrontFile
     */
    public static void main(String[] args) throws JMException,
            SecurityException, IOException, ClassNotFoundException {
        Problem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use
        Operator crossover; // Crossover operator
        Operator mutation; // Mutation operator
        Operator selection; // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("PureAFGA_main.log");
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
            // problem = new Kursawe("Real", 3);
            // problem = new Kursawe("BinaryReal", 3);
            // problem = new Water("Real");
            //problem = new ZDT1("ArrayReal", 100);
            // problem = new ConstrEx("Real");
            problem = new DTLZ1("Real");
            //problem = new LOTZProblem("Real",2);
            // problem = new OKA2("Real") ;
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
            algorithm = new PureAFGA(problem);

            // Algorithm parameters
            algorithm.setInputParameter("populationSize", 100);
            algorithm.setInputParameter("maxEvaluations", 20000);

            // Mutation and Crossover for Real codification
            crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
            crossover.setParameter("probability", 0.9);
            crossover.setParameter("distributionIndex", 20.0);

            mutation = MutationFactory.getMutationOperator("PolynomialMutation");
            mutation.setParameter("probability",
                    1.0 / problem.getNumberOfVariables());
            mutation.setParameter("distributionIndex", 20.0);

            // Selection Operator
            selection = SelectionFactory.getSelectionOperator("BinaryTournamentAfr");

            // Add the operators to the algorithm
            algorithm.addOperator("crossover", crossover);
            algorithm.addOperator("mutation", mutation);
            algorithm.addOperator("selection", selection);

            // Add the indicator object to the algorithm
            algorithm.setInputParameter("indicators", indicators);
            algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals", "0");

            boolean outputEveryPopulation = true;
            algorithm.setInputParameter("outputEveryPopulation",
                    outputEveryPopulation);
            String outputPath = "outputs/" + System.currentTimeMillis();
            if (outputEveryPopulation) {
                outputPath = ApplicationConstants.OutputFolder
                        + problem.getName()
                        + "/PureAFGA/"
                        + "/";
                boolean created = (new File(outputPath)).mkdirs();
                algorithm.setInputParameter("outputPath", outputPath);
            }

            // Execute the Algorithm
            long initTime = System.currentTimeMillis();
            SolutionSet population = algorithm.execute();
            long endTime = System.currentTimeMillis();
            long estimatedTime = endTime - initTime;

            // Result messages
            logger_.info("Total execution time: " + estimatedTime + "ms");
            logger_.info("Variables values have been writen to file VAR");
            population.printVariablesToFile(outputPath + "VAR");
            logger_.info("Objectives values have been writen to file FUN");
            population.printObjectivesToFile(outputPath + "FUN");

            if (indicators != null) {
                logger_.info("Quality indicators");
                logger_.info("Hypervolume: "
                        + indicators.getHypervolume(population));
                logger_.info("GD         : " + indicators.getGD(population));
                logger_.info("IGD        : " + indicators.getIGD(population));
                logger_.info("Spread     : " + indicators.getSpread(population));
                logger_.info("Epsilon    : " + indicators.getEpsilon(population));

                int evaluations = ((Integer) algorithm
                        .getOutputParameter("evaluations")).intValue();
                logger_.info("Speed      : " + evaluations + " evaluations");
            } // if
        }
    }
}
