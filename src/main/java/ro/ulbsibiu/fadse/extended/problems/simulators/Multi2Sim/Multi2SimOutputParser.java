/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.Multi2Sim;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.Msim3Constants;

/**
 * Parser for the output file of Msim simulator
 *
 * @author Andrei
 * @version 0.1
 * @since 15.04.2010
 */
public class Multi2SimOutputParser extends SimulatorOutputParser {
    String currentRegion = "";
    /**
     * constructor
     * @param outputFilePath  the path to the output file that needs processing
     */
    public Multi2SimOutputParser(SimulatorBase scbSim){
       super(scbSim);
    }

    /**
     * Searches for the output result value in a line of text 
     * This is depending on the simulator output file format
     * @param textLine    the line of text where the value is to be searched
     * @param lineNumber  in some cases the line number is necessary for processing
     *                    skipping lines, or changing the delimiter (starts at 1)
     */
    @Override
    protected void processLine(String textLine, int lineNumber){

        // if the line starts with ; then it is a comment
        if (textLine.length() > 0 && textLine.startsWith(";"))
            return;

        // FIRST find out in what region we are
        Pattern MY_PATTERN = Pattern.compile("\\[(.*?)\\]");
        Matcher m = MY_PATTERN.matcher(textLine);
        String region = "";
        
        while (m.find()) region = m.group(1);

        if (region != ""){
            this.currentRegion = region.trim();
        } else {
            // if there is no region change, then we can test the variable
            Scanner scanner = new Scanner(textLine);
            this.defaultDelimiter = "=\\s*";
            scanner.useDelimiter(this.defaultDelimiter);

            if (scanner.hasNext()){

              String name = this.currentRegion + Multi2SimConstants.REG_DELIMITER + scanner.next().trim();
              if (this.isInOutputs(name)){

                if(scanner.hasNext())
                    this.addSimpleObjective(name, scanner.nextDouble());
              }
            }

            scanner.close();
        }
    }

     /**
     * gets the final results
     * @return
     */
    @Override
    public LinkedList<Objective> getResults(Individual individual){
        String outputFile = this.simulator.getSimulatorOutputFile();

        // because m2s writes its output in two separate files, we have to
        // concatenate them in the simulatorOutputFile specified
        try {
            String[] ofiles = new String[2];
            ofiles[0] = this.simulator.getInputDocument().getSimulatorParameter("simulator_path") + Multi2SimConstants.OF_CACHE;
            ofiles[1] = this.simulator.getInputDocument().getSimulatorParameter("simulator_path") +  Multi2SimConstants.OF_PIPELINE;
            FileEnumeration lof = new FileEnumeration(ofiles);

            FileOutputStream fos = new FileOutputStream(outputFile);
            SequenceInputStream s = new SequenceInputStream(lof);
            int c;

            while ((c = s.read()) != -1){
                fos.write(c);
            }
            s.close();
        } catch (FileNotFoundException ex){
        } catch (Exception ex){
        }

        this.file = new File(outputFile);
        
        // continue with processing the new concatenated file the normal way
        this.processFile(individual);

        LinkedList<Objective> finalResults = new LinkedList<Objective>();
        for(Objective obj: this.currentObjectives){
            String key = obj.getName();
            if (this.results.containsKey(key)){
                obj.setValue(this.results.get(key));
            }

            finalResults.add(obj);
        }
  
        return finalResults;
    }

}
