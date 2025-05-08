package core.problem.application;

import input.model.InputData;
import org.uma.jmetal.problem.Problem;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked cast")
public class ProblemFactory {
    private static final List<String> SEARCH_PACKAGES = Arrays.asList(
            "core.problem.adapters",
            "org.uma.jmetal.problem.multiobjective.dtlz"
    );

    private static String getClassName(String name) {
        if("GAP".equalsIgnoreCase(name)) {
            return "GAPProblem";
        }

        throw new RuntimeException("Class name not supported!");
    }

    @SuppressWarnings("unchecked")
    public static <S> Problem<S> createProblem(String simpleClassName, InputData inputData) {
        String className = getClassName(simpleClassName);

        for (String pkg : SEARCH_PACKAGES) {
            String fullClassName = pkg + "." + className;
            try {
                Class<?> clazz = Class.forName(fullClassName);
                Constructor<?> constructor = clazz.getConstructor(InputData.class);
                return (Problem<S>) constructor.newInstance(inputData);
            } catch (ClassNotFoundException ignored) {
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Constructor found for class " + className + " but doesn't match args", e);
            } catch (Exception e) {
                throw new RuntimeException("Error creating problem instance for class " + className, e);
            }
        }

        throw new RuntimeException("Problem class not found in known packages: " + className);
    }
}