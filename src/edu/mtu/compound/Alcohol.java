package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Alcohol
 */
@SuppressWarnings("serial")
public class Alcohol extends Compound {

	public Alcohol(Int3D movementVector) {
		super(movementVector);
		// TODO Auto-generated constructor stub
	}
	
	public static Color getColor() {
		return Color.YELLOW;
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
