package jmetal.core.solution.sequencesolution.impl;

import java.util.HashMap;
import jmetal.core.solution.AbstractSolution;
import jmetal.core.solution.sequencesolution.SequenceSolution;

/**
 * Defines an implementation of solution representing sequences of chars.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class CharSequenceSolution extends AbstractSolution<Character> implements SequenceSolution<Character> {
  /** Constructor */
  public CharSequenceSolution(int stringLength, int numberOfObjectives) {
    super(stringLength, numberOfObjectives);

    for (int i = 0; i < stringLength; i++) {
      variables().set(i, ' ');
    }
  }

  /** Copy Constructor */
  public CharSequenceSolution(CharSequenceSolution solution) {
    super(solution.getLength(), solution.objectives().length);

    for (int i = 0; i < objectives().length; i++) {
      objectives()[i] = solution.objectives()[i];
    }

    for (int i = 0; i < variables().size(); i++) {
      variables().set(i, solution.variables().get(i));
    }

    for (int i = 0; i < constraints().length; i++) {
      constraints()[i] =  solution.constraints()[i];
    }

    attributes = new HashMap<>(solution.attributes);
  }

  @Override
  public CharSequenceSolution copy() {
    return new CharSequenceSolution(this);
  }

  @Override
  public int getLength() {
    return variables().size();
  }
}
