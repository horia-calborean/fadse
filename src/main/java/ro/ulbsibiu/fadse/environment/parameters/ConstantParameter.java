package ro.ulbsibiu.fadse.environment.parameters;

public class ConstantParameter extends Parameter {
	private Object value;

    public ConstantParameter(String name, String type, String description){
        super(name, type, description);
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
        return new ConstantParameter(this.getName(), this.getType(), this.getDescription());
	}

    @Override
    public String toString() {
        return "" + value + "";
    }
}
