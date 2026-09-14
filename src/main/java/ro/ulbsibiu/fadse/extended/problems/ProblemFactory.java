package ro.ulbsibiu.fadse.extended.problems;

import org.uma.jmetal.problem.Problem;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.getters.ProblemParametersGetter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class ProblemFactory {
    // Maps the short problem/simulator name (as configured in the design-space XML's
    // <simulator name="..."> attribute) to the fully qualified class actually implementing it.
    private static final Map<String, String> CLASS_NAMES = Map.of(
            "ServerSimulator", "ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator",
            "FalseSimulator", "ro.ulbsibiu.fadse.extended.problems.simulators.FalseSimulator",
            "DTLZ1", "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1"
    );

    public static <S> Problem<S> loadProblem(Environment environment) {
        String problemName = environment.getInputDocument().getSimulatorName();
        String className = CLASS_NAMES.getOrDefault(problemName, problemName);

        ProblemParametersGetter parametersGetter;
        String packageName = ProblemParametersGetter.class.getPackage().getName();

        String getterName = packageName + "." + problemName + "Getter";
        try {
            parametersGetter = (ProblemParametersGetter) Class.forName(getterName).getConstructor().newInstance();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Object[] parameters = parametersGetter.get(environment);
        Class<?>[] parameterTypes = Arrays.stream(parameters).map(Object::getClass).toArray(Class<?>[]::new);

        Problem<S> problem;

        try {
            problem = (Problem<S>) Class.forName(className).getConstructor(parameterTypes).newInstance(parameters);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return problem;
    }
}