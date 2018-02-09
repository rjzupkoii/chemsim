package edu.mtu.simulation.schedule;

/**
 * This interface defines the methods that are needed by Schedule to signal when
 * various operations are taking place.
 */
public interface Simulation {
	/**
	 * Initialize the simulation using the seed provided for the random number generator. 
	 */
	public void initialize(long seed);
	
	/**
	 * Start the simulation, run for the given number of time steps.
	 */
	public void start(int timeSteps);
	
	/**
	 * Invoked when one time step has been completed.
	 * 
	 * @param count The current time step.
	 * @param total The total time steps to be completed.
	 */
	public void step(int count, int total);
	
	/**
	 * Invoked when the scheduler has been stopped.
	 * 
	 * @param terminated True when the schedule was terminated during a time step, false otherwise.
	 */
	public void finish(boolean terminated);
}
