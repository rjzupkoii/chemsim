package edu.mtu.simulation;

import sim.display.Console;
import sim.display.GUIState;
import sim.engine.SimState;

public class ChemSimUI extends GUIState {

	/**
	 * Constructor.
	 */
	public ChemSimUI() {
		super(new ChemSim(System.currentTimeMillis()));
	}
	
	/**
	 * Constructor.
	 */
	public ChemSimUI(SimState state) {
		super(state);
	}

	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		ChemSimUI chemSimUI = new ChemSimUI();
		Console console = new Console(chemSimUI);
		console.setVisible(true);
	}
}
