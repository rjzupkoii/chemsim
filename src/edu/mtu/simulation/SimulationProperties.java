package edu.mtu.simulation;

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
	
	// The time, in minutes, to pad the model by 
	private int timePadding = 60;
	
	// Interval to report to the console and save data on
	private int reportInterval = 100;
	
	// Length of one time step in seconds
	private double timeStepLength = 60;
	
	// List of entities to terminate when zero
	private String[] terminateOn = new String[0]; 
	
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
	
	public void setTerminateOn(String[] value) {
		terminateOn = value;
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
	
	public double getTimeStepLength() {
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
	
	public void setTimeStepLength(double value) {
		timeStepLength = value;
	}
}
