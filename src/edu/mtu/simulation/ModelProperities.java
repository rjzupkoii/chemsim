package edu.mtu.simulation;

/**
 * This class contains various properties related to how the model should behave. 
 * These properties are managed by the ChemSim class and may change during model
 * execution or between model runs.
 */
public class ModelProperities {
	// Default time step durations for disproporting species
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	
	
	// Default for how fast hydrogen peroxide decays into hydroxyl radicals for the reactor
	private double hydrogenPeroxideDecay = 0.15;		// TODO Fix this, again. 
			
	// Reactor volume in liters
	private double reactorVolume;

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

	public double getReactorVolume() {
		return reactorVolume;
	}
	
	public int getSlowPathway() {
		return slowPathway;
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

	public void setReactorVolume(double value) {
		reactorVolume = value;
	}
	
	public void setSlowPathway(int value) {
		slowPathway = value;
	}
}
