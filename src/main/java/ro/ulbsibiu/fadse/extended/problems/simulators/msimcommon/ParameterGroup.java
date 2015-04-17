package ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.lang.NullPointerException;

public class ParameterGroup implements Parameter {
	protected String ID;
	protected String CliOption;
	protected String DefaultValue;
	protected LinkedList<Parameter> SubParameters;
	protected String Value;
	protected ParameterModifier DefaultModifier;
	protected boolean hasValue;
	protected boolean selfModifiable;
	protected boolean neverRender;
	
	public ParameterGroup(String id, String clioption) {
		setID(id);
		setCliOption(clioption);
		SubParameters = new LinkedList<Parameter>();
		Value = "";
		DefaultModifier = null;
		hasValue = false;
		selfModifiable = false;
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
	
	public void setNeverRender(boolean nr) {
		neverRender = nr;
	}
	
	public boolean getNeverRender() {
		return neverRender;
	}
	
	public void setSelfModifiable(boolean sm) {
		selfModifiable = sm;
	}
	
	public void setDefaultValue(String defaultvalue) {
		DefaultValue = defaultvalue;
		Value = defaultvalue;
		hasValue = true;
	}
	
	public void setDefaultModifier(ParameterModifier newModifier) {
		DefaultModifier = newModifier;
	}
	
	public void addSubParameter(Parameter p) {
		SubParameters.add(p);
	}
	
	public void retrieve(LinkedHashMap<String, String> allValues) {
		Parameter P;
		Iterator<Parameter> I = SubParameters.iterator();
		while (I.hasNext()) {
			P = I.next();
			P.retrieve(allValues);
			if (P.hasValue()) {
				hasValue = true;
			}
		}
	}
	
	public void modify(ParameterModifier m) {
		try {
			if (m == null) {
				m = DefaultModifier;
			}
			if (selfModifiable) {
				m.modify(this);
			}
			Parameter P;
			Iterator<Parameter> I = SubParameters.iterator();
			while (I.hasNext()) {
				P = I.next();
				P.modify(m);
			}
		}
		catch (NullPointerException e) {
		}
	}

	public String getValue() {
		Parameter P;
		StringBuilder ValueAssembler = new StringBuilder();
		Iterator<Parameter> I = SubParameters.iterator();
		while (I.hasNext()) {
			P = I.next();
			if (P.hasValue() && P.getNeverRender() == false) {
				ValueAssembler.append(P.toString());
				if (I.hasNext()) {
					ValueAssembler.append(" ");
				}
			}
		}
		return ValueAssembler.toString().trim();
	}
	
	public String toString() {
		return getValue();
	}
}
	
	
	

