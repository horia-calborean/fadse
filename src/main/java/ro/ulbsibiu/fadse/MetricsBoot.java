package ro.ulbsibiu.fadse;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Andrei
 */
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.Operator;
import jmetal.core.problem.Problem;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.solution.Solution;
import jmetal.SolutionSet;

import java.io.File;

import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.problems.DTLZ.DTLZ1;
import jmetal.qualityIndicator.QualityIndicator;
import jMetal.util.JMException;
import jmetal.base.*;
import jmetal.base.operator.crossover.*   ;
import jmetal.base.operator.mutation.*    ;
import jmetal.base.operator.selection.*   ;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.problems.DTLZ.DTLZ2;
import jmetal.problems.DTLZ.DTLZ3;
import jmetal.problems.DTLZ.DTLZ4;

import java.text.DecimalFormat;
import java.util.ArrayList;

import jmetal.base.operator.comparator.FitnessComparator;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.problems.DTLZ.DTLZ5;
import jmetal.problems.DTLZ.DTLZ6;
import jmetal.problems.DTLZ.DTLZ7;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;

import static jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder.NSGAIIVariant.NSGAII;

public class MetricsBoot {

/**
 * Returns the dimensions of a multidimensional array
 * @param array
 * @return number of dimensions
 */
public static int getArrayDimensions(Object array) {
    int arrDimensions = 0;
    Class cls = array.getClass();

    while (cls.isArray()) {
        arrDimensions++;
        cls = cls.getComponentType();
    }
    
    return arrDimensions;
}

/**
 * Reads a solution file into a SolutionSet object
 * @param filename
 * @return SolutionSet
 */
public static SolutionSet GetSolutionSetFromFile(String filename){
    MetricsUtil mu = new MetricsUtil();
    double[][] solution = mu.readFront(filename);
    
    SolutionSet solSet = new SolutionSet(solution.length);

    int nrOfObjectives =  solution[0].length;

    for (int i=0; i<solution.length; i++){
        Solution tmpSol = new Solution(nrOfObjectives);
        
        for (int j=0; j<nrOfObjectives; j++)
            tmpSol.setObjective(j, solution[i][j]);
        solSet.add(tmpSol);
    }
    return solSet;

}
public static SolutionSet RunAlgorithmNSGAII(Problem problem, int populationSize, int maxEvaluations, String outputPath )
throws JMException, ClassNotFoundException
{
        Algorithm algorithm ;         // The algorithm to use
        Operator crossover ;         // Crossover operator
        Operator  mutation  ;         // Mutation operator
        Operator  selection ;         // Selection operator

        algorithm = new NSGAII(problem);
        QualityIndicator indicators = null;

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("archiveSize",populationSize);
        algorithm.setInputParameter("maxEvaluations",maxEvaluations);

        // Mutation and Crossover for Real codification
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
        crossover.setParameter("probability",0.9);
        crossover.setParameter("distributionIndex",20.0);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation");
        mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
        mutation.setParameter("distributionIndex",20.0);

        // Selection Operator
        selection = SelectionFactory.getSelectionOperator("BinaryTournament2") ;

        // Add the operators to the algorithm
        algorithm.addOperator("crossover",crossover);
        algorithm.addOperator("mutation",mutation);
        algorithm.addOperator("selection",selection);

        // Add the indicator object to the algorithm
        algorithm.setInputParameter("indicators", indicators) ;

        
        algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals", "0");
        // Execute the Algorithm
        long initTime = System.currentTimeMillis();


        SolutionSet population = ExecuteAndOutputPopulationToFile(algorithm, problem, outputPath, populationSize, maxEvaluations);

        return population;

}
public static SolutionSet RunAlgorithmIBEA(Problem problem, int populationSize,int maxEvaluations, String outputPath )
throws JMException, ClassNotFoundException
{
        Algorithm algorithm ;         // The algorithm to use
        Operator  crossover ;         // Crossover operator
        Operator  mutation  ;         // Mutation operator
        Operator  selection ;         // Selection operator

        algorithm = new IBEA(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize",populationSize);
        algorithm.setInputParameter("archiveSize",populationSize);
        algorithm.setInputParameter("maxEvaluations",maxEvaluations);

        // Mutation and Crossover for Real codification
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
        crossover.setParameter("probability",1.0);
        crossover.setParameter("distribuitionIndex",20.0);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation");
        mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
        mutation.setParameter("distributionIndex",20.0);

        /* Selection Operator */
        selection = new BinaryTournament(new FitnessComparator());

        // Add the operators to the algorithm
        algorithm.addOperator("crossover",crossover);
        algorithm.addOperator("mutation",mutation);
        algorithm.addOperator("selection",selection);

        SolutionSet population = ExecuteAndOutputPopulationToFile(algorithm, problem, outputPath, populationSize, maxEvaluations);

        return population;

}

public static SolutionSet RunAlgorithmSPEA2(Problem problem, int populationSize, int maxEvaluations,String outputPath )
throws JMException, ClassNotFoundException
{
        Algorithm algorithm ;         // The algorithm to use
        Operator  crossover ;         // Crossover operator
        Operator  mutation  ;         // Mutation operator
        Operator  selection ;         // Selection operator

        algorithm = new SPEA2(problem);
        QualityIndicator indicators = null;

        // Algorithm parameters
        algorithm.setInputParameter("populationSize",populationSize);
        algorithm.setInputParameter("archiveSize",populationSize);
        algorithm.setInputParameter("maxEvaluations",maxEvaluations);

        // Mutation and crossover for real codification
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
        crossover.setParameter("probability",0.9);
        crossover.setParameter("distributionIndex",20.0);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation");
        mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
        mutation.setParameter("distributionIndex",20.0);

        // Selection operator
        selection = SelectionFactory.getSelectionOperator("BinaryTournament") ;

        // Add the operators to the algorithm
        algorithm.addOperator("crossover",crossover);
        algorithm.addOperator("mutation",mutation);
        algorithm.addOperator("selection",selection);

        // Add the indicator object to the algorithm
        algorithm.setInputParameter("indicators", indicators) ;

        SolutionSet population = ExecuteAndOutputPopulationToFile(algorithm, problem, outputPath, populationSize, maxEvaluations);

        return population;

}

public static SolutionSet ExecuteAndOutputPopulationToFile(Algorithm algorithm,
        Problem problem, String outputPath, int populationSize, int maxEvaluations) throws JMException,ClassNotFoundException{

        String varFilename = outputPath + "VAR_" + algorithm.getClass().getSimpleName() + "_" + problem.getName()+ "_" + populationSize + "_" + maxEvaluations;
        String objFilename = outputPath + "OBJ_"+ algorithm.getClass().getSimpleName() + "_" + problem.getName() + "_" + populationSize + "_" + maxEvaluations;
       

        File fileObj = new File(objFilename);
        SolutionSet population = null;
        if (fileObj.exists()){
            //System.out.println("readig soltion from: " + objFilename);
            population = GetSolutionSetFromFile(objFilename);
        } else
        {
            population = algorithm.execute();
          //  System.out.println("Writing results to: " + outputPath + "[VAR/OBJ]_" + algorithm.getClass().getSimpleName() + "_" + problem.getName() + "_" + populationSize);
            population.printVariablesToFile(varFilename);
            population.printObjectivesToFile(objFilename);
        }
        
        return population;
}
 public static void main(String[] args) throws JMException, ClassNotFoundException {

   String outputPath = "outputs\\";
   int maxPopulation = 100;
   ArrayList<Problem> problems = new ArrayList<Problem>();
   problems.add(new DTLZ1("Real"));
  // problems.add(new DTLZ2("Real"));
  // problems.add(new DTLZ3("Real"));
  // problems.add(new DTLZ4("Real"));
  // problems.add(new DTLZ5("Real"));
  // problems.add(new DTLZ6("Real"));
  // problems.add(new DTLZ7("Real"));

  // System.out.println("Population;NSGAII/SPEA2;SPEA2/NSGAII");
   int runTimesForAverage = 10;

   int maxGen = 50;

   for (Problem problem : problems){
       System.out.println(";;;");
       System.out.println(problem.getName() + ";;;");
       System.out.println(";;;");
       
       for (maxPopulation=100; maxPopulation<=100; maxPopulation+= 100){
           double coverage12Avg = 0;
           double coverage21Avg = 0;
           System.out.println("\nPopulation: " + maxPopulation);
           System.out.println("-------------------------------");
            //for (int j=1; j<=runTimesForAverage; j++){
                String newOutputPath = outputPath + "_";
                SolutionSet s1 = RunAlgorithmNSGAII(problem, maxPopulation,maxPopulation*maxGen, newOutputPath);
//                SolutionSet s2 = RunAlgorithmSPEA2(problem, maxPopulation, maxPopulation*maxGen, newOutputPath);
//                SolutionSet s3 = RunAlgorithmIBEA(problem, maxPopulation,maxPopulation*maxGen, newOutputPath);

             //   double coverage12 = CoverageOfTwoSets.computeCoverage(s1, s2);
             //   double coverage21 = CoverageOfTwoSets.computeCoverage(s2, s1);

            //    coverage12Avg += coverage12;
             //   coverage21Avg += coverage21;
              //  System.out.print(j + " ");
           // }
            coverage12Avg = coverage12Avg / runTimesForAverage;
            coverage21Avg = coverage21Avg / runTimesForAverage;
            DecimalFormat myFormatter = new DecimalFormat("###.###########");
            String output1 = myFormatter.format(coverage12Avg);
            String output2 = myFormatter.format(coverage21Avg);
 
            String rez =  maxPopulation + ";" + output1 + ";" + output2 + ";";
            System.out.println(rez.replace('.', ','));
            
       }
 
   }
   
   /* 
   SolutionSet s1 = GetSolutionSetFromFile("FUN_KURSAWE_SPEA2");
   SolutionSet s2 = GetSolutionSetFromFile("FUN_KURSAWE_NSGA2");

   double coverage12 = CoverageOfTwoSets.computeCoverage(s1, s2);
   double coverage21 = CoverageOfTwoSets.computeCoverage(s2, s1);
   System.out.println("KURSAWE Test:");
   System.out.println("SPEA2 over NSGA2: " + coverage12);
   System.out.println("NSGA2 over SPEA2: " + coverage21);
   */

 }
}
