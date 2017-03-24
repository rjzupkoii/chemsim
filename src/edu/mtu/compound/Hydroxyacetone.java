package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

/**
 * Hydroxyacetone 
 */
@SuppressWarnings("serial")
public class Hydroxyacetone extends Compound {

	public Hydroxyacetone(Int3D movementVector) {
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
		// CH3COCH2OH (hydroxyacetone) + HO* → HCHO (formaldehyde)
		if (compound.getClass() == Hydroxyl.class) {
			generate(Formaldehyde.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
