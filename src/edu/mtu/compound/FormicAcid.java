package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Formic Acid
 */
@SuppressWarnings("serial")
public class FormicAcid extends Compound {
	
	public FormicAcid(Int3D movementVector) {
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
		// HCOOH + HO* → O2 + CO2
		if (compound.getClass() == Hydroxyl.class) {
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
