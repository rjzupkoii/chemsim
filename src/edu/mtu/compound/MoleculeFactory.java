package edu.mtu.compound;

import java.util.List;

import edu.mtu.reaction.BasicReaction;
import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;

public class MoleculeFactory {

	/**
	 * Create a molecule of the given type and ensure it is scheduled (or not) appropriately.
	 * 
	 * @param formula of the molecule to be created.
	 * @param location of the molecule to be created.
	 */
	public static void create(String formula, final int[] location) {
		// First update out count
		ChemSim.getTracker().update(formula, 1);
		
		// Return if this is a dissolved molecule
		for (DissolvedMolecule moleclue : ReactionRegistry.DissolvedMoleclues) {
			if (moleclue.getFormula().equals(formula)) {
				return;
			}
		}
		
		// Return if it doesn't have any reactants
		if (!ReactionRegistry.getInstance().hasReactants(formula)) {
			return;
		}
		
		// Create and schedule the molecule
		Molecule entity = new Molecule(formula);
		ChemSim.getSchedule().insert(entity);
		Reactor.getInstance().insert(entity, location.clone());
	}
	
	/**
	 * Process the list of formulas and create new molecules from them.
	 * 
	 * @param formulas of the molecules to be created.
	 * @param location of the molecule to be created.
	 */
	public static void create(String[] formulas, final int[] location) {
		for (String formula : formulas) {
			create(formula, location);
		}
	}

	/**
	 * Create a disproportionating molecule and ensure it is add to the schedule.
	 * 
	 * @param reactions associated with the molecule.
	 * @param location of the molecule.
	 */
	public static void create(Molecule one, Molecule two, List<BasicReaction> reactions, final int[] location) {
		// Create the list of reactions
		BasicReaction[] rd = new BasicReaction[reactions.size()];
		rd = reactions.toArray(rd);
		
		// Prepare the canonical order based upon the reactions
		if (!one.getFormula().equals(rd[0].getReactants()[0])) {
			Molecule temp = one;
			one = two;
			two = temp;
		}
		
		// Create the entity
		Molecule entity = DisproportionatingMolecule.create(one, two, rd);
		ChemSim.getSchedule().insert(entity);
		Reactor.getInstance().insert(entity, location.clone());
		ChemSim.getTracker().update(entity.getFormula(), 1);
	}
}
