package edu.mtu.compound;

import java.util.List;

import edu.mtu.catalog.ReactionDescription;

/**
 * This class represents a disproportionating chemical entity.
 */
public class DisproportionatingMolecule extends Molecule {

	// TODO Refactor this class to be unique within the cell
	
	private int age = 0;
	private List<ReactionDescription> reactions;
	
	/**
	 * Constructor.
	 */
	public DisproportionatingMolecule(int hash) {
		super(hash);
	}
	
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
		DisproportionatingMolecule enttiy = new DisproportionatingMolecule(species.getFormulaHash());
		enttiy.reactions = reactions;
		return enttiy;
	}
	
	/**
	 * Create a new disproportionating species from the species and reactions provided.
	 */
	public static DisproportionatingMolecule create(Molecule one, Molecule two, List<ReactionDescription> reactions) { 
		if (two == null) {
			return create(one,reactions);
		}
		
		DisproportionatingMolecule entity = new DisproportionatingMolecule(one.getFormulaHash() + " + " + two.getFormulaHash());
		entity.reactions = reactions;
		return entity;		
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
