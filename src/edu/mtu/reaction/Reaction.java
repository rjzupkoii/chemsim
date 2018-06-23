package edu.mtu.reaction;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.compound.DisproportionatingMolecule;
import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.compound.MoleculeFactory;
import edu.mtu.primitives.Int3D;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import sim.util.Bag;

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

		// Note the age
		int age = molecule.updateAge();
		
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
			
			// Check to see if this reaction shouldn't occur yet
			ReactionDescription reaction = reactions[ndx];
			if (reaction.getReactionRate() > age) {
				continue;
			}
			
			// Note the odds
			double odds = reaction.getReactionOdds();
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
				Int3D location = reactor.getLocation(molecule);
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
		double selected = ChemSim.getInstance().random.nextDouble();
		
		// Select the correct reaction
		double previous = 0.0;
		for (int ndx = 0; ndx < reactionOdds.size(); ndx++) {
			
			// If the selected value is in the range, then create the products and return
			if (previous <= selected && selected <= reactionOdds.get(ndx)) {
				Int3D location = Reactor.getInstance().getLocation(molecule);
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
		if (molecule.hasBimoleculear()) {
			Bag molecules = Reactor.getInstance().getMolecules(molecule);
			int size = molecules.numObjs;
			if (size > 1 && bimolecularReaction(molecule, molecules)) {
				return true;
			}
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
	private boolean bimolecularReaction(Molecule molecule, final Bag molecules) {
		ReactionDescription[] reactions = null;
		
		// Check to see what other species at this location react with the given one
		int size = molecules.numObjs;
		for (int ndx = 0; ndx < size; ndx++) {
			Molecule checking = (Molecule)molecules.get(ndx);
			if (checking.equals(molecule)) {
				continue;
			}
			
			for (int formulaHash : molecule.getReactantHashes()) {
				// Check and continue if the hashes don't match
				if (!checking.sameEntity(formulaHash)) {
					continue;
				}
				
				// Get the reactions if we need them, then attempt to process
				if (reactions == null) {
					reactions = ReactionRegistry.getInstance().getBimolecularReaction(molecule);
				}
				if (process(molecule, (Molecule)molecules.get(ndx), reactions)) {
					return true;
				}				
			}
		}
		
		// Check to see if there are any dissolved molecule we should be aware of
		if (molecule.hasDissolvedReactants()) {		
			for (DissolvedMolecule reactant : ReactionRegistry.DissolvedMoleclues) {
				if (process(molecule, reactant, reactions)) {
					return true;
				}
			}
		}
				
		// Nothing was found in in our exact location, search out to the interaction radius
		
		
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
		if (ChemSim.getInstance().random.nextDouble() > decay) {
			return false;
		}
		
		// Add the products at this location
		Int3D location = Reactor.getInstance().getLocation(molecule);
		String[] products = ReactionRegistry.getInstance().getPhotolysisReaction(molecule);
		MoleculeFactory.create(products, location);
		
		// Note that a reaction occurred, molecule will dispose of itself
		return true;
	}	
		
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean process(Molecule molecule, Molecule reactant, ReactionDescription[] reactions) {
		
		// Can this reaction occur?
		List<ReactionDescription> matched = new ArrayList<ReactionDescription>();
		for (ReactionDescription rd : reactions) {
			if (rd.checkReactants(molecule, reactant)) {
				matched.add(rd);
			}
		}
		
		// TODO This shouldn't happen now... figure out how to get dissolved molecules in
		if (matched.isEmpty()) {
			return false;
		}
		
		// Add the molecules to the model
		Int3D location = Reactor.getInstance().getLocation(molecule);
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
		
		// The molecule will be disposed of by itself
		return true;
	}
		
	/**
	 * Perform a unimolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Molecule molecule) {
		ReactionDescription[] reactions = ReactionRegistry.getInstance().getUnimolecularReaction(molecule);
		return process(molecule, null, reactions);
	}
}
