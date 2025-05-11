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
    protected Map<String, Method> methodsDictionary;
    protected ClientsRepository clientsRepository;

    public WrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S, R> algorithm) {
        this.algorithm = algorithm;
        methodsDictionary = getMethods(algorithm);

        try {
            clientsRepository = ClientsRepository.getInstance(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO - Integrate the database save of an individual in this flow
    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;
        population = createInitialPopulation();
        population = evaluatePopulation(population);
        clientsRepository.join();
        initProgress();
        while (!isStoppingConditionReached()) {
            matingPopulation = selection(population);
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            clientsRepository.join();
            population = replacement(population, offspringPopulation);
            // TODO - checkpoint here ?
            updateProgress();
        }
    }

    @Override
    public R result() {
        try {
            return (R) this.methodsDictionary.get("result").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: createInitialPopulation", e);
        }
    }

    @Override
    public String name() {
        try {
            return (String) this.methodsDictionary.get("name").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: createInitialPopulation", e);
        }
    }

    @Override
    public String description() {
        try {
            return (String) this.methodsDictionary.get("description").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: createInitialPopulation", e);
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

    private Map<String, Method> getMethods(AbstractEvolutionaryAlgorithm<S,R> aea) {
        Map<String, Method> methods = new Hashtable<>();

        Class<?> jmetal = aea.getClass();
        while (jmetal != null && !WrappedEvolutionaryAlgorithm.class.equals(jmetal)) {
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