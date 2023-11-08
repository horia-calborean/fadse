package jmetal.auto.parameter;

import jmetal.core.util.errorchecking.Check;

public class PositiveIntegerValue extends Parameter<Integer> {

  public PositiveIntegerValue(String name) {
    super(name);
  }

  @Override
  public void check() {
    Check.that(value() > 0, "The " + name() + " value " + value() + " " +
        "cannot not be <= 0");
  }

  @Override
  public Parameter<Integer> parse(String[] arguments) {
    return super.parse(Integer::parseInt, arguments);
  }
}
