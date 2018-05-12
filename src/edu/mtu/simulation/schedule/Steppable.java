package edu.mtu.simulation.schedule;

/**
 * The base class for agents in the schedule.
 */
public abstract class Steppable {

	private boolean active = true;
	
	public abstract void doAction();

	public boolean isActive() {
		return active;
	}
	
	public void deactivate() {
		active = false;
	}
}
