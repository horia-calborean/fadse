package ro.ulbsibiu.fadse.io.parameters;

import org.w3c.dom.Document;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;

import java.util.HashMap;

public abstract class ParameterParser {
    protected final HashMap<String, Class<? extends Parameter>> parameterClassMap;

    public ParameterParser(){
        parameterClassMap = new HashMap<>();
        buildClassMap();
    }

    protected abstract void buildClassMap();

    protected Parameter createParameter(String name, String type, String description) throws Exception {
        Class<? extends Parameter> clazz = parameterClassMap.get(type);
        if (clazz != null) {
            return clazz.getDeclaredConstructor(String.class, String.class, String.class).newInstance(name, type, description);
        }
        throw new IllegalArgumentException("Unknown parameter type: " + type);
    }

    public abstract Parameter[] parseParameters(Document dseXmlDocument);
}