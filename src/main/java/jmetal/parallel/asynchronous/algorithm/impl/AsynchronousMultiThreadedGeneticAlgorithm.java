package jmetal.parallel.asynchronous.algorithm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.parallel.asynchronous.multithreaded.Master;
import jmetal.parallel.asynchronous.multithreaded.Worker;
import jmetal.parallel.asynchronous.task.ParallelTask;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observable.impl.DefaultObservable;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class AsynchronousMultiThreadedGeneticAlgorithm<S extends Solution<?>>
    extends Master<ParallelTask<S>, List<S>> {
  private Problem<S> problem;
  private CrossoverOperator<S> crossover;
  private MutationOperator<S> mutation;
  private SelectionOperator<List<S>, S> selection;
  private Replacement<S> replacement;
  private Termination termination;

  private List<S> population = new ArrayList<>();
  private int populationSize;
  private int evaluations = 0;
  private long initTime;

  private Map<String, Object> attributes;
  private Observable<Map<String, Object>> observable;

  private int numberOfCores;

  public AsynchronousMultiThreadedGeneticAlgorithm(
      int numberOfCores,
      Problem<S> problem,
      int populationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation,
      SelectionOperator<List<S>, S> selection,
      Replacement<S> replacement,
      Termination termination) {
    super(numberOfCores);
    this.problem = problem;
    this.crossover = crossover;
    this.mutation = mutation;
    this.populationSize = populationSize;
    this.termination = termination;
    this.selection = selection ;
    this.replacement = replacement ;

    attributes = new HashMap<>();
    observable = new DefaultObservable<>("Observable");

    this.numberOfCores = numberOfCores;

    createWorkers(numberOfCores, problem);
  }

  private void createWorkers(int numberOfCores, Problem<S> problem) {
    IntStream.range(0, numberOfCores).forEach(i -> new Worker<>(
            (task) -> {
              problem.evaluate(task.getContents());
              return ParallelTask.create(createTaskIdentifier(), task.getContents());
            },
            pendingTaskQueue,
            completedTaskQueue).start());
  }

  private int createTaskIdentifier() {
    return JMetalRandom.getInstance().nextInt(0, 1000000000) ;
  }

  @Override
  public void initProgress() {
    attributes.put("EVALUATIONS", evaluations);
    attributes.put("POPULATION", population);
    attributes.put("COMPUTING_TIME", System.currentTimeMillis() - initTime);

    observable.setChanged();
    observable.notifyObservers(attributes);
  }

  @Override
  public void updateProgress() {
    attributes.put("EVALUATIONS", evaluations);
    attributes.put("POPULATION", population);
    attributes.put("COMPUTING_TIME", System.currentTimeMillis() - initTime);
    attributes.put("BEST_SOLUTION", population.get(0));

    observable.setChanged();
    observable.notifyObservers(attributes);
  }

  @Override
  public List<ParallelTask<S>> createInitialTasks() {
    List<S> initialPopulation = new ArrayList<>();
    List<ParallelTask<S>> initialTaskList = new ArrayList<>() ;
    IntStream.range(0, populationSize)
        .forEach(i -> initialPopulation.add(problem.createSolution()));
    initialPopulation.forEach(
        solution -> {
          int taskId = JMetalRandom.getInstance().nextInt(0, 1000);
          initialTaskList.add(ParallelTask.create(taskId, solution));
        });

    return initialTaskList ;
  }

  @Override
  public void submitInitialTasks(List<ParallelTask<S>> initialTaskList) {
    if (initialTaskList.size() >= numberOfCores) {
      initialTaskList.forEach(this::submitTask);
    } else {
      int idleWorkers = numberOfCores - initialTaskList.size();
      initialTaskList.forEach(this::submitTask);
      while (idleWorkers > 0) {
        submitTask(createNewTask());
        idleWorkers--;
      }
    }
  }

  @Override
  public void processComputedTask(ParallelTask<S> task) {
    evaluations++;
    if (population.size() < populationSize) {
      population.add(task.getContents());
    } else {
      List<S> offspringPopulation = new ArrayList<>(1);
      offspringPopulation.add(task.getContents());

      population = replacement.replace(population, offspringPopulation);
      Check.that(population.size() == populationSize, "The population size is incorrect");
    }
  }

  @Override
  public void submitTask(ParallelTask<S> task) {
    pendingTaskQueue.add(task);
  }

  @Override
  public ParallelTask<S> createNewTask() {
    if (population.size() > 2) {
      List<S> parents = new ArrayList<>(2);
      parents.add(selection.execute(population));
      parents.add(selection.execute(population));

      List<S> offspring = crossover.execute(parents);

      mutation.execute(offspring.get(0));

      return ParallelTask.create(createTaskIdentifier(), offspring.get(0));
    } else {
      return ParallelTask.create(createTaskIdentifier(), problem.createSolution());
    }
  }

  @Override
  public boolean stoppingConditionIsNotMet() {
    return !termination.isMet(attributes);
  }

  @Override
  public void run() {
    initTime = System.currentTimeMillis();
    super.run();
  }

  @Override
  public List<S> getResult() {
    return population;
  }

  public Observable<Map<String, Object>> getObservable() {
    return observable;
  }
}
