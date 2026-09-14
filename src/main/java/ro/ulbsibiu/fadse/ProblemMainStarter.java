/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 *
 * @author Radu
 */
public class ProblemMainStarter {
    public static void main(String[] args) throws IllegalArgumentException {
        LinkedList<String> problems = new LinkedList<String>();
        problems.add("smpso.SMPSO_main");
        problems.add("spea2.SPEA2_main");
        problems.add("nsgaII.NSGAII_main");
        problems.add("cnsgaII.CNSGAII_main");
        problems.add("mochc.MOCHC_main");
        
        for(String problem : problems){
           	try {
	    Class<?> c = Class.forName("jmetal.metaheuristics."+problem);
	    Class[] argTypes = new Class[] { String[].class };
	    Method main = c.getDeclaredMethod("main", argTypes);
  	    String[] mainArgs = new String[]{"DTLZ1"};
	    System.out.format("invoking %s.main()%n", c.getName());
	    main.invoke(null, (Object)mainArgs);
        // production code should handle these exceptions more gracefully
	} catch (ClassNotFoundException x) {
	    x.printStackTrace();
	} catch (NoSuchMethodException x) {
	    x.printStackTrace();
	} catch (IllegalAccessException x) {
	    x.printStackTrace();
	} catch (InvocationTargetException x) {
	    x.printStackTrace();
	}    
        }
    }
}
