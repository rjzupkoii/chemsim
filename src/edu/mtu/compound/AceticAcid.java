package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Acetic Acid
 */
@SuppressWarnings("serial")
public class AceticAcid extends Compound {

	public AceticAcid(Int3D movementVector) {
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
		// CH3COOH + HO* → HOCCOOH (glyoxyalic acid) + HCOOH (formic acid)
		if (compound.getClass() == Hydroxyl.class) {
			generate(GlyoxyalicAcid.class);
			generate(FormicAcid.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
