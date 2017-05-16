package edu.mtu.compound;

import java.util.List;

import edu.mtu.catalog.ReactionDescription;
import sim.engine.SimState;

/**
 * This class represents a disproportionating chemical entity.
 */
@SuppressWarnings("serial")
public class DisproportionatingSpecies extends Species {

	private int age = 0;
	
	/**
	 * Constructor.
	 */
	public DisproportionatingSpecies(String formula) {
		super(formula);
	}

	/**
	 * Create a new disproportionating species from the species and reactions provided. 
	 */
	public static DisproportionatingSpecies create(Species species, List<ReactionDescription> reactions) {
		
		// TODO Implement this method
		return null;
		
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingSpecies create(Species one, Species two, List<ReactionDescription> reactions) { 
		
		// TODO Implement this method
		return null;
		
	}
	
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
