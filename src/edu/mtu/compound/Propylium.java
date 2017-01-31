package edu.mtu.compound;

import java.awt.Color;

import edu.mtu.simulation.Compound;
import sim.util.Int3D;

/**
 * Propylium, 2-oxo- 
 */
@SuppressWarnings("serial")
public class Propylium extends Compound {

	public Propylium(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.GREEN;
	}
	
	// Do nothing
	@Override
	protected void doUVExposure() { }

	@Override
	protected void interact(Compound compound) {
		// TODO Determine what interactions Hydrogen Peroxide has
	}
}
