package ro.ulbsibiu.fadse.environment.parameters;

public class ConstantParameterClass extends ParameterClass {
	private Object value;
    private String name;
    private String type;
    private String description;

    public ConstantParameterClass(String name, String type, String description){
        this.name = name;
        this.type = type;
        this.description = description;
    }

    @Override
	public Object getValue() {
		return value;
	}

    @Override
	public void setValue(Object value) {
		this.value =  value;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		//return super.clone();
        return new ConstantParameterClass(this.name, this.type, this.description);
	}

    public String toString() {
        return "" + value + "";
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
}
