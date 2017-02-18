package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.agent.Compound;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class HydrogenPeroxide extends Compound {

	public HydrogenPeroxide(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.BLUE;
	}

	// Do nothing
	@Override
	protected void doDisproportionation(MersenneTwisterFast random) { }
	
	@Override
	protected void doUVExposure(MersenneTwisterFast random) {		
		if (random.nextDouble() < ChemSim.getProperties().getHydrogenPeroxideDecay()) {
			generate(Hydroxyl.class);
			decay(this);
		}
	}

	@Override
	protected boolean interact(Compound compound) {
		// TODO Implement interaction pathways
		return false;
	}

	
	@Override
	protected void doOxidation(MersenneTwisterFast random) { 
		// TODO Implement interaction pathways
	}
}
