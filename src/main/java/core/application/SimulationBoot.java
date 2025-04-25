package core.application;

import core.model.paths.PathUtils;
import input.adapters.collector.gap.GAPInputCollector;
import input.application.enhancer.InputDataEnhancer;
import input.application.enhancer.gap.GapInputDataEnhancer;
import input.model.InputData;
import input.ports.collector.MicroArchInputCollector;

public class SimulationBoot {
    public static void main(String[] args) {
        System.out.println("main(String[] args) from SimulationBoot -> started");

        String dseFilePath = PathUtils.getDseRelativePath("gapdistsimin_andrei.xml");
        MicroArchInputCollector inputCollector = new GAPInputCollector(dseFilePath);
        InputData inputData = inputCollector.collectInputData();

        InputDataEnhancer enhancer = new GapInputDataEnhancer();
        enhancer.expandDataFromFiles(inputData);
    }
}