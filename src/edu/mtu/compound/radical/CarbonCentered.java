package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

/**
 * Carbon Centered Radical 
 */
@SuppressWarnings("serial")
public class CarbonCentered extends Compound {

	public CarbonCentered(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.DARK_GRAY;
	}
	
	// Do nothing
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) { }
	
	// Do nothing
	@Override
	protected void doUVExposure(MersenneTwisterFast random) { }

	// Do nothing
	@Override
	protected boolean interact(Compound compound) { 
		return false;
	}

	@Override
	protected void doOxidation(MersenneTwisterFast random) {
		generate(Peroxy.class);
		decay(this);		
	}
}
