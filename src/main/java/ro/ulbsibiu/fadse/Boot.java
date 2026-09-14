package ro.ulbsibiu.fadse;

import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.tools.monitor.SwingMonitor;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO - Rename this class as SimulationBoot or something like that
public class Boot {
    public static void main(String[] args) {
        System.out.println("#########################################");
        System.out.println("# FADSE              client server or xml");
        System.out.println("#########################################");

        ExitInputLister.addExitListener();

        if (args.length > 0 && args[0].equals("client")) {
            BootClient.main(args);
        } else if (args.length > 0 && args[0].equals("monitor")) {
            SwingMonitor.main(args);
        } else {
            String currentDir = System.getProperty("user.dir");
            File dir = new File(currentDir);

            String xmlFileName = "gapdistsimin_andrei.xml";

            if (args.length > 0) {
                xmlFileName = args[0];
            }

            String checkpointFile = "";
            String secondFile = "";

            String fuzzyConfigFile = "";

            String defaultSeparator = FileSystems.getDefault().getSeparator();

            String environmentConfigFile = dir
                    + defaultSeparator + "configs"
                    + defaultSeparator + "designSpace"
                    + defaultSeparator + xmlFileName;

            String neighborConfig = dir
                    + defaultSeparator + "configs"
                    + defaultSeparator + "neighbor"
                    + defaultSeparator + "simpleNeighborConfig.xml";

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
            //TODO
            env.setNeighborsConfigFile(neighborConfig);
            //END TODO            

            try {
                AlgorithmRunner algRunner = new AlgorithmRunner();
                algRunner.run(env);
            } catch (JMetalException | SecurityException | IllegalArgumentException ex) {
                Logger.getLogger(Boot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}