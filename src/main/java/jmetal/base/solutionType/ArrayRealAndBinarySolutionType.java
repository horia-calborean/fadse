/**
 * ArrayRealAndBinarySolutionType
 *
 * @author Antonio J. Nebro
 * @version 1.0
 * 
 * Class representing the solution type of solutions composed of array of reals 
 * and a binary string.
 * ASSUMTIONs:
 * - The numberOfVariables_ field in class Problem must contain the number
 *   of real variables. This field is used to apply real operators (e.g., 
 *   mutation probability)
 * - The upperLimit_ and lowerLimit_ arrays must have the length indicated
 *   by numberOfVaribles_.
 */
package jmetal.base.solutionType;

import jmetal.base.Problem;
import jmetal.base.SolutionType;
import jmetal.base.Variable;
import jmetal.base.variable.ArrayReal;
import jmetal.base.variable.Binary;

public class ArrayRealAndBinarySolutionType extends SolutionType {

	private int binaryStringLength_ ;
	private int numberOfRealVariables_ ;
	/**
	 * Constructor
	 * @param problem
	 * @param realVariables Number of real variables
	 * @param realVariables Number of real variables
	 * @throws ClassNotFoundException 
	 */
	public ArrayRealAndBinarySolutionType(Problem problem, int realVariables, int binaryStringLength) throws ClassNotFoundException {
		super(problem) ;
		problem.variableType_ = new Class[problem.getNumberOfVariables()];
		binaryStringLength_    = binaryStringLength ;
		numberOfRealVariables_ = realVariables ;

		problem.setSolutionType(this) ;
		// Initializing the types of the variables
	  problem.variableType_[0] = Class.forName("jmetal.base.variable.ArrayReal") ; 
	  problem.variableType_[1] = Class.forName("jmetal.base.variable.Binary") ;
	  
	} // Constructor

	/**
	 * Creates the variables of the solution
	 * @param decisionVariables
	 * @throws ClassNotFoundException 
	 */
	public Variable[] createVariables() throws ClassNotFoundException {
		Variable [] variables = new Variable[2];

    variables[0] = new ArrayReal(numberOfRealVariables_, problem_);
    variables[1] = new Binary(binaryStringLength_); 
    return variables ;
	} // createVariables
} // ArrayRealAndBinarySolutionType

