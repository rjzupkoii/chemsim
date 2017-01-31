package edu.mtu.compound.radical;

import java.awt.Color;

import edu.mtu.simulation.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Hydroxyl extends Compound {

	public Hydroxyl(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.CYAN;
	}

	// Do nothing
	@Override
	protected void doUVExposure() { }
}
