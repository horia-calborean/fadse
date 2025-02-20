package input.data;

import java.util.Map;

public abstract class InputData {
    protected Map<InputParameter, Object> data;
    public abstract Object get(InputParameter parameterName);
}