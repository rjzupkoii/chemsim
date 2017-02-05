package edu.mtu.compound;

import java.awt.Color;

import edu.mtu.compound.radical.CarbonCenteredRadical;
import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class Acetate extends Compound {

	public Acetate(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.RED;
	}

	// Do nothing
	@Override
	protected void doUVExposure() { }

	@Override
	protected void interact(Compound compound) {
		// Acetate + Hydroxyl -> Propylium, 2-oxo-
		if (compound.getClass() == Hydroxyl.class) {
			generate(CarbonCenteredRadical.class);
			decay(compound);
			decay(this);
		}
	}
}
