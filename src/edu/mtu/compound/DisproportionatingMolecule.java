package edu.mtu.compound;

import edu.mtu.catalog.ReactionDescription;

/**
 * This class represents a disproportionating chemical entity.
 */
public class DisproportionatingMolecule extends Molecule {

	private int age = 0;
	private ReactionDescription[] reactions;
	
	/**
	 * Constructor.
	 */
	public DisproportionatingMolecule(String formula) {
		// NOTE We are assuming that disproportion is always independent of photolysis		
		super(formula);
	}

	/**
	 * Create a new disproportionating species from the species and reactions provided. 
	 */
	public static DisproportionatingMolecule create(Molecule species, ReactionDescription[] reactions) {
		DisproportionatingMolecule entity = new DisproportionatingMolecule(species.getFormula());
		entity.reactions = reactions.clone();
		return entity;
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingMolecule create(Molecule one, Molecule two, ReactionDescription[] reactions) { 
		if (two == null) {
			return create(one, reactions);
		}
		
		DisproportionatingMolecule entity = new DisproportionatingMolecule(one.getFormula() + " + " + two.getFormula());
		entity.reactions = reactions.clone();
		return entity;		
	}
	
	@Override
	public void doAction() {
		// Check for any valid reactions
		int size = reactions.length;
		for (int ndx = 0; ndx < size; ndx++) {
			if (reactions[ndx] != null) {
				Reaction.getInstance().react(this);
				return;
			}
		}
		
		// If we are here, all of the reactions are gone
		dispose();
	}
	
	/**
	 * Get how old this species is in time steps.
	 */
	public int getAge() {
		return age;
	}
		
	/**
	 * Get the reactions for this entity.
	 */
	public ReactionDescription[] getReactions() {
		return reactions;
	}
		
	/**
	 * Updates and returns the age.s
	 */
	public int updateAge() {
		age++;
		return age;
	}
}
