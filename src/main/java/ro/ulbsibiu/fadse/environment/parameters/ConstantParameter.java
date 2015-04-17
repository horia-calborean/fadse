package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.base.Variable;


public class ConstantParameter implements Parameter {
	private Object value;
        private String name;
        private String type;
        private String description;
        public ConstantParameter(String name, String type, String description){
            this.name = name;
            this.type = type;
            this.description = description;
        }
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value =  value;
	}
	@Override
	public String toString() {
		return "" + value + "";
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

 
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setVariable(Variable v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Variable getVariable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
