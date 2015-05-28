/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.sniper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;
import ro.ulbsibiu.fadse.utils.JSONHelper;

/**
 *
 * @author Andrei DAIAN
 * @since 03.05.2013
 * @version 1.0
 */
public class SniperGroupResults extends SimulatorOutputParser {

	public SniperGroupResults(SimulatorBase sim) {
		super(sim);
	}

	public static HashMap<String, String> mcPatParamsList = new HashMap<String, String>();
	public static HashMap<String, Float> finalParamList = new HashMap<String, Float>();
	// Objectives for HW
	public final String OBJECTIVE_CLOCKS_PER_INSTRUCTION = "cpi";
	public final String OBJECTIVE_AREA = "area";
	public float finalCPI = 0;
	public float area = 0;
	public float peakPower = 0;
	public float totalLeakage = 0;
	public float peakDynamic = 0;
	public float subthresholdLeakage = 0;
	public float gateLeakage = 0;
	public float runtimeDynamic = 0;
	public float totalEnergy = 0;
	public int start = 15;
	public int end = 32;
	public float frequency = (float) 2.66; // GHz
	public float power = 0;
	public float temperature = 0;

	public boolean groupPartialResults() {
		String simOutputDir = this.simulator.getInputDocument()
				.getSimulatorParameter("simulator_opt");

		String simOutputFile = simOutputDir + "sim.out";

		ArrayList<Float> nInstructions = new ArrayList<Float>();
		ArrayList<Float> nCycles = new ArrayList<Float>();

		int nrCores = 0;

		try {
			FileInputStream fstream = new FileInputStream(simOutputFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read first line
			strLine = br.readLine();
			if (strLine != null) {
				StringTokenizer st = new StringTokenizer(strLine, "|");
				nrCores = st.countTokens() - 1;
			}

			strLine = br.readLine();
			if (strLine != null) {
				StringTokenizer st = new StringTokenizer(strLine, "|");

				// First one should be instructions
				st.nextToken();

				while (st.hasMoreTokens()) {
					nInstructions.add(Float.parseFloat(st.nextToken()
							.toString().trim()));
				}
			}

			strLine = br.readLine();
			if (strLine != null) {
				StringTokenizer st = new StringTokenizer(strLine, "|");

				// First one should be cycles
				st.nextToken();

				while (st.hasMoreTokens()) {
					nCycles.add(Float.parseFloat(st.nextToken().toString()
							.trim()));
				}
			}

			in.close();
		} catch (NumberFormatException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
		float instructionsSum = 0;

		for (int i = 0; i < nrCores; i++) {
			instructionsSum += nInstructions.get(i);
		}

		float maxCycles = 0;
		maxCycles = Collections.max(nCycles);

		float average = 0;

		average = maxCycles / instructionsSum;
		finalCPI = average;

		// This is for McPAT
		if (Integer.parseInt(this.simulator.getInputDocument()
				.getSimulatorParameter("mcpat")) == 1) {
			String powerFile = simOutputDir + "power.py";
			readFromFile(powerFile);

			// This is for Total Energy
			String energyFile = simOutputDir
					+ this.simulator.getInputDocument().getSimulatorParameter(
							"console_output");
			Energy energy = new Energy();
			totalEnergy = energy.readEnergy(energyFile);
			area = finalParamList.get("Area");
		}

		File finalResults = null;

		String hotspot = this.simulator.getInputDocument()
				.getSimulatorParameter("hotspot");
		if (hotspot != null && !hotspot.isEmpty()) {
			ReadMaxTemperature(simOutputDir);
		}

		try {
			finalResults = new File(this.simulator.getInputDocument()
					.getSimulatorParameter("simulator_final_results"));
			String parent = finalResults.getParent();
			if (parent != null) {
				File parentDir = new File(parent);
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
			}

			FileWriter outFile = new FileWriter(finalResults);
			PrintWriter out = new PrintWriter(outFile);

			out.println("[output]");
			out.println("cpi = " + finalCPI);
			out.println("area = " + area);
			out.println("energy = " + totalEnergy);
			out.println("temperature = " + temperature);

			out.close();

			return true;
		} catch (IOException e) {
		}

		return false;
	}

	private void ReadMaxTemperature(String simoutDir) {
		try {
			File temperatureFile = new File(simoutDir + File.pathSeparator
					+ "hotspot" + File.pathSeparator + "temperature.ttrace");

			FileInputStream fstream;

			fstream = new FileInputStream(temperatureFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			String fullString = "";

			strLine = br.readLine(); //skip first line with header
			
			while ((strLine = br.readLine()) != null) {
				fullString += " " +strLine;
			}
			
			String[] split = fullString.split(" ");
			
			for(String strValue : split){
				Float doubleValue = Float.parseFloat(strValue); 
				if(doubleValue > temperature){
					temperature = doubleValue;
				}
			}

			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void readFromFile(String fileName) {
		int counterLine = 0;

		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			String fullString = "";

			while ((strLine = br.readLine()) != null) {
				fullString += strLine;
			}

			in.close();

			String split[] = fullString.split(" = ");
			String jsonString = split[1];
			jsonString = jsonString.replaceAll("\'", "\"");
			Object processorArea = JSONHelper.GetValue(jsonString,
					"Processor:Area");
			if (processorArea != null) {
				finalParamList.put("Area",
						Float.parseFloat(processorArea.toString()));
			}

		} catch (NumberFormatException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
