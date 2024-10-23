package wrappers;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import simulation.AlgorithmOperator;
import simulation.ListOfSimulationParameters;
import simulation.SimulationParameter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class WrappedEvolutionaryAlgorithm<S, R> extends AbstractEvolutionaryAlgorithm<S, R> {
    protected AbstractEvolutionaryAlgorithm<S, R> evolutionaryAlgorithm;
    protected Dictionary<String, Method> methodsDictionary;
    protected Dictionary<AlgorithmOperator, Class> operatorsTypesDictionary;
    protected ListOfSimulationParameters simulationParameters;

    public WrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S, R> algorithm,
                                        ListOfSimulationParameters simulationParameters) {
        if (algorithm == null) {
            return;
        }

        evolutionaryAlgorithm = algorithm;
        methodsDictionary = new Hashtable<>();
        methodsDictionary = getMethodsHashTable(evolutionaryAlgorithm);
        operatorsTypesDictionary = getOperatorsHashTable(evolutionaryAlgorithm);
        this.simulationParameters = simulationParameters;

    }

    protected Dictionary<String, Method> getMethodsHashTable(AbstractEvolutionaryAlgorithm<S, R> algorithm) {
        Dictionary<String, Method> methodsHashTable = new Hashtable<>();

        Class algorithmBaseClass = algorithm.getClass();

        do {
            algorithmBaseClass = algorithmBaseClass.getSuperclass();
        } while (!algorithmBaseClass.getName().endsWith("AbstractEvolutionaryAlgorithm"));

        Class thisClass = this.getClass();

        Method[] algorithmMethods = algorithmBaseClass.getDeclaredMethods();
        Method[] thisMethods = thisClass.getDeclaredMethods();

        for (Method thisMethod : thisMethods) {
            String thisMethodName = thisMethod.getName();

            if (thisMethodName.startsWith("run") || thisMethodName.startsWith("main")) {
                continue;
            }

            for (Method algorithmMethod : algorithmMethods) {
                String algorithmMethodName = algorithmMethod.getName();

                if (algorithmMethodName.equals(thisMethodName)) {
                    algorithmMethod.setAccessible(true);
                    methodsHashTable.put(thisMethodName, algorithmMethod);
                    break;
                }
            }
        }

        return methodsHashTable;
    }

    protected Dictionary<AlgorithmOperator, Class> getOperatorsHashTable(AbstractEvolutionaryAlgorithm<S, R> algorithm) {
        Dictionary<AlgorithmOperator, Class> methodsHashTable = new Hashtable<>();

        Class algorithmBaseClass = algorithm.getClass();

        do {
            algorithmBaseClass = algorithmBaseClass.getSuperclass();
        } while (!algorithmBaseClass.getName().endsWith("AbstractGeneticAlgorithm"));

        Field[] algorithmFields = algorithmBaseClass.getDeclaredFields();

        for (Field field : algorithmFields) {
            String fieldName = field.getName();

            if (fieldName.contains("crossover")) {
                methodsHashTable.put(AlgorithmOperator.CROSSOVER, field.getType());
            } else if (fieldName.contains("mutation")) {
                methodsHashTable.put(AlgorithmOperator.MUTATION, field.getType());
            } else if (fieldName.contains("selection")) {
                methodsHashTable.put(AlgorithmOperator.SELECTION, field.getType());
            }
        }

        return methodsHashTable;
    }

    @Override
    public void run() {
        // TODO - get input parameters such as: outputEveryPopulation, outputPath, checkPointFileParams etc.
        // TODO - add check for mutation operators - In jmetal 6.6 they are declared in AbstractGeneticAlgorithm and initialized in NSGA-II

        boolean outputEveryPopulation = simulationParameters.contains(SimulationParameter.OUTPUT_EVERY_POPULATION_PATH);
        String outputPath = outputEveryPopulation ? (String) simulationParameters.get(SimulationParameter.OUTPUT_EVERY_POPULATION_PATH) : "";

        Type mutationOperatorType = operatorsTypesDictionary.get(AlgorithmOperator.MUTATION);

        if (mutationOperatorType instanceof BitFlipMutationFuzzyVirtualParameters) {
            try {
                ((BitFlipMutationFuzzyVirtualParameters) mutationOperator).x = ((Integer) getInputParameter("initialGeneration")).intValue();
                System.out.println("Initial generation is: " + ((BitFlipMutationFuzzyVirtualParameters) mutationOperator).x);
            } catch (Exception e) {
                System.out.println("NSGA-II: initial generations start was not set caused by: " + e.getMessage());
            }
        }

        List<S> offspringPopulation;
        List<S> matingPopulation;
//        if (chepointing == ok && file == exists) {
//            population = readCheckpointingFile();
//        } else {
//            population = createInitialPopulation();
//        }
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
    protected void initProgress() {
        try {
            this.methodsDictionary.get("initProgress").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateProgress() {
        try {
            this.methodsDictionary.get("updateProgress").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        try {
            return (Boolean) this.methodsDictionary.get("isStoppingConditionReached").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> createInitialPopulation() {
        try {
            return (List<S>) this.methodsDictionary.get("createInitialPopulation").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("evaluatePopulation").invoke(evolutionaryAlgorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> selection(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("selection").invoke(evolutionaryAlgorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("reproduction").invoke(evolutionaryAlgorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        try {
            return (List<S>) this.methodsDictionary.get("replacement").invoke(evolutionaryAlgorithm, population, offspringPopulation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public R result() {
        evolutionaryAlgorithm.setPopulation(population);
        return evolutionaryAlgorithm.result();
    }

    @Override
    public String name() {
        try {
            return (String) this.methodsDictionary.get("name").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        try {
            return (String) this.methodsDictionary.get("description").invoke(evolutionaryAlgorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}