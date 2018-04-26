package edu.mtu.simulation;

/**
 * This class contains various properties related to how the simulation should run 
 * and be managed. Note that the should not change once the application has been
 * started.
 */
public class SimulationProperties {

	// Flag to indicate no limit on the number of molecules
	public static final long NO_LIMIT = -1;
	
	// Flag for if results should be overwritten
	private boolean overWriteResults = true;
	
	// Limit to the number of molecules in the model, -1 means no limit
	//private long moleculeLimit = NO_LIMIT;
	private long moleculeLimit = 1000000;
				
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
	
	public String getChemicalsFileName() {
		return chemicalsFileName;
	}
	
	public String getMolarFileName() {
		return molarFileName;
	}
	
	public long getMoleculeLimit() {
		return moleculeLimit;
	}
			
	public boolean getOverWriteResults() {
		return overWriteResults;
	}
	
	public String getReactionsFileName() {
		return reactionsFileName;
	}
	
	public String getResultsFileName() {
		return resultsFileName;
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
	
	public void setMoleculeLimit(long value) {
		moleculeLimit = value;
	}
	
	public void setReactionsFileName(String value) {
		reactionsFileName = value;
	}
	
	public void setResultsFileName(String value) {
		resultsFileName = value;
	}
}
