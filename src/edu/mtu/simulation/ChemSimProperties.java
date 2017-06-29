package edu.mtu.simulation;

public class ChemSimProperties {
	
	// Initial settings for the model
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	
	
	private int moleculesPerMole = 10;
	
	private double oxygenSaturation = 1.0;
	private double uvIntensity = 0.025;
	
	private String chemicalsFileName = "tests/chemicals.csv";
	private String reactionsFileName = "tests/reactions.csv";
				
	// TODO Figure out a way to expose this as an actual MASON inspector
	public CompoundInspector getCompoundInspector() {
		return new CompoundInspector();
	}
	
	public String getChemicalsFileName() {
		return chemicalsFileName;
	}

	public int getFastPathway() {
		return fastPathway;
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
	
	public String getReactionsFileName() {
		return reactionsFileName;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}

	public double getUvIntensity() {
		return uvIntensity;
	}
	
	public void setChemicalsFileName(String value) {
		chemicalsFileName = value;
	}
	
	public void setFastPathway(int value) {
		fastPathway = value;
	}
			
	public void setMediumPathway(int value) {
		mediumPathway = value;
	}
	
	public void setMoleculesPerMole(int value) {
		moleculesPerMole = value;
	}

	public void setOxygenSaturation(double value) {
		oxygenSaturation  = value;
	}
	
	public void setReactionsFileName(String value) {
		reactionsFileName = value;
	}
	
	public void setSlowPathway(int value) {
		slowPathway = value;
	}	
	
	public void setUvIntensity(double value) {
		uvIntensity = value;
	}
}
