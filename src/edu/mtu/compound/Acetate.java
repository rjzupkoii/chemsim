package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.radical.CarbonCentered;
import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Acetate extends Compound {

	public Acetate(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.CYAN;
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
		// Acetate + Hydroxyl -> Propylium, 2-oxo-
		if (compound.getClass() == Hydroxyl.class) {
			generate(CarbonCentered.class);
			decay(compound);
			decay(this);
			return true;
		}
		
		return false;
	}
}
