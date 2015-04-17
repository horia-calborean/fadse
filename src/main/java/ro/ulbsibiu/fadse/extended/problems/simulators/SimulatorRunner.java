package ro.ulbsibiu.fadse.extended.problems.simulators;
import java.io.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;

/**
 * Generic class for starting a simulator
 *
 * @author Andrei
 * @version 0.1
 * @since 15.04.2010
 */
public class SimulatorRunner implements Runnable {
    protected SimulatorBase simulator;
    protected LinkedHashMap<String, String> simpleParameters;
    protected Parameter[] currentParameters;
    protected Process p = null;
    protected Individual individual;

    /**
     * contructor
     * @param simulator the Simulator that needs to be run
     * that is to be executed
     */
    public SimulatorRunner(SimulatorBase simulator) {
        this.simulator = simulator;
        this.simpleParameters = new LinkedHashMap<String, String>();
    }


    // <editor-fold defaultstate="collapsed" desc="Parameter Functions" >
    /**
     * PARAMETER FUNCTIONS
     * all functions needed for converting between base parameters and current
     * simulator parameters
     */

    /**
     * adds a parameter value to this.parameters Hashmap
     * @param name  the name of the parameter
     * @param value the value of the parameter
     */
    public void addSimpleParameter(String name, String value){
        // System.out.println("Vorher: " + simpleParameters.get(name));
        this.simpleParameters.remove(name);
        this.simpleParameters.put(name, value);
        // System.out.println("Nachher: " + simpleParameters.get(name));
    }

    /**
     * Return the parameters specific for the simulator
     * @return Map<String, String>
     */
    public Map<String, String> getSimpleParameters(){
        return this.simpleParameters;
    }
    /**
     * generates a simulator specific parameter string from the parameters collection
     * @return the actual parameter string like "executable -param1 -param2 -param1: value "
     */
    protected String[] getCommandLine(){
        LinkedList<String> params = new LinkedList<String>();
        params.add(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        for (Map.Entry<String, String> param:  this.simpleParameters.entrySet()){
            String str = getParameterPrefix(param.getKey()) + param.getKey();
            if (!param.getValue().isEmpty())
                str += " " + param.getValue();
            params.add(str);
        }
        String[] result = new String[params.size()];
        params.toArray(result);

        return result;
    }


    protected String getParameterPrefix(String parameterName){
        return "-";
    }


    /**
     * Creates a simple map (easy to work with) collection of parameters
     * all as simple <Strings, String> Collection
     * It is based on the XML file
     */
    protected void prepareParameters(){
        // this.simpleParameters = new LinkedHashMap<String, String>();
        for (Parameter param:   this.currentParameters) {
            // System.out.println("- AddParameter: " + param.getName() + " = " +  param.getValue().toString());
            this.addSimpleParameter(param.getName(), param.getValue().toString());
        }
    }

    public void setParameters(Parameter[] parameters){
        this.currentParameters = new Parameter[parameters.length];
        this.currentParameters = parameters;
        this.prepareParameters();
    }
   // </editor-fold>


    /**
     * starts the simulator process and waits for it to finish
     * @param fakeRun just for testing
     */
    public void run(boolean reallyRun) {
          if (!reallyRun){
               String executeCommand = this.simulator.getInputDocument().getSimulatorParameter("simulator_executable") +
                this.getCommandLine();
                System.out.println("Should have started the simulator: [" + simulator.getInputDocument().getSimulatorName() +
                "] with the following command: \n" + executeCommand +
                 "\n TO RUN IT ... change in SimulatorBase::perfromSimulation -> this.simulatorRunner.run(true);");
          } else {
              this.run();
          }
    }

    public void run(){
        String line;
        String executeCommand = "";
        String CommandLineArgs[] = this.getCommandLine();
        for (String s : CommandLineArgs){
            executeCommand += " "+ s;
        };
        System.out.println("- Starting simulator: [" + simulator.getInputDocument().getSimulatorName() +
                "] with the following command: \n" + executeCommand);

        try {
            // Execute simulator
            p = Runtime.getRuntime().exec(CommandLineArgs);

            // Retrieve output of simulator (for debugging only)
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = output.readLine()) != null) {
               // System.out.println(line);
            }
            output.close();

            // Wait for the simulation to end
            p.waitFor();
            System.out.println("- Simulation ended");
            p = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Stops the process
     */
    public void stopRunning() {
        if(p != null)
            p.destroy();
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }
}
