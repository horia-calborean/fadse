package input.ports.extractor.common;

import core.model.objectives.Objective;

import java.util.Map;

public interface ObjectivesExtractor {
    Map<String, Objective> extractObjectives();
}