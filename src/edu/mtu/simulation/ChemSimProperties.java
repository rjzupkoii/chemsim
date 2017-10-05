package edu.mtu.simulation;

import edu.mtu.Reactor.Reactor;

public class ChemSimProperties {
	
	// Default time step durations for disproporting species
	private int fastPathway = 1;
	private int mediumPathway = 2;
	private int slowPathway = 3;	

	// Default for how many hydrogen peroxide molecules decay into hydroxyl radicals
	private int hydrogenPeroxideRatio = 10;
	
	// Default for how fast hydrogen peroxide decays into hydroxyl radicals
	private int hydrogenPeroxideDecay = 100;
	
	// Default paths to experiments
	private String chemicalsFileName = "experiment/chemicals.csv";
	private String reactionsFileName = "experiment/reactions.csv";
						
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
	
	public int getHydrogenPeroxideDecay() {
		return hydrogenPeroxideDecay;
	}

	public int getHydrogenPeroxideRatio() {
		return hydrogenPeroxideRatio;
	}
	
	public int getMediumPathway() {
		return mediumPathway;
	}
	
	public long getMoleculesPerMole() {
		return Reactor.AvogadroNumber;
	}
		
	public String getReactionsFileName() {
		return reactionsFileName;
	}
	
	public int getSlowPathway() {
		return slowPathway;
	}
	
	public void setChemicalsFileName(String value) {
		chemicalsFileName = value;
	}
	
	public void setFastPathway(int value) {
		fastPathway = value;
	}
			
	public void setHydrogenPeroxideDecay(int value) {
		hydrogenPeroxideDecay = value;
	}
	
	public void setHydrogenPeroxideRatio(int value) {
		hydrogenPeroxideRatio = value;
	}
	
	public void setMediumPathway(int value) {
		mediumPathway = value;
	}

	public void setReactionsFileName(String value) {
		reactionsFileName = value;
	}
	
	public void setSlowPathway(int value) {
		slowPathway = value;
	}
}
