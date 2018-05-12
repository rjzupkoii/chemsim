package edu.mtu.simulation.schedule;

import java.util.ArrayList;

/**
 * The schedule is based upon a ring buffer, but modified so that there is always
 * a marker node that ensures a callback is made to the simulation.  
 */
public class Schedule {
	
	private boolean stopping;
	private boolean stop;
	
	private int timeStep;
	
	private ArrayList<Steppable> active;
	private ArrayList<Steppable> next = new ArrayList<Steppable>();
		
	/**
	 * Constructor.
	 */
	public Schedule() {
		// We are stopped by default
		stop = true;
	}
	
	/**
	 * Get the count of nodes in the schedule.
	 */
	public int getCount() {
		return active.size() + next.size();
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
		next.add(steppable);
	}
		
	/**
	 * Remove the node indicated from the schedule.
	 */
	public void remove(Steppable steppable) {
		if (active.contains(steppable)) {
			steppable.deactivate();
		} else {
			next.remove(steppable);
		}
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
		
		// Set the termination flags
		stopping = false;
		stop = false;
		
		// Set the current time step
		timeStep = 0;			
		while (!stop) {
			active = new ArrayList<Steppable>(next);
			next.clear();
			System.out.println(getCount());
			
			if (active.size() == 0) {
				break;
			}
			for (Steppable steppable : active) {
				if (steppable.isActive()) {
					steppable.doAction();	
				}
			}
			for (Steppable steppable : active) {
				if (steppable.isActive()) {
					next.add(steppable);
				}
			}

			
			// If we are here then this must have been the end of time step marker
			timeStep++;
			simulation.step(timeStep, runTill);
			if (timeStep == runTill || stopping) {
				stop = true;
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
	}
}
