package input.model;

import input.ports.parameter.InputParameter;

import java.util.HashMap;
import java.util.Map;

public class InputData {
    protected Map<InputParameter, Object> data;

    public InputData() {
        data = new HashMap<>();
    }

    public void set(InputParameter name, Object value) {
        data.put(name, value);
    }

    public Object get(InputParameter name) {
        return data.get(name);
    }
}