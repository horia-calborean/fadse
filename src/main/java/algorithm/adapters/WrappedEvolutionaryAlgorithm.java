package algorithm.adapters;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

// TODO - SEE THE WrappedEvolutionaryAlgorithm<S, R> CLASS FROM test-jmetal-6.0-radu BRANCH

@SuppressWarnings("unchecked cast")
public abstract class WrappedEvolutionaryAlgorithm<S,R> extends AbstractEvolutionaryAlgorithm<S, R> {
    protected AbstractEvolutionaryAlgorithm<S, R> algorithm;
    Dictionary<String, Method> methodsDictionary;

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
            throw new RuntimeException(e);
        }
    }

    private Dictionary<String, Method> getMethods(AbstractEvolutionaryAlgorithm<S,R> aea) {
        Dictionary<String, Method> methods = new Hashtable<>();
        Class jmetal = aea.getClass();
        do {
            jmetal = jmetal.getSuperclass();

        } while (!jmetal.getName().endsWith("AbstractEvolutionaryAlgorithm"));

        Class current = this.getClass();

        Method[] allMethods = current.getDeclaredMethods();
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