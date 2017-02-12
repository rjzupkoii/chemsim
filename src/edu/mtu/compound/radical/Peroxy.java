package edu.mtu.compound.radical;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.Alcohol;
import edu.mtu.compound.Aldehyde;
import edu.mtu.compound.HydrogenPeroxide;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.ChemSimProperties;
import edu.mtu.simulation.Compound;
import sim.util.Int3D;

/**
 * Peroxy Radical
 */
@SuppressWarnings("serial")
public class Peroxy extends Compound {
	
	// Keep track of the timestep for disproportionation
	private int timeStep = 0;

	public Peroxy(Int3D movementVector) {
		super(movementVector);
	}

	public static Color getColor() {
		return Color.MAGENTA;
	}

	// Do nothing
	@Override
	protected void doOxidation(MersenneTwisterFast random) { }
	
	// Do nothing
	@Override
	protected void doUVExposure(MersenneTwisterFast random) { }

	// Do nothing
	@Override
	protected boolean interact(Compound compound) { 
		return false;
	}

	@Override
	protected void doDisproportionation(MersenneTwisterFast random) {
		// Update the timestep each time we are called
		timeStep++;
		
		ChemSimProperties properties = ChemSim.getProperties();
		if (timeStep == properties.getFastPathway()) {
			generate(Oxyl.class);
		}
		if (timeStep == properties.getMediumPathway()) {
			generate(Alcohol.class);
			generate(Aldehyde.class);
		}
		if (timeStep == properties.getSlowPathway()) {
			generate(Oxyl.class);
			generate(HydrogenPeroxide.class);
			decay(this);
		}
	}
}
