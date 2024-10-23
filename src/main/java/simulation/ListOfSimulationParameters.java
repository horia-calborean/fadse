package simulation;

import java.util.Dictionary;
import java.util.Hashtable;

public class ListOfSimulationParameters {
    protected Hashtable<SimulationParameter, Object> parameters;

    public ListOfSimulationParameters(){
        parameters = new Hashtable<>();
    }

    public void add(SimulationParameter parameter, Object value){
        if(parameters.containsKey(parameter)){
            return;
        }

        parameters.put(parameter, value);
    }

    public Object get(SimulationParameter parameter){
        return parameters.get(parameter);
    }

    public void clear(){
        parameters.clear();
    }

    public void remove(SimulationParameter parameter){
        parameters.remove(parameter);
    }

    public void update(SimulationParameter parameter, Object value){
        parameters.put(parameter, value);
    }

    public boolean contains(SimulationParameter parameter){
        return parameters.containsKey(parameter);
    }
}