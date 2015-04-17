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

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class TestRulesKnowledge {

    public static void main(String[] args) throws Exception {
        // Load from 'FCL' file
        String fileName = "gap_by_java_generated.fcl";
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
            fis.setVariable("PARA1", 8);
            fis.setVariable("PARA2", 4);
            fis.setVariable("PARA4", 11);
            fis.setVariable("PARA5", 13);
        } catch (java.lang.RuntimeException e) {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }

        // Evaluate
        fis.evaluate();

        // Show output variable's chart 
        fis.getVariable("OUTPARA1").chartDefuzzifier(true);
        fis.getVariable("OUTPARA2").chartDefuzzifier(true);
        fis.getVariable("OUTPARA4").chartDefuzzifier(true);
        fis.getVariable("OUTPARA5").chartDefuzzifier(true);

        System.out.println("OUTPARA1 := " + fis.getVariable("OUTPARA1").defuzzify());
        System.out.println("OUTPARA1 := " + fis.getVariable("OUTPARA2").defuzzify());
        System.out.println("OUTPARA1 := " + fis.getVariable("OUTPARA4").defuzzify());
        System.out.println("OUTPARA1 := " + fis.getVariable("OUTPARA5").defuzzify());

        // Print ruleSet
        //System.out.println(fis);
    }
}
