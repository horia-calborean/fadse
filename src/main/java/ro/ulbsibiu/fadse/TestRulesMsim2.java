package ro.ulbsibiu.fadse;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Horia
 */
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierRandom;
import org.jfree.chart.JFreeChart;

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class TestRulesMsim2 {

    public static void main(String[] args) throws Exception {
        // Load from 'FCL' file
        String fileName = "msim2.fcl";
        FIS fis = FIS.load(fileName, true);
        // Error while loading?
        if (fis == null) {
            System.err.println("Can't load file: '"
                    + fileName + "'");
            return;
        }
        fis.getVariable("outlvpt_size").setDefuzzifier(new DefuzzifierRandom(fis.getVariable("outlvpt_size"),0.5));

        // Show 
        fis.chart();
/*
     dl1_nsets: REAL;
    dl1_bsize: REAL;
    lvpt_size: REAL;

    issue_width: REAL;
    commit_width: REAL;
    decode_width: REAL;
    rf_size: REAL;
 */
        // Set inputs
        try {
            fis.setVariable("dl1_nsets", 64);
        } catch (java.lang.RuntimeException e) {
        }
        try {
            fis.setVariable("dl1_bsize", 64);
        } catch (java.lang.RuntimeException e) {
        }

        // Evaluate
        fis.evaluate();

        // Show output variable's chart 
        fis.getVariable("outlvpt_size").chartDefuzzifier(true);
      
        // Print ruleSet
        //System.out.println(fis);
    }
}
