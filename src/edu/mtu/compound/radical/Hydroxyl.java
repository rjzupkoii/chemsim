package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.Acetate;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Hydroxyl extends Compound {

	public Hydroxyl(Int3D movementVector) {
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
		// TODO This is just a duplication of Acetate.interact which is a good sign
		// that we need to refactor how interactions are done
	
		// Acetate + Hydroxyl -> Propylium, 2-oxo-
		if (compound.getClass() == Acetate.class) {
			generate(CarbonCentered.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
