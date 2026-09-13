package ro.ulbsibiu.fadse.algorithm;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Legacy counterpart of core.algorithm.adapters.WrappedEvolutionaryAlgorithm, for the
 * ro.ulbsibiu.fadse.Boot -> AlgorithmRunner -> ServerSimulator path. It joins ServerSimulator's
 * own network tracker (ro.ulbsibiu.fadse...network.server.status.SimulationStatus) between
 * generations, instead of core.network.ClientsRepository - an unrelated tracker built for the
 * new core/input/GAP architecture that binds its own listener sockets and would otherwise
 * conflict with the ones ServerSimulator already started.
 */
@SuppressWarnings("unchecked")
public class LegacyWrappedEvolutionaryAlgorithm<S, R> extends AbstractEvolutionaryAlgorithm<S, R> {
    private final AbstractEvolutionaryAlgorithm<S, R> algorithm;
    private final Map<String, Method> methodsDictionary;
    private final Problem<?> problem;

    public LegacyWrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S, R> algorithm, Problem<?> problem) {
        this.algorithm = algorithm;
        this.problem = problem;
        this.methodsDictionary = getMethods(algorithm);
    }

    private void joinIfNeeded() {
        if (problem instanceof ServerSimulator) {
            ((ServerSimulator) problem).join();
        }
    }

    @Override
    public void run() {
        population = createInitialPopulation();
        population = evaluatePopulation(population);
        joinIfNeeded();
        initProgress();
        while (!isStoppingConditionReached()) {
            List<S> matingPopulation = selection(population);
            List<S> offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            joinIfNeeded();
            population = replacement(population, offspringPopulation);
            updateProgress();
        }
    }

    @Override
    public R result() {
        return (R) SolutionListUtils.getNonDominatedSolutions((List<? extends Solution<?>>) population);
    }

    @Override
    public String name() {
        try {
            return (String) methodsDictionary.get("name").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: name", e);
        }
    }

    @Override
    public String description() {
        try {
            return (String) methodsDictionary.get("description").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: description", e);
        }
    }

    @Override
    public List<S> createInitialPopulation() {
        try {
            return (List<S>) methodsDictionary.get("createInitialPopulation").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: createInitialPopulation", e);
        }
    }

    @Override
    public void initProgress() {
        try {
            methodsDictionary.get("initProgress").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: initProgress", e);
        }
    }

    @Override
    public List<S> selection(List<S> population) {
        try {
            return (List<S>) methodsDictionary.get("selection").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: selection", e);
        }
    }

    @Override
    public List<S> reproduction(List<S> population) {
        try {
            return (List<S>) methodsDictionary.get("reproduction").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: reproduction", e);
        }
    }

    @Override
    public List<S> evaluatePopulation(List<S> population) {
        try {
            return (List<S>) methodsDictionary.get("evaluatePopulation").invoke(algorithm, population);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: evaluatePopulation", e);
        }
    }

    @Override
    public List<S> replacement(List<S> population, List<S> offspringPopulation) {
        try {
            return (List<S>) methodsDictionary.get("replacement").invoke(algorithm, population, offspringPopulation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: replacement", e);
        }
    }

    @Override
    public void updateProgress() {
        try {
            methodsDictionary.get("updateProgress").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: updateProgress", e);
        }
    }

    @Override
    public boolean isStoppingConditionReached() {
        try {
            return (boolean) methodsDictionary.get("isStoppingConditionReached").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: isStoppingConditionReached", e);
        }
    }

    private Map<String, Method> getMethods(AbstractEvolutionaryAlgorithm<S, R> aea) {
        Map<String, Method> methods = new Hashtable<>();

        Class<?> jmetal = aea.getClass();
        while (jmetal != null && !Object.class.equals(jmetal)) {
            for (Method m : jmetal.getDeclaredMethods()) {
                String methodName = m.getName();
                if (!methodName.startsWith("run") && !methodName.startsWith("main")) {
                    m.setAccessible(true);
                    methods.putIfAbsent(methodName, m);
                }
            }
            jmetal = jmetal.getSuperclass();
        }

        return methods;
    }
}
