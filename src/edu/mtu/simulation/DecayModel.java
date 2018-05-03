package edu.mtu.simulation;

import java.io.IOException;

public interface DecayModel {

	/**
	 * Estimate how long the model needs to run for.
	 * 
	 * @return The estimated number of time steps.
	 */
	public long estimateRunningTime();
	
	/**
	 * Get the decay quantity for the current time step based upon experimental results.
	 * 
	 * @param timeStep The current time step of the model.
	 * @param compound to get the decay quantity for.
	 * @param moleclues The current number of molecules.
	 * @return The number of the molecules that should decay.
	 */
	public long getDecayQuantity(long timeStep, String compound, long moleclues);
	
	/**
	 * Prepare the decay model by loading the experimental results, 
	 * and then calculating out the slope and decay rate in molecules.
	 * 
	 * @param fileName to load the data from.
	 */
	public void prepare(String fileName) throws IOException;
}
