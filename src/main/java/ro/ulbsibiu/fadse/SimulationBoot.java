package ro.ulbsibiu.fadse;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.util.JMException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.tools.monitor.SwingMonitor;

public class SimulationBoot {

    public static void main(String[] args) {
        System.out.println("#########################################");
        System.out.println("# FADSE              client server or xml");
        System.out.println("#########################################");

        ExitInputLister.addExitListener();

        if (args.length > 0 && args[0].equals("client")) {
            FadseClientBoot.main(args);
        } else if (args.length > 0 && args[0].equals("monitor")) {
            SwingMonitor.main(args);
        } else {
            File currentDirectory = new File(System.getProperty("user.dir"));

            String xmlFileName = "gapdistsimin_andrei.xml";

            if (args.length > 0) {
                xmlFileName = args[0];
            }

            String checkpointFile = "";
            String secondFile = "";

            String fuzzyConfigFile ="";
            String fileSeparator = FileSystems.getDefault().getSeparator();

            String environmentConfigFile = currentDirectory
            		+ fileSeparator + "configs"
            		+ fileSeparator + "designSpace"
            		+ fileSeparator + xmlFileName;

            String neighborConfig = currentDirectory
            		+ fileSeparator + "configs"
            		+ fileSeparator + "neighbor"
            		+ fileSeparator + "simpleNeighborConfig.xml";

            for (int i = 1; i < args.length; i++) {
                if (args[i].endsWith(".xml")) {
                    neighborConfig = args[i];
                } else if (args[i].endsWith(".csv")) {
                    checkpointFile = args[i];
                } else if (args[i].endsWith(".spd")) { //for SMPSO speed checkpointFile
                    secondFile = args[i];
                } else if (args[i].endsWith(".fcl")) {
                    fuzzyConfigFile = args[i];
                }
            }           

            Environment env = new Environment(environmentConfigFile);
            CheckpointFileParameter checkpointFileParameter = new CheckpointFileParameter(checkpointFile, secondFile);
            env.setCheckpointFileParameter(checkpointFileParameter);
            env.setFuzzyInputFile(fuzzyConfigFile);
            env.setNeighborsConfigFile(neighborConfig);
            
            try {
                AlgorithmRunner algRunner = new AlgorithmRunner();
                algRunner.run(env);
            } catch (JMException | SecurityException | IOException | IllegalArgumentException | IllegalAccessException |
                     ClassNotFoundException ex) {
                Logger.getLogger(SimulationBoot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}