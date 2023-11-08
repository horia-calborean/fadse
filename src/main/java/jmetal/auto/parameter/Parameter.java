package jmetal.auto.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import jmetal.auto.parameter.catalogue.CrossoverParameter;
import jmetal.auto.parameter.catalogue.MutationParameter;
import jmetal.core.util.errorchecking.Check;

/**
 * Abstract class representing generic parameters. Any parameter has a name and a value that is
 * assigned by parsing an array which contains sequences of pairs [key, value], as for example:,
 * ["--populationSize", "100", "--offspringPopulationSize", "100", "--createInitialSolutions", "random"]
 *
 * Every parameter has a {@link Parameter#name} (such as "populationSize" or "offpspringPopulationSize")
 * and a {@link Parameter#value} that is obtained after invoking the {@link Parameter#parse(String[])} and
 * {@link Parameter#check()} methods.
 *
 * Parameters can be seen a factories of any kind of objects, from single values (e.g., {@link RealParameter}
 * to genetic operators (e.g., {@link MutationParameter}).
 *
 * A parameter can contain other parameters, so we define three different types of associations
 * between them. We illustrate the associations by using the {@link CrossoverParameter} as an example:
 * - global parameter: any crossover has a probability parameter
 * - specific parameter: a SBX crossover has a distribution index as specific parameter
 * - non-configurable parameter: constant parameters needed by a particular parameter
 *
 * The {@Parameter} class provides methods for setting and getting these sup-parameters:
 * - {@link Parameter#addGlobalParameter(Parameter)}
 * - {@link Parameter#addSpecificParameter(String, Parameter)}}
 * - {@link Parameter#addNonConfigurableParameter(String, Object)}
 * - {@link Parameter#globalParameters()}
 * - {@link Parameter#specificParameters()}
 * - {@link Parameter#getNonConfigurableParameter(String)}
 * - {@link Parameter#findGlobalParameter(String)}
 * - {@link Parameter#findSpecificParameter(String)}
 *
 * @author Antonio J. Nebro
 * @param <T> Type of the parameter
 */
public abstract class Parameter<T> {
  private T value;
  private final String name;
  private final List<Pair<String, Parameter<?>>> specificParameters = new ArrayList<>() ;
  private final List<Parameter<?>> globalParameters = new ArrayList<>();
  private final Map<String, Object> nonConfigurableParameters = new HashMap<>();

  protected Parameter(String name) {
    this.name = name ;
  }

  private T on(String key, String[] args, Function<String, T> parser) {
    return parser.apply(retrieve(args, key));
  }

  private String retrieve(String[] args, String key) {
    int index = Arrays.asList(args).indexOf(key);
    Check.that(index != -1 && index != args.length - 1, "Missing parameter: " + key);
    return args[index + 1];
  }

  public abstract void check();

  public abstract Parameter<T> parse(String[] arguments) ;

  /**
   * Parsing a parameter implies to parse and check its global and specific parameters
   *
   * @param parseFunction
   * @return
   */
  public Parameter<T> parse(Function<String, T> parseFunction, String[] args) {
    value(on("--" + name(), args, parseFunction));

    for (Parameter<?> parameter : globalParameters()) {
      parameter.parse(args).check();
    }

    specificParameters()
        .forEach(
            pair -> {
              if (pair.getKey().equals(value())) {
                pair.getValue().parse(args).check();
              }
            });

    return this;
  }

  public String name() {
    return name;
  }

  public List<Pair<String, Parameter<?>>> specificParameters() {
    return specificParameters;
  }

  public void addSpecificParameter(String dependsOn, Parameter<?> parameter) {
    specificParameters.add(new ImmutablePair<>(dependsOn, parameter));
  }

  public List<Parameter<?>> globalParameters() {
    return globalParameters;
  }

  public void addGlobalParameter(Parameter<?> parameter) {
    globalParameters.add(parameter);
  }

  public void addNonConfigurableParameter(String parameterName, Object value) {
    nonConfigurableParameters.put(parameterName, value) ;
  }

  public Object getNonConfigurableParameter(String parameterName) {
    return nonConfigurableParameters.get(parameterName) ;
  }

  public  Map<String, Object> nonConfigurableParameters() {
    return nonConfigurableParameters ;
  }

  public T value() {
    return value;
  }

  public void value(T value) {
    this.value = value;
  }

  public Parameter<?> findGlobalParameter(String parameterName) {
    return globalParameters().stream()
        .filter(parameter -> parameter.name().equals(parameterName))
        .findFirst()
        .orElse(null);
  }

  public Parameter<?> findSpecificParameter(String parameterName) {
    return Objects.requireNonNull(specificParameters().stream()
            .filter(pair -> pair.getRight().name().equals(parameterName))
            .findFirst()
            .orElse(null))
        .getValue();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Name: " + name() + ": " + "Value: " + value());
    if (!globalParameters.isEmpty()) {
      result.append("\n\t");
      for (Parameter<?> parameter : globalParameters) {
        result.append(" \n -> ").append(parameter.toString());
      }
    }
    if (!specificParameters.isEmpty()) {
      result.append("\n\t");

      for (Pair<String, Parameter<?>> parameter : specificParameters) {
        result.append(" \n -> ").append(parameter.toString());
      }
    }
    return result.toString();
  }
}
