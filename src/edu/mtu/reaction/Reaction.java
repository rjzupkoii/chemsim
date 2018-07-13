package edu.mtu.reaction;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.compound.DisproportionatingMolecule;
import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.compound.MoleculeFactory;
import edu.mtu.primitives.Entity;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;

/**
 * This class provides a means for a chemical species to react with other species.
 */
public class Reaction {
			
	private static Reaction instance = new Reaction();
		
	/**
	 * Singleton constructor.
	 */
	private Reaction() { }
	
	/**
	 * Get the instance of the singleton.
	 */
	public static Reaction getInstance() {
		return instance;
	}
			
	/**
	 * Have the chemical species disproportionate according to its reaction rate.
	 * 
	 * @param molecule of the chemical for the disproportionation.
	 * @param cell where the reaction should take place.
	 */
	public void disproportionate(DisproportionatingMolecule molecule) {
		
		// Local pointer to the reactions
		ReactionDescription[] reactions = molecule.getReactions();
		int size = reactions.length;

		// Keep track of the odds
		boolean probabilistic = false;
		ArrayList<Double> reactionOdds = new ArrayList<Double>();
		
		// Find the indicies and odds of valid reactions
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (int ndx = 0; ndx < size; ndx++) {
			// Press on if the descrption is null
			if (reactions[ndx] == null) {
				continue;
			}
						
			// Note the odds
			double odds = reactions[ndx].getReactionOdds();
			probabilistic = probabilistic || (odds < 1);
			reactionOdds.add(odds);			
			
			// Note the index for removal
			indicies.add(ndx);
		}
				
		if (probabilistic) {
			doProbabilistic(molecule, reactions, indicies, reactionOdds);
		} else {
			// Process the reactions
			for (int index : indicies) {
				// Create the products for the reaction
				Reactor reactor = Reactor.getInstance();
				int[] location = reactor.getLocation(molecule);
				for (String formula : reactions[index].getProducts()) {			
					MoleculeFactory.create(formula, location);
				}
			}
		}
			
		// Null out the reactions that occurred
		for (int ndx : indicies) {
			reactions[ndx] = null;
		}
	}
	
	private void doProbabilistic(DisproportionatingMolecule molecule, ReactionDescription[] reactions, ArrayList<Integer> indicies, ArrayList<Double> reactionOdds) {
		// Add the odds so we can do a single dice roll
		int size = reactionOdds.size();
		double value = 0;
		for (int ndx = 1; ndx < size; ndx++) {
			value = reactionOdds.get(ndx) + reactionOdds.get(ndx - 1);
			reactionOdds.set(ndx, value);
		}
		if (value != 1.0) {
			throw new IllegalArgumentException("Total odds of the reaction '" + molecule.getFormula() + "' cannot exceed 1.0");
		}
		double selected = ChemSim.getRandom().nextDouble();
		
		// Select the correct reaction
		double previous = 0.0;
		for (int ndx = 0; ndx < reactionOdds.size(); ndx++) {
			
			// If the selected value is in the range, then create the products and return
			if (previous <= selected && selected <= reactionOdds.get(ndx)) {
				int[] location = Reactor.getInstance().getLocation(molecule);
				for (String formula : reactions[indicies.get(ndx)].getProducts()) {			
					MoleculeFactory.create(formula, location);
				}
				return;
			}
			
			previous = reactionOdds.get(ndx);
		}
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param moleclue for the reaction.
	 * @return True if a reaction occurred, false otherwise.
	 */
	public boolean react(DisproportionatingMolecule moleclue) {
		 disproportionate(moleclue);
		 return true;
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param moleclue for the reaction.
	 * @return True if a reaction occurred, false otherwise.
	 */
	public boolean react(Molecule molecule) {
		// First, see if there are any bimolecular reactions to take place
		if (molecule.hasBimoleculear() && bimolecularReaction(molecule)) {
			return true;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (molecule.hasUnimolecular() && unimolecularDecay(molecule)) {
			return true;
		}

		// Finally, see if photolysis needs to take place
		if (molecule.hasPhotolysis()) {
			return photolysis(molecule);
		}
		
		return false;
	}
	
	/**
	 * Perform a bimolecular reaction on the given species.
	 */
	private boolean bimolecularReaction(Molecule molecule) {
		
		// Get the possible hashes
		Integer[] hashes = molecule.getReactantHashes(); 
		
		// Check to see if there are any dissolved molecule we should be aware of,
		// this comes first since a molecule that reacts with dissolved molecules
		// is unlikely to do anything else
		if (molecule.hasDissolvedReactants()) {		
			for (DissolvedMolecule reactant : ReactionRegistry.DissolvedMoleclues) {
				for (int formulaHash : hashes) {
					if (reactant.sameEntity(formulaHash)) {
						int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
						ReactionDescription[] reactions = ReactionRegistry.getInstance().getBimolecularReaction(molecule);
						return process(molecule, reactant, location, reactions);
					}
				}
			}
		}
								
		// Get the possible interaction radii
		int[] radii = molecule.getInteractionRadii();
		
		// Use the lattice to search out to the interaction radius		
		for (int ndx = 0; ndx < hashes.length; ndx++) {
			Entity match = Reactor.getInstance().grid.findFirstByTag(molecule, hashes[ndx], radii[ndx]);
			if (match != null) {
				return process(molecule, (Molecule)match, radii[ndx]);
			}
		}		
		
		return false;
	}
		
	/**
	 * Attempt photolysis on the species.
	 * 
	 * @param species to attempt photolysis on.
	 * @param cell where the reaction should take place.
	 * @return True if it occurred, false otherwise.
	 */
	private boolean photolysis(Molecule molecule) {
		// Check to see if the reaction occurred based upon decay rates
		double decay = ChemSim.getProperties().getHydrogenPeroxideDecay();
		if (ChemSim.getRandom().nextDouble() > decay) {
			return false;
		}
		
		// Add the products at this location
		int[] location = Reactor.getInstance().getLocation(molecule);
		String[] products = ReactionRegistry.getInstance().getPhotolysisReaction(molecule);
		MoleculeFactory.create(products, location);
		
		// Note that a reaction occurred, molecule will dispose of itself
		return true;
	}	
	
	/**
	 * Process the reactions that are possible for this entity, assume only one
	 * bimolecular reaction based upon the search radius.
	 */
	private boolean process(Molecule molecule, Molecule reactant, int radius) {
		ReactionDescription[] reactions = ReactionRegistry.getInstance().getBimolecularReaction(molecule);
		for (ReactionDescription rd : reactions) {
			if (rd.checkReactants(molecule, reactant) && rd.getInteractionRadius() == radius) {
				// Add the molecules to the model
				int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
				MoleculeFactory.create(rd.getProducts(), location);
				
				// Clean up the reactant and inform the molecule to dispose of itself
				reactant.dispose();
				return true;
			}
		}
		
		// Nothing was found, throw an error since that shouldn't occur
		throw new IllegalAccessError(String.format("No matches found for %s, %s", molecule, reactant));
	}
	
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean process(Molecule molecule, Molecule reactant, int[] location, ReactionDescription[] reactions) {
		
		// Find the correct reaction(s)
		List<ReactionDescription> matched = new ArrayList<ReactionDescription>();
		for (ReactionDescription rd : reactions) {
			if (rd.checkReactants(molecule, reactant)) {
				matched.add(rd);
			}
		}
		if (matched.size() == 0) {
			throw new IllegalAccessError(String.format("No matches found for %s, %s", molecule, reactant));
		}
				
		// Add the molecules to the model
		if (matched.size() > 1) {
			// Disproportion is occurring
			MoleculeFactory.create(molecule, reactant, matched, location);
		} else {
			// A standard reaction is occurring
			MoleculeFactory.create(matched.get(0).getProducts(), location);
		}
		
		// Clean up the reactant that was involved
		if (reactant != null) {
			reactant.dispose();
		}
		
		// The molecule will be dispose itself
		return true;
	}
		
	/**
	 * Perform a unimolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Molecule molecule) {
		int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
		ReactionDescription[] reactions = ReactionRegistry.getInstance().getUnimolecularReaction(molecule);
		return process(molecule, null, location, reactions);
	}
}
