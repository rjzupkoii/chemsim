package edu.mtu.compound;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.catalog.ReactionDescription;

/**
 * This class represents a disproportionating chemical entity.
 */
public class DisproportionatingMolecule extends Molecule {

	private int age = 0;
	private List<ReactionDescription> reactions;
	
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
	public static DisproportionatingMolecule create(Molecule species, List<ReactionDescription> reactions) {
		DisproportionatingMolecule entity = new DisproportionatingMolecule(species.getFormula());
		entity.reactions = new ArrayList<ReactionDescription>(reactions);
		return entity;
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingMolecule create(Molecule one, Molecule two, List<ReactionDescription> reactions) { 
		if (two == null) {
			return create(one,reactions);
		}
		
		DisproportionatingMolecule entity = new DisproportionatingMolecule(one.getFormula() + " + " + two.getFormula());
		entity.reactions = new ArrayList<ReactionDescription>(reactions);
		return entity;		
	}
	
	@Override
	public void doAction() {
		if (reactions.size() != 0) {
			Reaction.getInstance().react(this);
		} else {
			dispose();
		}
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
	public List<ReactionDescription> getReactions() {
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
