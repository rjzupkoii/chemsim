package edu.mtu.simulation.schedule;

import java.util.ArrayDeque;
import java.util.ArrayList;

import edu.mtu.simulation.ChemSim;

/**
 * The schedule is based upon a ring buffer, but modified so that there is always
 * a marker node that ensures a callback is made to the simulation.  
 */
public class Schedule {
		
	// Flags to indicate shutdown
	private boolean halt;				// Shut down now
	private boolean stopping;			// Shut down at end of time ste	
	private boolean stopped;			// Schedule is complete
	
	// Current time step of the schedule
	private int timeStep;
	
	// Pointer to the simulation
	private Simulation simulation;
	
	private ArrayDeque<Steppable> schedule;
	private ArrayList<Steppable> pending;
		
	/**
	 * Constructor.
	 */
	public Schedule() {
		schedule = new ArrayDeque<Steppable>();
		pending = new ArrayList<Steppable>();
		stopped = true;
	}
	
	/**
	 * Get the count of nodes in the schedule.
	 */
	public int getCount() {
		return schedule.size() + pending.size();
	}
	
	/**
	 * Get the current time step number.
	 */
	public int getTimeStep() {
		return timeStep;
	}
	
	/**
	 * Clears the schedule completely, simulation finish will be called.
	 */
	public void halt() {
		// Set the flags
		halt = true;
		stopped = true;
		
		// Clear the arrays
		schedule.clear();
		pending.clear();
		
		// Call the finish method
		simulation.finish(halt);
	}
	
	/**
	 * Add a new steppable to the next time step.
	 */
	public void insert(Steppable steppable) {
		pending.add(steppable);
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
		
		// Check for illegal states
		if (schedule.size() > 0) {
			throw new IllegalStateException("Schedule variable shoud be empty when starting the simulation!");
		}
		
		// Set the relevant flags and pointers
		halt = false;
		stopped = false;
		stopping = false;
		timeStep = 0;
		this.simulation = simulation;
		
		// Prepare the pending data by first shuffling it to remove any basis
		shuffle(pending);
		
		// Update the schedule, clear pending
		schedule.addAll(pending);
		pending.clear();
		
		// Run the schedule
		while (getCount() != 0) {
			// If the schedule is empty then the time step is complete
			if (schedule.size() == 0) {
				// Update the time step, inform the simulation, exit if we are done 
				timeStep++;
				simulation.step(timeStep, runTill);
				if (timeStep == runTill || stopping) {
					break;
				}
				
				// Shuffle and run
				shuffle(pending);
				schedule.addAll(pending);
				pending.clear();				
			}
			
			// Otherwise, run the time step
			Steppable steppable = schedule.remove();
			if (steppable.isActive()) {
				steppable.doAction(timeStep);
				pending.add(steppable);
 			}
		}
			
		// Perform clean-up operations
		if (!halt) {
			simulation.finish(halt);
		}
		stopped = true;
	}
	
	/**
	 * Perform a Fisher–Yates shuffle on the steppables to remove possible bias
	 * due to the activation order.
	 */
	private void shuffle(ArrayList<Steppable> steppables) {
		for (int ndx = steppables.size() - 1; ndx > 0; ndx--)
	    {
	      int index = ChemSim.getRandom().nextInt(ndx + 1);
	      Steppable swap = steppables.get(index);
	      steppables.set(index, steppables.get(ndx));
	      steppables.set(ndx, swap);
	    }
	}
	
	/**
	 * Signals the schedule to that it should stop at the end of the current time step.
	 */
	public void stop() {
		stopping = true;
	}
	
	/**
	 * Returns true if the schedule is stopped, false otherwise.
	 */
	public boolean stopped() {
		return stopped;
	}
}
