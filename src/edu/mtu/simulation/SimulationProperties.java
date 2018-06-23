package edu.mtu.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains various properties related to how the simulation should run 
 * and be managed. Note that the should not change once the application has been
 * started.
 */
public class SimulationProperties {

	// Flag to indicate no limit on the number of molecules
	public static final int NO_LIMIT = -1;
	
	// Flag for if results should be overwritten
	private boolean overWriteResults = true;
	
	// Flag to indicate the type of decay model to use
	private boolean experimetnalDecay = false;
	
	// Limit to the number of molecules in the model, -1 means no limit
	private int moleculeLimit = NO_LIMIT;
				
	// Interval to report to the console and save data on
	private int reportInterval = 100;
	
	// Length of one time step in seconds
	private int timeStepLength = 60;
	
	// List of entities to terminate when zero
	private List<String> terminateOn = new ArrayList<String>(); 
	
	// Default paths to experiments
	private String chemicalsFileName = "";
	private String experimentalDataFileName = "";
	private String reactionsFileName = "";
	
	// Location of results
	private String resultsFileName = "data/results%s.csv";
	private String molarFileName = "data/molar%s.csv";
	
	private static SimulationProperties instance = new SimulationProperties();
	
	/**
	 * Constructor.
	 */
	private SimulationProperties() {	}
	
	/**
	 * Get the instance of the properties.
	 */
	public static SimulationProperties getInstance() {
		return instance;
	}
	
	public void addTerminationOn(String value) {
		terminateOn.add(value);
	}
	
	public String getChemicalsFileName() {
		return chemicalsFileName;
	}
	
	public String getExperimentalDataFileName() {
		return experimentalDataFileName;
	}
	
	public boolean getExperimentalDecay() {
		return experimetnalDecay;
	}
	
	public String getMolarFileName() {
		return molarFileName;
	}
	
	public int getMoleculeLimit() {
		return moleculeLimit;
	}
			
	public boolean getOverWriteResults() {
		return overWriteResults;
	}
	
	public String getReactionsFileName() {
		return reactionsFileName;
	}
	
	public int getReportInterval() {
		return reportInterval;
	}
	
	public String getResultsFileName() {
		return resultsFileName;
	}
	
	public List<String> getTerminationOn() {
		return terminateOn;
	}
	
	public int getTimeStepLength() {
		return timeStepLength;
	}
	
	public void setChemicalsFileName(String value) {
		chemicalsFileName = value;
	}
	
	public void setExperimentalDataFileName(String value) {
		experimentalDataFileName = value;
	}
	
	public void setExperimentalDecay(boolean value) {
		experimetnalDecay = value;
	}
	
	public void setMolarFileName(String value) {
		molarFileName = value;
	}
	
	public void setOverWriteResults(boolean value) {
		overWriteResults = value;
	}
	
	public void setMoleculeLimit(int value) {
		moleculeLimit = value;
	}
	
	public void setReactionsFileName(String value) {
		reactionsFileName = value;
	}
	
	public void setReportInterval(int value) {
		reportInterval = value;
	}
	
	public void setResultsFileName(String value) {
		resultsFileName = value;
	}
	
	public void setTimeStepLength(int value) {
		timeStepLength = value;
	}
}
