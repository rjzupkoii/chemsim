package edu.mtu.simulation;

import edu.mtu.simulation.decay.DecayModel;

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
		
	// The expected duration of the model in time steps
	private int timeSteps;
		
	// Values needed to ensure hydrogen peroxide exhibits a linear decay based upon the UV intensity
	private double hydrogenPeroxideDecay = 0;
	
	// Scalar needed to convert molecules to molar values
	private double moleculeToMol = 0;
	
	// Decay model to use for the model, may be null
	private DecayModel decayModel = null;
	
	public int getFastPathway() {
		return fastPathway;
	}
	
	public DecayModel getDecayModel() {
		return decayModel;
	}
	
	public double getHydrogenPeroxideDecay() {
		return hydrogenPeroxideDecay;
	}
	
	public int getMediumPathway() {
		return mediumPathway;
	}
	
	public double getMoleculeToMol() {
		return moleculeToMol;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}
		
	public int getTimeSteps() {
		return timeSteps;
	}
	
	public void setDecayModel(DecayModel value) {
		decayModel = value;
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
	
	public void setMoleculeToMol(double value) {
		moleculeToMol = value;
	}
	
	public void setSlowPathway(int value) {
		slowPathway = value;
	}
	
	public void setTimeSteps(int value) {
		timeSteps = value;
	}
}
