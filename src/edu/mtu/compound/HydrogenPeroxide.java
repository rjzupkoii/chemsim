package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.simulation.ChemSim;
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
	protected void doUVExposure(MersenneTwisterFast random) {
		// Upon UV exposure, probabilistically decay into the Hydroxyl radical
		if (random.nextDouble() < ChemSim.getProperties().getHydroxylAppearanceProbability()) {
			generate(Hydroxyl.class);
		}
		decay(this);
	}

	@Override
	protected void interact(Compound compound) {
		// TODO Implement interaction pathways
	}
}
