package edu.mtu.simulation;

public class ChemSimProperties {
	
	// Default time step durations for disproporting species
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	
	
	// Flag for if results should be overwritten
	private boolean overWriteResults = true;
	
	// Default for how fast hydrogen peroxide decays into hydroxyl radicals for the reactor
	private double hydrogenPeroxideDecay = 3.0 * Math.pow(10, -7);		// mol/L*sec
	
	// Adjustment to be applied to hydroxyl based upon how much is available for use in the system
	private double hydroxylAdjustment = 0.18;
	
	// Assume no adjustment to acetone
	private double acetoneAdjustment = 1.0;	
	public double getAcetoneAdjustment() { return acetoneAdjustment; }
	public void setAcetonAdjustment(double value) { acetoneAdjustment = value; }
	
	// Reactor volume in liters
	private double reactorVolume = 1.8;

	// Number of cells along one axis
	private int cellCount = 25;
	
	// Default paths to experiments
	private String chemicalsFileName = "experiment/chemicals.csv";
	private String reactionsFileName = "experiment/reactions.csv";
							
	public int getCellCount() {
		return cellCount;
	}
	
	public String getChemicalsFileName() {
		return chemicalsFileName;
	}

	public int getFastPathway() {
		return fastPathway;
	}
	
	/**
	 * H2O2 + UV -> HO* decay in mol/sec
	 */
	public double getHydrogenPeroxideDecay() {
		return hydrogenPeroxideDecay;
	}

	public double getHydroxylAdjustment() {
		return hydroxylAdjustment;
	}
	
	public int getMediumPathway() {
		return mediumPathway;
	}
			
	public boolean getOverWriteResults() {
		return overWriteResults;
	}
	
	public String getReactionsFileName() {
		return reactionsFileName;
	}
	
	public double getReactorVolume() {
		return reactorVolume;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}
	
	public void setCellCount(int value) {
		cellCount = value;
	}
	
	public void setChemicalsFileName(String value) {
		chemicalsFileName = value;
	}
	
	public void setFastPathway(int value) {
		fastPathway = value;
	}
			
	public void setHydrogenPeroxideDecay(double value) {
		hydrogenPeroxideDecay = value;
	}
		
	public void setHydroxylAdjustment(double value) {
		hydroxylAdjustment = value;
	}
	
	public void setMediumPathway(int value) {
		mediumPathway = value;
	}

	public void setOverWriteResults(boolean value) {
		overWriteResults = value;
	}
	
	public void setReactionsFileName(String value) {
		reactionsFileName = value;
	}
	
	public void setReactorVolume(double value) {
		reactorVolume = value;
	}
	
	public void setSlowPathway(int value) {
		slowPathway = value;
	}
}
