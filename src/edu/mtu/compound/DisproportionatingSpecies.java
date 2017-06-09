package edu.mtu.compound;

import java.awt.Color;
import java.util.List;

import edu.mtu.catalog.ReactionDescription;
import sim.engine.SimState;

/**
 * This class represents a disproportionating chemical entity.
 */
@SuppressWarnings("serial")
public class DisproportionatingSpecies extends Species {

	private int age = 0;
	private List<ReactionDescription> reactions;
	
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
		DisproportionatingSpecies enttiy = new DisproportionatingSpecies(species.getFormula());
		enttiy.reactions = reactions;
		return enttiy;
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingSpecies create(Species one, Species two, List<ReactionDescription> reactions) { 
		if (two == null) {
			return create(one,reactions);
		}
		
		DisproportionatingSpecies entity = new DisproportionatingSpecies(one.getFormula() + " + " + two.getFormula());
		entity.reactions = reactions;
		return entity;		
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
	
	/**
	 * Get the color for this entity.
	 */
	public Color getColor() {
		return Color.RED;
	}
	
	/**
	 * Get the reactions for this entity.
	 */
	public List<ReactionDescription> getReactions() {
		return reactions;
	}
}
