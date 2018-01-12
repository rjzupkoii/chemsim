package edu.mtu.simulation.schedule;

public abstract class Steppable extends Node {
	
	public boolean deleted = false;
	
	public abstract void doAction();

}
