package input.model;

import input.ports.parameter.setup.SetupParameter;

import java.util.HashMap;
import java.util.Map;

public class InputData {
    protected Map<SetupParameter, Object> data;

    public InputData() {
        data = new HashMap<>();
    }

    public void set(SetupParameter name, Object value) {
        data.put(name, value);
    }

    public Object get(SetupParameter name) {
        return data.get(name);
    }
}