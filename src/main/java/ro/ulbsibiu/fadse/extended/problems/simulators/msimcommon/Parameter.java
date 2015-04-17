package ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon;

import java.util.LinkedHashMap;

public interface Parameter {
	public String getID();
	public void setID(String newID);
	
	public void setDefaultValue(String defaultvalue);
	
	public void setCliOption(String newCliOption);
	public String getCliOption();
	
	public String getValue();
	
	public boolean hasValue();
	
	public void setNeverRender(boolean nr);
	public boolean getNeverRender();
	
	public void retrieve(LinkedHashMap<String, String> allValues);
	public String toString();
	public void modify(ParameterModifier m);
}
