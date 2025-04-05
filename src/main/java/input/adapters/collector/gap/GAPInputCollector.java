package input.adapters.collector.gap;

import core.model.objectives.Objective;
import input.adapters.extractor.gap.GAPJsonDataExtractor;
import input.adapters.extractor.gap.GAPXmlDataExtractor;
import input.adapters.document.JsonInputDocument;
import input.adapters.document.XmlInputDocument;
import input.model.setup.GapSetupParameters;
import input.model.InputData;
import input.ports.document.InputDocument;
import input.ports.collector.MicroArchInputCollector;
import input.ports.extractor.common.BenchmarkExtractor;
import input.ports.extractor.common.MetaheuristicExtractor;
import input.ports.extractor.common.ObjectivesExtractor;
import input.ports.extractor.fadse.ClientsFileExtractor;
import input.ports.extractor.fadse.DatabaseExtractor;
import input.ports.extractor.gap.*;
import input.ports.parameter.problem.ProblemParameter;

import java.util.List;
import java.util.Map;

public class GAPInputCollector extends MicroArchInputCollector {

    public GAPInputCollector(String filePath) {
        super(filePath);
    }

    @Override
    protected Object createExtractor(InputDocument inputDocument) {
        if (inputDocument instanceof XmlInputDocument) {
            return new GAPXmlDataExtractor((XmlInputDocument) inputDocument);
        } else if (inputDocument instanceof JsonInputDocument) {
            return new GAPJsonDataExtractor((JsonInputDocument) inputDocument);
        }
        throw new IllegalArgumentException("Error when creating GAP input data extractor");
    }

    @Override
    public InputData collectInputData() {
        InputData inputData = new InputData();

        Map<String, String> configParameters = getConfigParameters();
        inputData.set(GapSetupParameters.GAP_CONFIG, configParameters);

        ProblemParameter<?>[] problemParameters = getProblemParameters();
        inputData.set(GapSetupParameters.GAP_PARAMETERS, problemParameters);

        List<String> benchmarksList = getBenchmarkList();
        inputData.set(GapSetupParameters.BENCHMARKS, benchmarksList);

        Map<String, String> metaheuristicData = getMetaheuristicData();
        inputData.set(GapSetupParameters.METAHEURISTIC, metaheuristicData);

        Map<String, Objective> objectives = getObjectives();
        inputData.set(GapSetupParameters.OBJECTIVES, objectives);

        Map<String, String> dbConnectionData = getDbConnectionData();
        inputData.set(GapSetupParameters.DATABASE, dbConnectionData);

        String fadseClientsFilePath = getClientsFileName();
        inputData.set(GapSetupParameters.FADSE_CLIENTS_FILE_PATH, fadseClientsFilePath);

        String outputPath = getOutputPath();
        inputData.set(GapSetupParameters.OUTPUT_PATH, outputPath);

        String type = getType();
        inputData.set(GapSetupParameters.TYPE, type);

        String name = getName();
        inputData.set(GapSetupParameters.NAME, name);

        return inputData;
    }

    protected Map<String, String> getConfigParameters() {
        return ((GapConfigExtractor) dataExtractor).extractGapConfigParameters();
    }

    protected ProblemParameter<?>[] getProblemParameters() {
        try {
            return ((GapParametersExtractor) dataExtractor).extractParameters();
        }
        catch (Exception ex){
            System.out.println("Exception occurred when extracting problem parameters");
            return null;
        }
    }

    protected List<String> getBenchmarkList() {
        return ((BenchmarkExtractor) dataExtractor).extractBenchmarksList();
    }

    protected Map<String, String> getMetaheuristicData() {
        return ((MetaheuristicExtractor) dataExtractor).parseMetaheuristic();
    }

    protected Map<String, String> getDbConnectionData() {
        return ((DatabaseExtractor) dataExtractor).parseDbConnectionData();
    }

    protected Map<String, Objective> getObjectives() {
        return ((ObjectivesExtractor) dataExtractor).extractObjectives();
    }

    protected String getType() {
        return ((TypeExtractor) dataExtractor).extractType();
    }

    protected String getName() {
        return ((NameExtractor) dataExtractor).extractName();
    }

    protected String getOutputPath() {
        return ((OutputPathExtractor) dataExtractor).extractOutputPath();
    }

    protected String getClientsFileName() {
        return ((ClientsFileExtractor) dataExtractor).extractClientsFilePath();
    }
}