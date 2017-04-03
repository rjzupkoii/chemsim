package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Pyruvic Aldehyde
 */
@SuppressWarnings("serial")
public class PyruvicAldehyde extends Compound {

	public PyruvicAldehyde(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.CYAN;
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
		// CH3COCHO (pyruvic aldehyde) + HO* (hydroxyl radical) → CH3COCOOH (pyruvic acid)
		// CH3COCHO (pyruvic aldehyde) + HO* (hydroxyl radical) → CH3COOH (acetic acid)
		if (compound.getClass() == Hydroxyl.class) {
			generate(PyruvicAcid.class);
			generate(AceticAcid.class);
			decay(compound);
			decay(this);
			return true;
		}		
		
		return false;
	}
}
