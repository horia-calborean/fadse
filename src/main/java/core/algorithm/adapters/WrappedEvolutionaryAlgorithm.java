package core.algorithm.adapters;

import core.network.ClientsRepository;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import output.application.CsvUtils;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO - SEE THE WrappedEvolutionaryAlgorithm<S, R> CLASS FROM test-jmetal-6.0-radu BRANCH

@SuppressWarnings("unchecked cast")
public class WrappedEvolutionaryAlgorithm<S,R> extends AbstractEvolutionaryAlgorithm<S, R> {
    protected AbstractEvolutionaryAlgorithm<S, R> algorithm;
    protected Map<String, Method> methodsDictionary;
    protected ClientsRepository clientsRepository;
    protected String cvsPath;

    public WrappedEvolutionaryAlgorithm(AbstractEvolutionaryAlgorithm<S, R> algorithm, String path) {
        this.cvsPath = path + "fadse.xlsx";
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
        Path pathObj = Paths.get(cvsPath);
        try {
            boolean deleted = Files.deleteIfExists(pathObj);
            Logger.getLogger(WrappedEvolutionaryAlgorithm.class.getName()).log(Level.INFO, "Deleted file: " + deleted);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        List<S> offspringPopulation;
        List<S> matingPopulation;
        population = createInitialPopulation();
        CsvUtils.writeExcel((List<? extends Solution<?>>) population, "initial pop non-evaluated", cvsPath);
        population = evaluatePopulation(population);
        clientsRepository.join();
        CsvUtils.writeExcel((List<? extends Solution<?>>) population, "initial pop evaluated", cvsPath);
        Logger.getLogger(WrappedEvolutionaryAlgorithm.class.getName()).log(Level.INFO, "Initial population evaluated");
        initProgress();
        int gen = 0;
        while (!isStoppingConditionReached()) {
            matingPopulation = selection(population);
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            clientsRepository.join();
            Logger.getLogger(WrappedEvolutionaryAlgorithm.class.getName()).log(Level.INFO, "Offsprings evaluated");
            population = replacement(population, offspringPopulation);
            // TODO - checkpoint here ?
            updateProgress();
            CsvUtils.writeExcel((List<? extends Solution<?>>) population, "pop after gen " + gen, cvsPath);
            Logger.getLogger(WrappedEvolutionaryAlgorithm.class.getName()).log(Level.INFO, "Generation " + (gen++) + " done");
        }
    }

    @Override
    public R result() {
        return (R) SolutionListUtils.getNonDominatedSolutions((List<? extends Solution<?>>)population);
    }

    @Override
    public String name() {
        try {
            return (String) this.methodsDictionary.get("name").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: name", e);
        }
    }

    @Override
    public String description() {
        try {
            return (String) this.methodsDictionary.get("description").invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: description", e);
        }
    }

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