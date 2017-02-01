package edu.mtu.simulation;

public class ChemSimProperties {
	
	// Initial settings for the model
	private int moleculesPerMole = 100; 
	private int acetateMoles = 1;
	private int hydrogenPeroxideMoles = 15;
	private double uvIntensity = 0.025;
		
	public int getAcetateMoles() {
		return acetateMoles;
	}

	public int getHydrogenPeroxideMoles() {
		return hydrogenPeroxideMoles;
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
	
	public void setMoleculesPerMole(int value) {
		moleculesPerMole = value;
	}

	public void setUvIntensity(double uvIntensity) {
		this.uvIntensity = uvIntensity;
	}
}
