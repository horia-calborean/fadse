package ro.ulbsibiu.fadse.extended.problems.simulators.msim3;

import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class Msim3PyExtractor {
	private PythonInterpreter Python;
	private String OutFilename;
	private String ErrFilename;
	private String CurrentDir;
	private String ScriptDir;
	private String ScriptMain;
	private String FS;
	
	public Msim3PyExtractor(String outfilename, String errfilename) {
		CurrentDir = System.getProperty("user.dir");
		FS = System.getProperty("file.separator");
		ScriptDir = "pyextractor";
		ScriptMain = "Extractor.py";
		OutFilename = outfilename;
		ErrFilename = errfilename;
		
		Python = new PythonInterpreter();
		setup();
	}
	
	private void setup() {
		Python.exec("import sys");
		Python.exec("sys.path = ['__classpath__']");
		Python.exec("sys.path.append('" + CurrentDir + FS + ScriptDir + "')");
		Python.exec("sys.path.append('" + CurrentDir + FS + ScriptDir + FS +"lib" + "')");
		Python.execfile(CurrentDir + FS + ScriptDir + FS + ScriptMain);
		setOutputs();
	}
	
	private void setOutputs() {
		Python.exec("OutFile = open('" + OutFilename + "','w')");
		Python.exec("ErrFile = open('" + ErrFilename + "','w')");
	}
	
	public void initializeExtractor() {
		Python.exec("initialize()");
	}
	
	public void processLine(String line) {
		PyObject processLine_f = Python.get("processLine");
		processLine_f.__call__(new PyString(line));
	}
	
	public void finalize() {
		Python.exec("finalize()");
		Python.exec("OutFile.flush()");
		Python.exec("ErrFile.flush()");
		Python.exec("OutFile.close()");
		Python.exec("ErrFile.close()");
	}
	
	public Double getObjectiveValue(String objectiveName) {
		Double Value = new Double(0);
		try {
			Value = (Double) Python.get("getMetricValue").__call__(new PyString(objectiveName)).__tojava__(Double.class);
		}
		catch (PyException e) {
			Value = null;
		}
		return Value;
	}
	
	public Integer getErrorCount() {
		Integer Value = new Integer(0);
		Value = (Integer) Python.get("getErrorCount").__call__().__tojava__(Integer.class);
		return Value;
	}
	
}
