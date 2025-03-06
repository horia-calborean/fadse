package input.ports.extractor;

import core.ports.simulationClient.SimulationClient;

import java.util.List;

public interface ClientsExtractor {
    List<SimulationClient> extractClientsFilePath();
}