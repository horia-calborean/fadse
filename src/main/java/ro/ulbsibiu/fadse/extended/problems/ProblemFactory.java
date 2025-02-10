package ro.ulbsibiu.fadse.extended.problems;

import org.uma.jmetal.problem.Problem;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.getters.ProblemParametersGetter;

import java.lang.reflect.InvocationTargetException;

public class ProblemFactory {
    public static <S> Problem<S> loadProblem(Environment environment) {
        // TODO - It might be more useful to create a look-up table of pairs e.g. <ProblemName.DTZL1, "DTLZ1">
        String problemName = environment.getInputDocument().getSimulatorName();

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

        Problem<S> problem;

        try {
            problem = (Problem<S>) Class.forName(problemName).getConstructor().newInstance(parameters);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return problem;
    }
}