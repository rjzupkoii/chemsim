package edu.mtu.compound;

import edu.mtu.reaction.Reaction;
import edu.mtu.reaction.BasicReaction;

/**
 * This class represents a disproportionating chemical entity and is used to support
 * reaction pathways with probability involved.
 */
public class DisproportionatingMolecule extends Molecule {

	private BasicReaction[] reactions;
	
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
	public static DisproportionatingMolecule create(Molecule species, BasicReaction[] reactions) {
		DisproportionatingMolecule entity = new DisproportionatingMolecule(species.getFormula());
		entity.reactions = reactions.clone();
		return entity;
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingMolecule create(Molecule one, Molecule two, BasicReaction[] reactions) { 
		if (two == null) {
			return create(one, reactions);
		}
		
		DisproportionatingMolecule entity = new DisproportionatingMolecule(one.getFormula() + " + " + two.getFormula());
		entity.reactions = reactions.clone();
		return entity;		
	}
	
	@Override
	public void doAction(int step) {
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
	 * Get the reactions for this entity.
	 */
	public BasicReaction[] getReactions() {
		return reactions;
	}
}
