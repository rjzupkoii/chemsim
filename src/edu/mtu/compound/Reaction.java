package edu.mtu.compound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mtu.Reactor.Cell;
import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionDescription;
import edu.mtu.catalog.ReactionRegistry;
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
	 * @param species of the chemical for the disproportionation.
	 * @param cell where the reaction should take place.
	 */
	public void disproportionate(DisproportionatingSpecies species, Cell cell) {
		// Is there anything to do?
		long count = cell.count(species);
		if (count == 0) {
			return;
		}
		
		// Note the age
		int age = species.updateAge();
		
		// Process potential products
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (int ndx = 0; ndx < species.getReactions().size(); ndx++) {
			// Check to see if this reaction shouldn't occur yet
			ReactionDescription reaction = species.getReactions().get(ndx);
			if (reaction.getReactionRate() > age) {
				continue;
			}
			
			// Note the index for removal
			indicies.add(ndx);
			
			// Create the products for the reaction			
			ReactionRegistry registry = ReactionRegistry.getInstance();
			for (String product : reaction.getProducts()) {			
				cell.add(registry.getSpecies(product), count);
			}
			cell.remove(species, count);
		}
		
		// Remove the reactions that occurred
		Collections.sort(indicies, Collections.reverseOrder());
		for (int ndx : indicies) {
			species.getReactions().remove(ndx);
		}
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param species of chemical for the reaction.
	 */
	public void react(Species species, Cell cell) {
		// Perform any relevant dispropriation reactions
		if (species instanceof DisproportionatingSpecies) {
			disproportionate((DisproportionatingSpecies)species, cell);
			return;
		}
		
		// First, see if there are any bimolecular reactions to take place
		if (bimolecularReaction(species, cell)) {
			return;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (unimolecularDecay(species, cell)) {
			return;
		}
		
		// Third, see if photolysis needs to take place
		if (photolysis(species, cell)) {
			return;
		}
	}
	
	/**
	 * Perform a bimolecular reaction on the given species.
	 */
	private boolean bimolecularReaction(Species species, Cell cell) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(species);
		if (reactions == null) {
			return false;
		}
		
		// Check to see what other species at this location react with the given one
		for (Species reactant : cell.getMolecules()) {
			if (reactant.equals(species)) {
				continue;
			}
			
			// Process the reactants and press on if a match is not found
			if (process(species, reactant, reactions, cell)) {
				return true;
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
	private boolean photolysis(Species species, Cell cell) {
		// Return if there is nothing to do
		if (!species.getPhotosensitive()) {
			return false;
		}
				
		// Return if there are no products registered
		List<String> products = ReactionRegistry.getInstance().getPhotolysisReaction(species);
		if (products == null) {
			return false;
		}
		
		// Decay the species based upon it's reaction with UV
		ReactionRegistry registry = ReactionRegistry.getInstance();
		for (String product : products) {
			// TODO Marker for changing out the hydrogen peroxide decay quantity
			long value = (long)(ChemSim.getProperties().getHydrogenPeroxideDecay() * Reactor.getInstance().getAvogadroNumber());
			
			cell.add(registry.getSpecies(product), value);
			cell.remove(species, value);
		}
		return true;
	}	
	
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean process(Species species, Species reactant, List<ReactionDescription> reactions, Cell cell) {
		// Can this reaction occur?
		List<ReactionDescription> matched = new ArrayList<ReactionDescription>();
		for (ReactionDescription rd : reactions) {
			if (rd.checkReactants(species, reactant)) {
				matched.add(rd);
			}
		}
		if (matched.isEmpty()) {
			return false;
		}
		
		// Add the appropriate number of molecules to the model
		long value;
		if (matched.size() > 1) {
			// A disproporting reaction is occurring
			value = getQuantity(species,reactant, cell);
			Species product = DisproportionatingSpecies.create(species, reactant, reactions);
			cell.add(product, value);
		} else {
			// A standard reaction is occurring
			value = getQuantity(species,reactant, cell);
			ReactionRegistry registry = ReactionRegistry.getInstance();
			for (String formula : matched.get(0).getProducts()) {
				cell.add(registry.getSpecies(formula), value);
			}
		}
		
		// Clean up the reactants that were involved
		cell.remove(species, value);
		if (reactant != null) {
			cell.remove(reactant);
		}
		return true;
	}
	
	/**
	 * Get the minimum quantity of reactant to use.
	 */
	private long getQuantity(Species one, Species two, Cell cell) {
		long valueOne = (one != null) ? cell.count(one) : 0;
		long valueTwo = (two != null) ? cell.count(two) : 0;
		if (valueOne == 0) {
			return valueTwo;
		}
		if (valueTwo == 0) {
			return valueOne;
		}
		return Math.min(valueOne, valueTwo);
	}
	
	/**
	 * Perform a unimolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Species species, Cell cell) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(species);
		if (reactions == null) {
			return false;
		}

		// Return the results of processing the reactions
		return process(species, null, reactions, cell);
	}
}
