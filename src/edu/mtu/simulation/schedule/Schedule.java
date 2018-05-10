package edu.mtu.simulation.schedule;

/**
 * The schedule is based upon a ring buffer, but modified so that there is always
 * a marker node that ensures a callback is made to the simulation.  
 */
public class Schedule {
	
	private boolean stopping;
	private boolean stop;
	
	private int count;
	private int timeStep;
	
	private Node current;
	private Node last;
	
	/**
	 * Constructor.
	 */
	public Schedule() {
		// We are stopped by default
		stop = true;
		
		// Create the marker node
		last = new Node();
		last.next = last;
		current = last;
	}
	
	/**
	 * Get the count of nodes in the schedule.
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * Get the current time step number.
	 */
	public int getTimeStep() {
		return timeStep;
	}
	
	/**
	 * Insert a new node at the beginning of the next time step.
	 */
	public void insert(Node node) {
		node.next = last.next;
		last.next = node;
		count++;
	}
		
	/**
	 * Remove the node indicated from the schedule.
	 */
	public void remove(Node node) {
		node.deleted = true;		
		count--;
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
		
		Node previous = last;		
		while (!stop) {
			current = current.next;
			
			// If this is a steppable, then clear it if it has been deleted, otherwise do the action
			if (current instanceof Steppable) {
				if (current.deleted) {
					previous.next = current.next;
					continue;
				}
				((Steppable)current).doAction();
				previous = current;
				continue;
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
