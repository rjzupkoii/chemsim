package edu.mtu.simulation.decay;

import java.io.IOException;

/**
 * This class provides a model for photolysis based upon
 * a single decay rate for the whole experiment.
 */
public class Photolysis implements DecayModel {

	private long quantity;
	
	@Override
	public int estimateRunningTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public long getDecayQuantity(int timeStep, String compound, long moleclues) {
		// TODO Adjust this for multiple compounds
		return quantity;
	}

	@Override
	public void prepare(String fileName) throws IOException {
		// TODO Determine how to code this
		
		/*
		// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
		// this means we need to determine the odds that any individual 
		// hydrogen peroxide agent will be removed each time step based upon
		// the new population which requires us knowing the initial decay
		int decay = (int)Math.ceil(Math.abs((count * rate * volume) / chemical.mols)) * SCALING;
		properties.setHydrogenPeroxideDecayQuantity(decay);
		
		// Since we know the decay rate we can calculate the running time
		long time = ((chemical.count * multiplier) / decay) + PADDING;
		properties.setTimeSteps(time);
		*/
	}
}
