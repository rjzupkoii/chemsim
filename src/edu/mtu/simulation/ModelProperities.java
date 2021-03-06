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
				
	private double decayProbability;
	
	// The odds that hydroxyl will be retained in the model
	private double hydroxylRetention = 1.0; 
	
	// Scalar needed to convert molecules to molar values
	private double moleculeToMol = 0;
	
	// pH of the model
	private final double pH = 7.0;
	
	// Decay model to use for the model, may be null
	private DecayModel decayModel = null;
	
	public int getFastPathway() {
		return fastPathway;
	}
	
	public DecayModel getDecayModel() {
		return decayModel;
	}
		
	public double getHydroxylRetention() {
		return hydroxylRetention;
	}
	
	public int getMediumPathway() {
		return mediumPathway;
	}
	
	public double getMoleculeToMol() {
		return moleculeToMol;
	}
	
	public double getPH() {
		return pH;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}
		
	public int getTimeSteps() {
		return timeSteps;
	}
		
	public double getDecayProbability() {
		return decayProbability;
	}

	public void setDecayModel(DecayModel value) {
		decayModel = value;
	}
		
	public void setFastPathway(int value) {
		fastPathway = value;
	}
		
	public void setHydroxylRetention(double value) {
		hydroxylRetention = value;
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
	
	public void setDecayProbability(double value) {
		decayProbability = value;
	}
}
