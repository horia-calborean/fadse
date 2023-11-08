package jmetal.core.util;


import java.util.ArrayList;
import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

public class AlgorithmDefaultOutputData {

  public static <S extends Solution<?>> void generateSingleObjectiveAlgorithmOutputData(S solution, long computingTime) {
    List<S> population = new ArrayList<>(1) ;
    population.add(solution) ;

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

    JMetalLogger.logger.info("Fitness: " + solution.objectives()[0]) ;
    JMetalLogger.logger.info("Solution: " + solution.objectives()[0]) ;
  }

  public static <S extends Solution<?>> void generateMultiObjectiveAlgorithmOutputData(List<S> solutionList, long computingTime) {
    new SolutionListOutput(solutionList)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
