package edu.mtu.simulation;

public class ChemSimProperties {
	
	// Initial settings for the model
	private int moleculesPerMole = 100; 
	private int acetateMoles = 1;
	private int hydrogenPeroxideMoles = 15;
	private double hydroxylAppearanceProbability = 0.05;
	private double uvIntensity = 0.025;
		
	// TODO Figure out a way to expose this as an actual MASON inspector
	public CompoundInspector getCompoundInspector() {
		return new CompoundInspector();
	}
	
	public int getAcetateMoles() {
		return acetateMoles;
	}

	public int getHydrogenPeroxideMoles() {
		return hydrogenPeroxideMoles;
	}
	
	public double getHydroxylAppearanceProbability() {
		return hydroxylAppearanceProbability;
	}

	public int getMoleculesPerMole() {
		return moleculesPerMole;
	}

	public double getUvIntensity() {
		return uvIntensity;
	}

	public void setAcetateMoles(int value) {
		acetateMoles = value;
	}
		
	public void setHydrogenPeroxideMoles(int value) {
		hydrogenPeroxideMoles = value;
	}
	
	public void setHydroxylAppearanceProbability(double value) {
		this.hydroxylAppearanceProbability = value;
	}
	
	public void setMoleculesPerMole(int value) {
		moleculesPerMole = value;
	}

	public void setUvIntensity(double value) {
		this.uvIntensity = value;
	}	
}
