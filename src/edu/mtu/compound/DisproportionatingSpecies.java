package edu.mtu.compound;

import sim.engine.SimState;

/**
 * This class represents a disproportionating chemical entity.
 */
@SuppressWarnings("serial")
public class DisproportionatingSpecies extends Species {

	/**
	 * Constructor.
	 */
	public DisproportionatingSpecies(String formula) {
		super(formula);
	}

	private int age = 0;
	
	@Override
	public void step(SimState state) {
		// Update the age of this species
		age++;
		
		// Disproportionate
		Reaction.getInstance().disproportionate(this);
	}
	
	/**
	 * Get how old this species is in time steps.
	 */
	public int getAge() {
		return age;
	}
}
