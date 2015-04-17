package ro.ulbsibiu.fadse;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Horia
 */
import java.util.Iterator;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethod;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethodMin;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethodProduct;

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class TestSimpleCache {

    public static void main(String[] args) throws Exception {
    //test commit directly from website
        // Load from 'FCL' file
        String fileName = "simple.fcl";
        FIS fis = FIS.load(fileName, true);
        // Error while loading?
        if (fis == null) {
            System.err.println("Can't load file: '"
                    + fileName + "'");
            return;
        }

        // Show
        fis.chart();

        // Set inputs
        try {
            fis.setVariable("l1size", 32);
        } catch (java.lang.RuntimeException e) {
        }
        try {
            fis.setVariable("tech", 110);
        } catch (java.lang.RuntimeException e) {
        }
//        try {
//            fis.setVariable("l2size", 7);
//        } catch (java.lang.RuntimeException e) {
//            e.printStackTrace();
//        }
        // Evaluate
        fis.evaluate();

        // Show output variable's chart
        fis.getVariable("l2size").chartDefuzzifier(true);

        // Print ruleSet
        //System.out.println(fis);
    }
}
