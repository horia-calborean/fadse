package core.application;

import core.model.paths.PathUtils;
import input.adapters.collector.gap.GAPInputCollector;
import input.application.enhancer.InputDataEnhancer;
import input.application.enhancer.gap.GapInputDataEnhancer;
import input.model.InputData;
import input.ports.collector.MicroArchInputCollector;

public class ServerBoot {
    public static void main(String[] args) {
        System.out.println("main(String[] args) from ServerBoot -> started");
        if (args.length > 0 && args[0].equals("client")) {
            ClientBoot.main(args);
        }
        else{
            String xmlFileName = "gapdistsimin_andrei.xml";
            String dseFilePath = "";
            if (args.length > 0) {
                xmlFileName = args[0];
                dseFilePath = xmlFileName;
            }
            else{
                dseFilePath = PathUtils.getDseRelativePath(xmlFileName);
            }
            MicroArchInputCollector inputCollector = new GAPInputCollector(dseFilePath);
            InputData inputData = inputCollector.collectInputData();

            InputDataEnhancer enhancer = new GapInputDataEnhancer();
            enhancer.expandDataFromFiles(inputData);

            AlgorithmRunner algorithmRunner = new AlgorithmRunner(inputData);
            algorithmRunner.run();
        }
    }
}