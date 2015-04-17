/**
 * PolynomialBitFlipMutation.java
 * @author Antonio J. Nebro
 * @version 10
 * 
 * This class implements a mutation operator to be applied to 
 * ArrayRealAndBinarySolutionType objects. The mutation combines polynomial 
 * and bitflip mutation
 */
package jmetal.base.operator.mutation;

import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.variable.ArrayReal;
import jmetal.base.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;

public class PolynomialBitFlipMutation extends Mutation {
	/**
	 * ETA_M_DEFAULT_ defines a default index for mutation
	 */
	public static final double ETA_M_DEFAULT_ = 20.0;

	/**
	 * eta_c stores the index for mutation to use
	 */
	public double eta_m_ = ETA_M_DEFAULT_;

  /**
   * ARRAY_REAL_AND_BINARY_SOLUTION represents class jmetal.base.solutionType.ArrayRealAndBinarySolutionType
   */
  private static Class ARRAY_REAL_AND_BINARY_SOLUTION ; 

  /**
   * Constructor
   */
  public PolynomialBitFlipMutation() {
    try {
    	ARRAY_REAL_AND_BINARY_SOLUTION = Class.forName("jmetal.problems.bci5.ArrayRealAndBinarySolutionType") ;
    } catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } // catch
  } // Constructor
	
	@Override
  public Object execute(Object object) throws JMException {
		Solution solution = (Solution)object;

		if (solution.getType().getClass() != ARRAY_REAL_AND_BINARY_SOLUTION) {
			Configuration.logger_.severe("PolynomialBitFlipMutation.execute: the solution " +
					"type " + solution.getType() + " is not allowed with this operator");

			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;
		} // if 
		
		Double realProbability = (Double)getParameter("realProbability");       
		if (realProbability == null){
			Configuration.logger_.severe("PolynomialBitFlipMutation.execute: probability of the real component" +
			"not specified");
			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;  
		}

		Double binaryProbability = (Double)getParameter("binaryProbability");       
		if (binaryProbability == null){
			Configuration.logger_.severe("PolynomialBitFlipMutation.execute: probability of the binary component" +
			"not specified");
			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;  
		}

		Double distributionIndex = (Double)getParameter("distributionIndex");
		if (distributionIndex != null) {
			eta_m_ = distributionIndex ;
		} // if

		//System.out.println("RP: " + realProbability + "\tBP: " + binaryProbability) ;
		doMutation(realProbability, binaryProbability,solution);
		return solution;
  } // execute

	/**
	 * doMutation method
	 * @param realProbability
	 * @param binaryProbability
	 * @param solution
	 * @throws JMException
	 */
	public void doMutation(Double realProbability, Double binaryProbability, Solution solution) throws JMException {   
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		
		XReal x = new XReal(solution) ;
		
		Binary binaryVariable = (Binary)solution.getDecisionVariables()[1] ;
		
		// Polynomial mutation applied to the array real
		for (int var=0; var < x.size(); var++) {	
			if (PseudoRandom.randDouble() <= realProbability) {
				y      = x.getValue(var);
				yl     = x.getLowerBound(var);                
				yu     = x.getUpperBound(var);
				delta1 = (y-yl)/(yu-yl);
				delta2 = (yu-y)/(yu-yl);
				rnd = PseudoRandom.randDouble();
				mut_pow = 1.0/(eta_m_+1.0);
				if (rnd <= 0.5)
				{
					xy     = 1.0-delta1;
					val    = 2.0*rnd+(1.0-2.0*rnd)*(Math.pow(xy,(eta_m_+1.0)));
					deltaq =  java.lang.Math.pow(val,mut_pow) - 1.0;
				}
				else
				{
					xy = 1.0-delta2;
					val = 2.0*(1.0-rnd)+2.0*(rnd-0.5)*(java.lang.Math.pow(xy,(eta_m_+1.0)));
					deltaq = 1.0 - (java.lang.Math.pow(val,mut_pow));
				}
				y = y + deltaq*(yu-yl);
				if (y<yl)
					y = yl;
				if (y>yu)
					y = yu;
				x.setValue(var, y);                           
			} // if		
		} // for

		// BitFlip mutation applied to the binary part
		for (int i = 0; i < binaryVariable.getNumberOfBits(); i++)
			if (PseudoRandom.randDouble() < binaryProbability) 
				binaryVariable.bits_.flip(i) ;
	} // doMutation
} // PolynomialBitFlipMutation

