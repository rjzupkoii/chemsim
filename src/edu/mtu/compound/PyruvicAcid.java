package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Pyruvic Acid
 */
@SuppressWarnings("serial")
public class PyruvicAcid extends Compound {

	public PyruvicAcid(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.RED;
	}
	
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) {	}

	@Override
	protected void doOxidation(MersenneTwisterFast random) {	}

	@Override
	protected void doUVExposure(MersenneTwisterFast random) {	}

	@Override
	protected boolean interact(Compound compound) {
		// CH3COCOOH (pyruvic acid) + HO* (hydroxyl radical) → CH3COOH (acetic acid)
		if (compound.getClass() == Hydroxyl.class) {
			generate(AceticAcid.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}