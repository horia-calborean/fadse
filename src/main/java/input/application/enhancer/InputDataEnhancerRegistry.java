package input.application.enhancer;

import input.application.enhancer.gap.GapInputDataEnhancer;
import input.model.setup.GapSetupParameters;
import input.model.setup.SetupParameter;

import java.util.HashMap;
import java.util.Map;

public class InputDataEnhancerRegistry {
    private static final Map<Class<? extends SetupParameter>, InputDataEnhancer> enhancers = new HashMap<>();

    static {
        enhancers.put(GapSetupParameters.class, new GapInputDataEnhancer());
    }

    public static InputDataEnhancer getEnhancer(SetupParameter setupParameter) {
        return enhancers.get(setupParameter.getClass());
    }
}