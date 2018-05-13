package edu.mtu.simulation.schedule;

import java.util.ArrayDeque;

/**
 * The schedule is based upon a ring buffer, but modified so that there is always
 * a marker node that ensures a callback is made to the simulation.  
 */
public class Schedule {
	
	private class Escapement extends Steppable {
		@Override
		public void doAction() { } 
	}
	
	private boolean stopping;
	private boolean stop;
	
	private int timeStep;
	
	private ArrayDeque<Steppable> schedule;
	
	/**
	 * Constructor.
	 */
	public Schedule() {
		schedule = new ArrayDeque<Steppable>();
		stop = true;
	}
	
	/**
	 * Get the count of nodes in the schedule.
	 */
	public int getCount() {
		return schedule.size();
	}
	
	/**
	 * Get the current time step number.
	 */
	public int getTimeStep() {
		return timeStep;
	}
	
	/**
	 * Insert a new steppable at the beginning of the next time step.
	 */
	public void insert(Steppable steppable) {
		schedule.add(steppable);
	}
		
	/**
	 * Remove the node indicated from the schedule.
	 */
	public void remove(Steppable steppable) {
		steppable.deactivate();
	}
	
	/**
	 * 
	 * @param simulation
	 * @param runTill
	 */
	public void start(Simulation simulation, int runTill) {
		// Check to make sure a simulation was provided
		if (simulation == null) {
			throw new IllegalArgumentException("The simulation cannot be null");
		}
		
		// Prepare to run, set the flags, time step
		stopping = false;
		stop = false;
		timeStep = 0;			
		schedule.add(new Escapement());
		
		while (schedule.size() > 0) {			
			Steppable steppable = schedule.remove();			
			if (steppable instanceof Escapement) {
				timeStep++;
				simulation.step(timeStep, runTill);
				if (timeStep == runTill || stopping) {
					schedule.clear();
					break;
				}
				schedule.add(steppable);
			} else if (steppable.isActive()) {
				steppable.doAction();
				schedule.add(steppable);
			}
		}
	
		// Perform clean-up operations
		simulation.finish(stop);
	}
	
	/**
	 * Signals the schedule to that it should stop at the end of the current time step.
	 */
	public void stop() {
		stopping = true;
	}
	
	/**
	 * Signals that the schedule should immediately terminate operation.
	 */
	public void terminate() {
		stop = true;
		schedule.clear();
	}
}
