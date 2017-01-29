package edu.mtu.simulation;

import sim.engine.SimState;

@SuppressWarnings("serial")
public class ChemSim extends SimState {

	/**
	 * Constructor.
	 */
	public ChemSim(long seed) {
		super(seed);
	}
	
	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		doLoop(ChemSim.class, args);
		System.exit(0);
	}
}
