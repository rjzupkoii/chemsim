package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Formaldehyde
 */
@SuppressWarnings("serial")
public class Formaldehyde extends Compound {

	public Formaldehyde(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.CYAN;
	}
	
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) {	}

	@Override
	protected void doOxidation(MersenneTwisterFast random) {	}

	@Override
	protected void doUVExposure(MersenneTwisterFast random) {	}

	@Override
	protected boolean interact(Compound compound) {
		// HCHO (formaldehyde) + HO* → HCOOH (formic acid)
		if (compound.getClass() == Hydroxyl.class) {
			generate(FormicAcid.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
