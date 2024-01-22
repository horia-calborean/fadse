package ro.ulbsibiu.fadse.runners.jMetal;

import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.problem.ProblemFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class WrappedEvolutionaryAlgorithm<S, R> extends AbstractEvolutionaryAlgorithm<S, R>  {
    AbstractEvolutionaryAlgorithm aea;

    Dictionary<String, Method> methodsDictionary = new Hashtable<>();

    public static void main(String[] args) {
        String problemName = "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1";

        Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability,
                crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
                mutationDistributionIndex);

        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());

        var algorithm =
                new NSGAIIIBuilder<>(problem)
                        .setCrossoverOperator(crossover)
                        .setMutationOperator(mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(300)
                        .setNumberOfDivisions(12)
                        .build();

        WrappedEvolutionaryAlgorithm walgorithm = new WrappedEvolutionaryAlgorithm(algorithm);

        org.uma.jmetal.algorithm.examples.AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(walgorithm).execute();

        List<DoubleSolution> population = (List<DoubleSolution>) walgorithm.result();
        long computingTime = algorithmRunner.getComputingTime();

        new SolutionListOutput(population)
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
                .print();

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
    }


    public WrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S,R> algorithm) {
        if (algorithm == null) {
            return;
        }
        aea = algorithm;
        methodsDictionary = GetDictionary(aea);

    }

    private Dictionary<String, Method> GetDictionary(AbstractEvolutionaryAlgorithm aea) {
        Dictionary<String, Method> methods = new Hashtable<>();
        Class jmetal = aea.getClass();
        for (;;){
            jmetal = jmetal.getSuperclass();
            if (jmetal.getName().endsWith("AbstractEvolutionaryAlgorithm")) {
                break;
            }

        }

        Class current = this.getClass();

        Method[] allMethods = current.getDeclaredMethods();
        Method[] alljMetalMethods = jmetal.getDeclaredMethods();
        for (Method m : allMethods) {
            String mname = m.getName();
            if (mname.startsWith("run") || mname.startsWith("main")) {
                continue;
            }

            for (Method mj : alljMetalMethods) {
                if (mj.getName() == mname) {
                        mj.setAccessible(true);
                        methods.put(mname, mj);
                        break;
                }
            }
        }

        return methods;
    }

    @Override
    public void initProgress() {
        try {
            this.methodsDictionary.get("initProgress").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProgress() {
        try {
            this.methodsDictionary.get("updateProgress").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isStoppingConditionReached() {
        try {
            return (Boolean)this.methodsDictionary.get("isStoppingConditionReached").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<S> createInitialPopulation() {
        try {
           return (List<S>) this.methodsDictionary.get("createInitialPopulation").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        try {
           return (List<S>) this.methodsDictionary.get("evaluatePopulation").invoke(this.aea, population);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> selection(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("selection").invoke(this.aea, population);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        try {
            return (List<S>)this.methodsDictionary.get("reproduction").invoke(this.aea, population);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        try {
            return (List<S>) this.methodsDictionary.get("replacement").invoke(this.aea, population, offspringPopulation);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;
        population = createInitialPopulation();
        population = evaluatePopulation(population);
        initProgress();
        // checkpoint here
        while (!isStoppingConditionReached()) {
            matingPopulation = selection(population);
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            population = replacement(population, offspringPopulation);
            // checkpoint here
            updateProgress();
        }
    }

    @Override
    public R result() {
        this.aea.setPopulation(population);
        return (R)this.aea.result();
    }

    @Override
    public String name() {
        try {
            return (String) this.methodsDictionary.get("name").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        try {
            return (String) this.methodsDictionary.get("description").invoke(this.aea);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
