package edu.mtu.simulation;

public class ChemSimProperties {
	
	// Initial settings for the model
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	
	
	private int acetateMoles = 1;
	private int hydrogenPeroxideMoles = 15;
	private int moleculesPerMole = 100;
	
	private double hydroxylAppearanceProbability = 0.05;
	private double oxygenSaturation = 1.0;
	private double uvIntensity = 0.025;
		
	// TODO Figure out a way to expose this as an actual MASON inspector
	public CompoundInspector getCompoundInspector() {
		return new CompoundInspector();
	}
	
	public int getAcetateMoles() {
		return acetateMoles;
	}

	public int getFastPathway() {
		return fastPathway;
	}
	
	public int getHydrogenPeroxideMoles() {
		return hydrogenPeroxideMoles;
	}
	
	public double getHydroxylAppearanceProbability() {
		return hydroxylAppearanceProbability;
	}

	public int getMediumPathway() {
		return mediumPathway;
	}
	
	public int getMoleculesPerMole() {
		return moleculesPerMole;
	}
	
	public double getOxygenSaturation() {
		return oxygenSaturation;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}

	public double getUvIntensity() {
		return uvIntensity;
	}

	public void setAcetateMoles(int value) {
		acetateMoles = value;
	}
	
	public void setFastPathway(int fastPathway) {
		this.fastPathway = fastPathway;
	}
		
	public void setHydrogenPeroxideMoles(int value) {
		hydrogenPeroxideMoles = value;
	}
	
	public void setHydroxylAppearanceProbability(double value) {
		this.hydroxylAppearanceProbability = value;
	}
	
	public void setMediumPathway(int mediumPathway) {
		this.mediumPathway = mediumPathway;
	}
	
	public void setMoleculesPerMole(int value) {
		moleculesPerMole = value;
	}

	public void setOxygenSaturation(double value) {
		oxygenSaturation  = value;
	}
	
	public void setSlowPathway(int slowPathway) {
		this.slowPathway = slowPathway;
	}	
	
	public void setUvIntensity(double value) {
		this.uvIntensity = value;
	}
}
