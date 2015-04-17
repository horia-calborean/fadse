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
public class TestTipper {

    public static void main(String[] args) throws Exception {
        // Load from 'FCL' file
        String fileName = "false.fcl";
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
            fis.setVariable("l1size", 58);
        } catch (java.lang.RuntimeException e) {
        }
        try {
            fis.setVariable("l1assoc", 4);
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
        fis.getVariable("outl2size").chartDefuzzifier(true);
        System.out.println(fis.getVariable("outl2size").getDefaultValue());
        System.out.println(fis.getVariable("outl2size").getValue());
        System.out.println(fis.getVariable("outl2size").defuzzify());
        System.out.println(fis.getVariable("outl2size").getUniverseMax());
        Iterator<String> i = fis.getVariable("outl2size").iteratorLinguisticTermNames();
        while (i.hasNext()) {
            String term = i.next();
            System.out.println("Memebership to " + term + ": " + fis.getVariable("outl2size").getMembership(term));
        }
        //new RuleActivationMethodProduct();
        try {
            fis.setVariable("l1size", 64);
        } catch (java.lang.RuntimeException e) {
        }
        try {
            fis.setVariable("l1assoc", 4);
        } catch (java.lang.RuntimeException e) {
        }
        fis.setVariable("l2size", fis.getVariable("outl2size").defuzzify());
        fis.evaluate();
        fis.getVariable("outl3size").chartDefuzzifier(true);
        System.out.println(fis.getVariable("outl3size").defuzzify());
        i = fis.getVariable("outl3size").iteratorLinguisticTermNames();
        while (i.hasNext()) {
            String term = i.next();
            System.out.println("Memebership to " + term + ": " + fis.getVariable("outl3size").getMembership(term));
        }
        // Print ruleSet
        //System.out.println(fis);
    }
}
