package edu.mtu.compound;

import java.awt.Color;

import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class HydrogenPeroxide extends Compound {

	public HydrogenPeroxide(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.BLUE;
	}

	@Override
	protected void doUVExposure() {
		// Upon UV exposure, decay into two Hydroxyl radicals
		decay();
		generate(Hydroxyl.class);
		generate(Hydroxyl.class);
	}
}
