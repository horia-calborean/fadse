package core.algorithm.adapters;

import core.network.ClientsRepository;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

// TODO - SEE THE WrappedEvolutionaryAlgorithm<S, R> CLASS FROM test-jmetal-6.0-radu BRANCH

@SuppressWarnings("unchecked cast")
public class WrappedEvolutionaryAlgorithm<S,R> extends AbstractEvolutionaryAlgorithm<S, R> {
    protected AbstractEvolutionaryAlgorithm<S, R> algorithm;
    Map<String, Method> methodsDictionary;

    public WrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S, R> algorithm) {
        this.algorithm = algorithm;
        methodsDictionary = getMethods(algorithm);
    }

    // TODO - Integrate the database save of an individual in this flow
    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;
        population = createInitialPopulation();
        try {
            ClientsRepository.getInstance(null).join();
        } catch (Exception e) {
            throw new RuntimeException("InputData is null: ", e);
        }
        population = evaluatePopulation(population);
        initProgress();
        // TODO - checkpoint here ?
        while (!isStoppingConditionReached()) {
            matingPopulation = selection(population);
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            population = replacement(population, offspringPopulation);
            // TODO - checkpoint here ?
            updateProgress();
        }
    }

    // TODO - DO THE SAME WITH THE REST OF THE METHODSs
    @Override
    public List<S> createInitialPopulation() {
        try {
            return (List<S>) this.methodsDictionary.get("createInitialPopulation").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: createInitialPopulation", e);
        }
    }

    @Override
    public void initProgress() {
        try {
            this.methodsDictionary.get("initProgress").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: initProgress" +  e);
        }
    }

    @Override
    public List<S> selection(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("selection").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: selection", e);
        }
    }

    @Override
    public List<S> reproduction(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("reproduction").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: reproduction", e);
        }
    }

    @Override
    public List<S> evaluatePopulation(List<S> population) {
        try {
            return (List<S>) this.methodsDictionary.get("evaluatePopulation").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: evaluatePopulation", e);
        }
    }

    @Override
    public List<S> replacement(List<S> population, List<S> offspringPopulation) {
        try {
            return (List<S>) this.methodsDictionary.get("replacement").invoke(algorithm, population, offspringPopulation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: replacement", e);
        }
    }

    @Override
    public void updateProgress() {
        try {
            this.methodsDictionary.get("updateProgress").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: updateProgress", e);
        }
    }

    @Override
    public boolean isStoppingConditionReached() {
        try {
            return (boolean) this.methodsDictionary.get("isStoppingConditionReached").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: isStoppingConditionReached", e);
        }
    }

    @Override
    public R result(){
        try {
            return (R) this.methodsDictionary.get("result").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: result", e);
        }
    }

    @Override
    public String name(){
        try {
            return (String) this.methodsDictionary.get("name").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: name", e);
        }
    }

    @Override
    public String description(){
        try {
            return (String) this.methodsDictionary.get("description").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: description", e);
        }
    }

    private Map<String, Method> getMethods(AbstractEvolutionaryAlgorithm<S,R> aea) {
        Map<String, Method> methods = new Hashtable<>();
//        Class jmetal = aea.getClass();
//        do {
//            jmetal = jmetal.getSuperclass();
//
//        } while (!jmetal.getName().endsWith("AbstractEvolutionaryAlgorithm"));
//
//        Class current = this.getClass();
//        Method[] allMethods = current.getMethods();

        Class<?> jmetal = AbstractEvolutionaryAlgorithm.class;
        Method[] allMethods = WrappedEvolutionaryAlgorithm.class.getDeclaredMethods();
        Method[] alljMetalMethods = jmetal.getDeclaredMethods();
        for (Method m : allMethods) {
            String methodName = m.getName();
            if (methodName.startsWith("run") || methodName.startsWith("main")) {
                continue;
            }

            for (Method mj : alljMetalMethods) {
                if (mj.getName().equals(methodName)) {
                    mj.setAccessible(true);
                    methods.put(methodName, mj);
                    break;
                }
            }
        }

        return methods;
    }
}