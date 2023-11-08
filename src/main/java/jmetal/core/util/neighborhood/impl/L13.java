package jmetal.core.util.neighborhood.impl;

import jmetal.core.solution.Solution;
import jmetal.core.util.neighborhood.util.TwoDimensionalMesh;

/**
 * Class defining an L9 neighborhood of a solution belonging to a list of solutions which is
 * structured as a bi-dimensional mesh. The neighbors is illustrated as follows:
 *
 *              *
 *            * * *
 *          * * o * *
 *            * * *
 *              *
 *
 * @author Esteban López Camacho
 */
@SuppressWarnings("serial")
public class L13<S extends Solution<?>> extends TwoDimensionalMesh<S> {
  
  private static final int [][] neighborhood = {
      {2, 0},
      {1, -1}, {1, 0}, {1, 1},
      {0, -2}, {0, -1}, {0, 1}, {0, 2},
      {-1, -1}, {-1, 0}, {-1, 1},
      {-2, 0}
  };

  /**
   * Constructor.
   * Defines a neighborhood for a solution set of rows x columns solutions
   */
  public L13(int rows, int columns) {
    super(rows, columns, neighborhood) ;
  }
}
