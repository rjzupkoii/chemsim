package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Hydroxyl extends Compound {

	public Hydroxyl(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.RED;
	}
	
	// Do nothing
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) { }

	// Do nothing
	@Override
	protected void doOxidation(MersenneTwisterFast random) { }
	
	// Do nothing
	@Override
	protected void doUVExposure(MersenneTwisterFast random) { }

	@Override
	protected boolean interact(Compound compound) {
		// TODO Implement interaction pathways
		return false;
	}
}
