package edu.mtu.simulation;

import edu.mtu.Reactor.Reactor;

public class ChemSimProperties {
	
	// Default time step durations for disproporting species
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	
	
	// Flag for if results should be overwritten
	private boolean overWriteResults = true;
	
	// Default for how fast hydrogen peroxide decays into hydroxyl radicals for the reactor
	private double hydrogenPeroxideDecay = 3.0 * Math.pow(10, -7);		// mol/L*sec
	
	// Reactor volume in liters
	private double reactorVolume = 1.8;

	// Number of cells along one axis
	private int cellCount = 5;
	
	// Default paths to experiments
	private String chemicalsFileName = "experiment/chemicals.csv";
	private String reactionsFileName = "experiment/reactions.csv";
						
	// TODO Figure out a way to expose this as an actual MASON inspector
	public CompoundInspector getCompoundInspector() {
		return new CompoundInspector();
	}
	
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

	public int getMediumPathway() {
		return mediumPathway;
	}
	
	public double getMoleculesPerMole() {
		return Reactor.getInstance().getAvogadroNumber();
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
