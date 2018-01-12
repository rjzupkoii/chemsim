package edu.mtu.simulation.schedule;

/**
 * This interface defines the methods that are needed by Schedule to signal when
 * various operations are taking place.
 */
public interface Simulation {
	/**
	 * Invoked when the scheduler has been stopped.
	 * 
	 * @param terminated True when the schedule was terminated during a time step, false otherwise.
	 */
	public void complete(boolean terminated);
	
	/**
	 * Invoked when a time step has been completed.
	 */
	public void doTimeStep(); 
}
