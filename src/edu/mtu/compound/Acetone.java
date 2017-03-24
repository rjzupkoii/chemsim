package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Acetone extends Compound {

	public Acetone(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.BLUE;
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
		// CH3COCH3 (acetone) + HO* (hydroxyl radical) → CH3COCHO (pyruvic aldehyde) + CH3COCH2OH (hydroxyacetone) + HCHO (formaldehyde)
		if (compound.getClass() == Hydroxyl.class) {
			generate(PyruvicAldehyde.class);
			generate(Hydroxyacetone.class);
			generate(Formaldehyde.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
