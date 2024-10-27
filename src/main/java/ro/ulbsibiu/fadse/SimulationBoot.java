package ro.ulbsibiu.fadse;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.SimulationIO;
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

            String designSpaceConfigFileName = "gapdistsimin_andrei.xml";
            String clientConfigFileName = "simpleClientConfig.xml";

            if (args.length > 0) {
                designSpaceConfigFileName = args[0];
            }

            String checkpointFilePath = "";
            String smpsoDataFilePath = "";

            String fuzzyConfigFilePath = "";
            String fileSeparator = FileSystems.getDefault().getSeparator();

            String designSpaceConfigFilePath = currentDirectory
                    + fileSeparator + "configs"
                    + fileSeparator + "designSpace"
                    + fileSeparator + designSpaceConfigFileName;

            String clientConfigFilePath = currentDirectory
                    + fileSeparator + "configs"
                    + fileSeparator + "client"
                    + fileSeparator + clientConfigFileName;

            for (int i = 1; i < args.length; i++) {
                if (args[i].endsWith(".xml")) {
                    clientConfigFilePath = args[i];
                } else if (args[i].endsWith(".csv")) {
                    checkpointFilePath = args[i];
                } else if (args[i].endsWith(".spd")) {
                    smpsoDataFilePath = args[i];
                } else if (args[i].endsWith(".fcl")) {
                    fuzzyConfigFilePath = args[i];
                }
            }

            SimulationIO simulationIO = new SimulationIO(designSpaceConfigFilePath);
            CheckpointFileParameter checkpointFileParameter = new CheckpointFileParameter("Checkpointing", checkpointFilePath, smpsoDataFilePath);
            simulationIO.setCheckpointFileParameter(checkpointFileParameter);
            simulationIO.setFuzzyInputFilePath(fuzzyConfigFilePath);
            simulationIO.setClientsConfigFilePath(clientConfigFilePath);

            try {
                AlgorithmRunner algRunner = new AlgorithmRunner();
                algRunner.run(simulationIO);
            } catch (JMetalException | SecurityException | IOException | IllegalArgumentException | IllegalAccessException |
                     ClassNotFoundException ex) {
                Logger.getLogger(SimulationBoot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}