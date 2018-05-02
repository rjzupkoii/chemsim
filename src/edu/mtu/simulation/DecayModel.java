package edu.mtu.simulation;

import java.util.Map;

/**
 * This class provides a model for photolysis based upon experimentally 
 * observed data.  
 */
public class DecayModel {
	private int calculated;
	private Map<Integer, Double> slope;
	private Map<Integer, Double> mols;
	
	/**
	 * Get the decay quantity for the current time step based upon experimental results.
	 * 
	 * @param timeStep The current time step of the model.
	 * @param moleclues The current number of molecules.
	 * @param volume The volume of the experimental reactor.
	 * @return The number of the molecules that should decay.
	 */
	public int getDecayQuantity(int timeStep, long moleclues, double volume) {
		double slope = 0, mols = 0;
				
		// Check to see if we need to update the calculation
		if (this.slope.containsKey(timeStep)) {
			slope = this.slope.get(timeStep);
			mols = this.mols.get(timeStep);
			
			// Calculate out the number of molecules, p_q  = (molecules * slope * volume) / mols
			calculated = (int)Math.ceil(Math.abs((moleclues * slope * volume) / mols)) * ChemSim.SCALING;
		}
		
		return calculated;
	}
		
	/**
	 * Prepare the decay model by loading the experimental results, 
	 * and then calculating out the slope and decay rate in molecules.
	 * 
	 * @param fileName to load the data from.
	 */
	public void prepare(String fileName, double volume) {
		
		// Calculate the slope, m = (y2 - y1) / (x2 - x1)
		
		// Calculate the mols, mols = value * volume * 1000
		
		// Store the slope and mols for later calculation
		
	}
}
