package edu.mtu.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.Molecule;
import edu.mtu.parser.Parser;

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
	public void addBimolecularReaction(ReactionDescription reaction) {
		// Add the reactions to the registry
		for (String reactant : reaction.getReactants()) {
			if (!bimolecular.containsKey(reactant)) {
				bimolecular.put(reactant, new ArrayList<ReactionDescription>());
			}
			((ArrayList<ReactionDescription>)bimolecular.get(reactant)).add(reaction);
		}
	}
	
	/**
	 * Add the given photolysis reaction to the registry.
	 */
	public void addPhotolysisReaction(ReactionDescription reaction) throws InvalidActivityException {
		String reactant = reaction.getReactants().get(0);
		if (photolysis.containsKey(reactant)) {
			throw new InvalidActivityException("Reaction registry already contains photolysis products for " + reactant);
		}
		
		photolysis.put(reactant, reaction.getProducts());
	}
	
	/**
	 * Add the given unimolecular reaction to the registry.
	 */
	public void addUnimolecularReaction(ReactionDescription reaction) throws InvalidActivityException {
		// Make sure the key is valid
		String key = reaction.getReactants().get(0);
		if (key.toUpperCase() == "UV") {
			throw new IllegalArgumentException("A photolysis reaction cannot be unimolecular");
		}
				
		if (!unimolecular.containsKey(key)) {
			unimolecular.put(key, new ArrayList<ReactionDescription>());
		}
		
		((ArrayList<ReactionDescription>)unimolecular.get(key)).add(reaction);
	}
	
	/**
	 * Clear the current contents of the registry.
	 */
	public void clear() {
		bimolecular.clear();
		photolysis.clear();
		unimolecular.clear();
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public List<ReactionDescription> getBiomolecularReaction(Molecule molecule) {
		return bimolecular.get(molecule.getFormula());
	}
	
	/**
	 * Get a list of all the entities in the registry.
	 * @return
	 */
	public List<String> getEntityList() {
		HashSet<String> entities = new HashSet<String>();
		
		for (String key : photolysis.keySet()) {
			entities.add(key);
			for (String value : photolysis.get(key)) {
				entities.add(value);
			}
		}
		
		entities.addAll(extractEntities(unimolecular));
		entities.addAll(extractEntities(bimolecular));
		
		return new ArrayList<String>(entities);
	}
		
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public List<String> getPhotolysisReaction(String formula) {
		return photolysis.get(formula);
	}
	
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public List<String> getPhotolysisReaction(Molecule molecule) {
		return photolysis.get(molecule.getFormula());
	}
		
	/**
	 * Returns the list of unimolecular reactions for the chemical species or null.
	 */
	public List<ReactionDescription> getUnimolecularReaction(Molecule molecule) {
		return unimolecular.get(molecule.getFormula());
	}
	
	/**
	 * Load the contents of the indicated file into the registry.
	 * 
	 * @param fileName The name and path of the file to be loaded.
	 */
	public void load(String fileName) throws IOException {
		List<ReactionDescription> reactions = Parser.parseReactions(fileName);
		for (ReactionDescription reaction : reactions) {
			// Check for a unimolecular reaction
			if (reaction.getReactants().size() == 1) {
				addUnimolecularReaction(reaction);
				continue;
			}
			
			// Is this a photolysis reaction?
			if (reaction.getReactants().get(1).toUpperCase().equals("UV")) {
				addPhotolysisReaction(reaction);
			}
			
			// Must be a bimolecular reaction
			addBimolecularReaction(reaction);
		}
	}
	
	/**
	 * Extract the unique entity names from the collection.
	 */
	private HashSet<String> extractEntities(Map<String, List<ReactionDescription>> reactions) {
		HashSet<String> entities = new HashSet<String>();
		for (String key : reactions.keySet()) {
			entities.add(key);
			for (ReactionDescription value : reactions.get(key)) {
				entities.addAll(value.getReactants());
				entities.addAll(value.getProducts());
			}
		}
		return entities;
	}
}
