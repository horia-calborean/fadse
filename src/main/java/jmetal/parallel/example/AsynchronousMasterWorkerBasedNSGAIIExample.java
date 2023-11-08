package jmetal.parallel.example;

import static java.lang.Math.sin;

import java.util.List;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.parallel.asynchronous.algorithm.impl.AsynchronousMultiThreadedNSGAII;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.RunTimeChartObserver;

public class AsynchronousMasterWorkerBasedNSGAIIExample {
  public static void main(String[] args) {
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    int populationSize = 100;
    int maxEvaluations = 25000;
    int numberOfCores = 32;

    DoubleProblem problem = new ZDT1() {
      @Override
      public DoubleSolution evaluate(DoubleSolution solution) {
        super.evaluate(solution);
        computingDelay();

        return solution;
      }

      private void computingDelay() {
        for (long i = 0; i < 1000; i++)
          for (long j = 0; j < 10000; j++) {
            double a = sin(i) * Math.cos(j);
          }
      }
    };

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    long initTime = System.currentTimeMillis();

    AsynchronousMultiThreadedNSGAII<DoubleSolution> nsgaii =
            new AsynchronousMultiThreadedNSGAII<DoubleSolution>(
                    numberOfCores, problem, populationSize, crossover, mutation, new TerminationByEvaluations(maxEvaluations));


    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
            new RunTimeChartObserver<>(
                    "NSGA-II",
                    80, 10, "resources/referenceFrontsCSV/ZDT1.csv");

    nsgaii.getObservable().register(runTimeChartObserver);

    nsgaii.run();

    long endTime = System.currentTimeMillis();

    List<DoubleSolution> resultList = nsgaii.getResult();

    JMetalLogger.logger.info("Computing time: " + (endTime - initTime));
    new SolutionListOutput(resultList)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();
    System.exit(0);
  }
}
