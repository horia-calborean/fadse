package jmetal.problem.singleobjective;

import jmetal.core.problem.sequenceproblem.impl.CharSequenceProblem;
import jmetal.core.solution.sequencesolution.impl.CharSequenceSolution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.JMetalRandom;

/** This problem consists in finding a string matching a target string. */
@SuppressWarnings("serial")
public class StringMatching extends CharSequenceProblem {
  private String targetString;
  private final char[] alphabet;

  public StringMatching(String targetString, String alphabet) {
    this.targetString = targetString;
    this.alphabet = alphabet.toCharArray();
  }

  @Override
  public int numberOfVariables() {
    return targetString.length() ;
  }

  @Override
  public int numberOfObjectives() {
    return 1 ;
  }

  @Override
  public int numberOfConstraints() {
    return 0 ;
  }

  @Override
  public String name() {
    return "String Match" ;
  }
  public StringMatching(String targetString) {
    this(
        targetString,
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890, .-;:_!\"#%&/()=?@${[]}");
  }

  @Override
  public int length() {
    return targetString.length();
  }

  @Override
  public CharSequenceSolution evaluate(CharSequenceSolution solution) {
    Check.that(solution.getLength() == targetString.length(), "The solution has an invalid length");
    int counter = 0;

    for (int i = 0; i < targetString.length(); i++) {
      if (targetString.charAt(i) != solution.variables().get(i)) {
        counter++;
        // counter += Math.abs(targetString.charAt(i) - solution.variables().get(i)) ;
      }
    }

    solution.objectives()[0] = counter;

    return solution ;
  }

  @Override
  public CharSequenceSolution createSolution() {
    CharSequenceSolution solution = new CharSequenceSolution(targetString.length(), numberOfObjectives()) ;
    for (int i = 0 ; i < targetString.length(); i++) {
      solution.variables().set(i, alphabet[JMetalRandom.getInstance().nextInt(0, alphabet.length-1)]);
    }

    return solution ;
  }

  public char[] getAlphabet() {
    return alphabet;
  }
}
