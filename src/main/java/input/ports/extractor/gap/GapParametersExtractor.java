package input.ports.extractor.gap;

import input.ports.parameter.problem.ProblemParameter;

public interface GapParametersExtractor {
    ProblemParameter<?>[] extractParameters() throws Exception;
}
