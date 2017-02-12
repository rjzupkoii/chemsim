package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

/**
 * Oxyl radical
 * @author Robert Zupko
 *
 */
@SuppressWarnings("serial")
public class Oxyl extends Compound {

	public Oxyl(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.ORANGE;
	}
	
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doOxidation(MersenneTwisterFast random) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doUVExposure(MersenneTwisterFast random) {
		// TODO Auto-generated method stub
	}

	@Override
	protected boolean interact(Compound compound) {
		// TODO Auto-generated method stub
		return false;
	}
}
