package core.problem.application;

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

    @SuppressWarnings("unchecked")
    public static <S> Problem<S> createProblem(String simpleClassName, Object... args) {
        Class<?>[] argTypes = Arrays.stream(args)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);

        for (String pkg : SEARCH_PACKAGES) {
            String fullClassName = pkg + "." + simpleClassName;
            try {
                Class<?> clazz = Class.forName(fullClassName);
                Constructor<?> constructor = clazz.getConstructor(argTypes);
                return (Problem<S>) constructor.newInstance(args);
            } catch (ClassNotFoundException ignored) {
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Constructor found for class " + simpleClassName + " but doesn't match args", e);
            } catch (Exception e) {
                throw new RuntimeException("Error creating problem instance for class " + simpleClassName, e);
            }
        }

        throw new RuntimeException("Problem class not found in known packages: " + simpleClassName);
    }
}