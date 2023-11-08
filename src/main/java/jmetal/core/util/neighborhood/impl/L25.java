package jmetal.core.util.neighborhood.impl;

import jmetal.core.solution.Solution;
import jmetal.core.util.neighborhood.util.TwoDimensionalMesh;

/**
 * Class representing neighborhoods for a solution into a list of solutions
 *
 * @author Esteban López Camacho
 */
@SuppressWarnings("serial")
public class L25<S extends Solution<?>> extends TwoDimensionalMesh<S> {
  
  private static final int [][] neighborhood = {
      {3, 0},
      {2, -1}, {2, 0}, {2, 1},
      {1, -2}, {1, -1}, {1, 0}, {1, 1}, {1, 2}, 
      {0, -3}, {0, -2}, {0, -1}, {0, 1}, {0, 2}, {0, 3},
      {-1, -2}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 2},
      {-2, -1}, {-2, 0}, {-2, 1},
      {-3, 0}
  };
  
  /**
   * Constructor.
   * Defines a neighborhood for solutionSetSize
   */
  public L25(int rows, int columns) {
    super(rows, columns, neighborhood) ;
  }
}
