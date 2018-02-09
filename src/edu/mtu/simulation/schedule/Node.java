package edu.mtu.simulation.schedule;

/**
 * The base class for nodes in the schedule.
 */
public class Node {
	// Flag to indicate if the node has been deleted
	public boolean deleted = false;
	
	// Pointer to the next node
	public Node next = null;
	
}
