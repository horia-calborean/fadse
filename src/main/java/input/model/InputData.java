package input.model;

import input.model.setup.SetupParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Set<SetupParameter> getKeySet(){
        return data.keySet();
    }
}