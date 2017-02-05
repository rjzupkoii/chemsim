package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

/**
 * Carbon Centered Radical 
 */
@SuppressWarnings("serial")
public class CarbonCenteredRadical extends Compound {

	public CarbonCenteredRadical(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.DARK_GRAY;
	}
	
	// Do nothing
	@Override
	protected void doUVExposure(MersenneTwisterFast random) { }

	@Override
	protected void interact(Compound compound) {
		// TODO Update this with the relevent reactions
	}
}
