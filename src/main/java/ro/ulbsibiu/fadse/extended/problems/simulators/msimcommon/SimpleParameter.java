package ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon;

import java.util.LinkedHashMap;

public class SimpleParameter implements Parameter {
	public String ID;
	private String Value;
	public String CliOption;
	private boolean hasValue;
	private boolean neverRender;
	private String DefaultValue;
	
	public SimpleParameter(String id, String clioption) {
		setID(id);
		setCliOption(clioption);
		Value = "";
		hasValue = false;
		neverRender = false;
	}

	public void setID(String newID) {
		ID = newID;
	}

	public String getID() {
		return ID;
	}

	public void setCliOption(String newCliOption) {
		CliOption = newCliOption;		
	}

	public String getCliOption() {
		return CliOption;
	}
	
	public boolean hasValue() {
		return hasValue;
	}
	
	public void setDefaultValue(String defaultvalue) {
		DefaultValue = defaultvalue;
		Value = defaultvalue;
		hasValue = true;
	}
	
	public void setNeverRender(boolean n) {
		neverRender = n;
	}
	
	public boolean getNeverRender() {
		return neverRender;
	}
	
	public void retrieve(LinkedHashMap<String, String> allValues) {
		if (allValues.containsKey(ID)) {
			Value = allValues.get(ID);
			hasValue = true;
		}
		else {
			hasValue = false;
		}
	}
	
	public void modify(ParameterModifier m) {
		m.modify(this);
	}
	
	public String getValue() {
		return Value;
	}
	
	public String toString() {
		if (hasValue && neverRender == false) {
			String Output = CliOption + " " + getValue(); 
			return Output.trim();
		}
		else {
			return "";
		}
	}

	
}
