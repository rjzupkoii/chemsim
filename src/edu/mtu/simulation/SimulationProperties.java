package edu.mtu.simulation;

/**
 * This class contains various properties related to how the simulation should run 
 * and be managed. Note that the should not change once the application has been
 * started.
 */
public class SimulationProperties {
	
	// Flag for if results should be overwritten
	private boolean overWriteResults = true;
		
	// Initial number of molecules in the model
	private int initialMolecules = 0;
	
	// The time, in seconds, to pad the model by (15 minutes default) 
	private int timePadding = 900;
	
	// How many time steps between reports to console
	private int reportInterval = 60;	// 60 iterations = 1 minute at default deltaT
	
	// Length of a time step in seconds
	private double deltaT = 1;
		
	// List of entities to terminate when zero
	private String[] terminateOn = new String[0]; 
	
	// Default paths to experiments
	private String chemicalsFileName = "";
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
	
	public double getDeltaT() {
		return deltaT;
	}
		
	public String getChemicalsFileName() {
		return chemicalsFileName;
	}
		
	public String getMolarFileName() {
		return molarFileName;
	}
	
	public int getInitialMolecules() {
		return initialMolecules;
	}
			
	public boolean getOverWriteResults() {
		return overWriteResults;
	}
	
	public int getPadding() {
		return timePadding;
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
	
	public String[] getTerminationOn() {
		return terminateOn;
	}

	public void setChemicalsFileName(String value) {
		chemicalsFileName = value;
	}
			
	public void setMolarFileName(String value) {
		molarFileName = value;
	}
	
	public void setOverWriteResults(boolean value) {
		overWriteResults = value;
	}
	
	public void setInitialMolecules(int value) {
		initialMolecules = value;
	}
	
	public void setPadding(int value) {
		timePadding = value;
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
	
	public void setTerminateOn(String[] value) {
		terminateOn = value;
	}
	
	public void setDeltaT(double value) {
		deltaT = value;
	}
}
