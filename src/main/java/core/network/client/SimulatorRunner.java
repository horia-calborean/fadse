package core.network.client;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import core.model.individual.FadseIndividual;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;

public class SimulatorRunner implements Runnable {
    protected Simulator simulator;
    protected LinkedHashMap<String, String> simpleParameters;
    protected ProblemParameter<?>[] currentParameters;
    protected Process p = null;
    protected FadseIndividual individual;

    public SimulatorRunner(Simulator simulator) {
        this.simulator = simulator;
        this.simpleParameters = new LinkedHashMap<>();
    }

    public void addSimpleParameter(String name, String value){
        simpleParameters.remove(name);
        simpleParameters.put(name, value);
    }

    public Map<String, String> getSimpleParameters(){
        return this.simpleParameters;
    }

    protected String[] getCommandLine(){
        LinkedList<String> params = new LinkedList<>();
        InputData inputData = individual.getInputData();
        Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
        String simulator_executable = problemConfigParameters.get("simulator_executable");
        params.add(simulator_executable);
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

    protected void prepareParameters(){
        for (ProblemParameter<?> param:   this.currentParameters) {
            addSimpleParameter(param.getName(), param.getValue().toString());
        }
    }

    public void setParameters(ProblemParameter<?>[] parameters){
        this.currentParameters = new ProblemParameter<?>[parameters.length];
        this.currentParameters = parameters;
        this.prepareParameters();
    }


    public void run(boolean reallyRun) {
          if (!reallyRun){
              InputData inputData = individual.getInputData();
              Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
              String simulator_executable = problemConfigParameters.get("simulator_executable");
               String executeCommand = simulator_executable + Arrays.toString(getCommandLine());
               String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);
                System.out.println("Should have started the simulator: [" + simulatorName +
                "] with the following command: \n" + executeCommand +
                 "\n TO RUN IT ... change in SimulatorBase::performSimulation -> this.simulatorRunner.run(true);");
          } else {
              this.run();
          }
    }

    public void run(){
        StringBuilder executeCommand = new StringBuilder();
        String[] CommandLineArgs = this.getCommandLine();
        for (String s : CommandLineArgs){
            executeCommand.append(" ").append(s);
        }
        InputData inputData = individual.getInputData();
        String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);
        System.out.println("- Starting simulator: [" + simulatorName +
                "] with the following command: \n" + executeCommand);

        try {
            // Execute simulator
            p = Runtime.getRuntime().exec(CommandLineArgs);

            // Retrieve output of simulator (for debugging only)
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (output.readLine() != null) {
               // System.out.println(line);
            }
            output.close();

            // Wait for the simulation to end
            p.waitFor();
            System.out.println("- Simulation ended");
            p = null;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        
    }

    public void stopRunning() {
        if(p != null)
            p.destroy();
    }

    public FadseIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(FadseIndividual individual) {
        this.individual = individual;
    }
}