package edu.mtu.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.Species;

/**
 * This singleton contains a look up of the reactions in the simulation. In order to account for 
 * disproportionation each of the species is associated with a list of formulas.
 */
public class ReactionRegistry {

	private static ReactionRegistry instance = new ReactionRegistry();
	
	// Photolysis is [Reactant] + UV -> [Product] + ... + [Product] 
	private Map<String, List<String>> photolysis = new HashMap<String, List<String>>();
		
	// Bimolecular reactions are [Reactant] + [Reactant] -> [Product] + ... + [Product]
	private Map<String, List<ReactionDescription>> bimolecular = new HashMap<String, List<ReactionDescription>>();
	
	// Unimolecular reactions are [Reactant] -> [Product] + ... + [Product]
	private Map<String, List<ReactionDescription>> unimolecular = new HashMap<String, List<ReactionDescription>>();
	
	/**
	 * Singleton constructor.
	 */
	private ReactionRegistry() { }
	
	/**
	 * Get the instance of the singleton.
	 */
	public static ReactionRegistry getInstance() {
		return instance;
	}

	/**
	 * Add the given bimolecular reaction to the registry.
	 */
	public void addBimolecularReaction(List<String> reactants, List<String> products) {
		// Create the reaction description
		ReactionDescription reaction = new ReactionDescription(reactants, products);
		
		// Add the reactions to the registry
		for (String reactant : reactants) {
			if (!bimolecular.containsKey(reactant)) {
				bimolecular.put(reactant, new ArrayList<ReactionDescription>());
			}
			((ArrayList<ReactionDescription>)bimolecular.get(reactant)).add(reaction);
		}
	}
	
	/**
	 * Add the given photolysis reaction to the registry.
	 */
	public void addPhotolysisReaction(String reactant, List<String> products) throws InvalidActivityException {
		if (photolysis.containsKey(reactant)) {
			throw new InvalidActivityException("Reaction registry already contains photolysis products for " + reactant);
		}
		
		photolysis.put(reactant, products);
	}
	
	/**
	 * Add the given unimolecular reaction to the registry.
	 */
	public void addUnimolecularReaction(ReactionDescription reaction) throws InvalidActivityException {
		String key = reaction.getReactants().get(0);
		if (!unimolecular.containsKey(key)) {
			unimolecular.put(key, new ArrayList<ReactionDescription>());
		}
		
		((ArrayList<ReactionDescription>)unimolecular.get(key)).add(reaction);
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public List<ReactionDescription> getBiomolecularReaction(Species species) {
		return bimolecular.get(species.getFormula());
	}
	
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public List<String> getPhotolysisReaction(Species species) {
		return photolysis.get(species.getFormula());
	}
	
	/**
	 * Returns the list of unimolecular reactions for the chemical species or null.
	 * @param species
	 * @return
	 */
	public List<ReactionDescription> getUnimolecularReaction(Species species) {
		return unimolecular.get(species.getFormula());
	}
}
