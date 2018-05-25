package edu.mtu.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private List<String> products;
	
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
			if (!((ArrayList<ReactionDescription>)bimolecular.get(reactant)).contains(reaction)) {
				((ArrayList<ReactionDescription>)bimolecular.get(reactant)).add(reaction);
			}
		}
	}
	
	/**
	 * Add the given photolysis reaction to the registry.
	 */
	public void addPhotolysisReaction(ReactionDescription reaction) throws InvalidActivityException {
		String reactant = reaction.getReactants().get(0);
		if (reactant.toUpperCase().equals("UV")) {
			reactant = reaction.getReactants().get(1);
		}
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
		products = null;
		bimolecular.clear();
		photolysis.clear();
		unimolecular.clear();
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public List<ReactionDescription> getBiomolecularReaction(Molecule molecule) {
		List<ReactionDescription> results = bimolecular.get(molecule.getFormula());
		return (results == null) ? null : Collections.unmodifiableList(results);
	}
	
	/**
	 * Get a list of all the entities in the registry.
	 * @return A list of entities in the registry.
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
	 * Get a list of all of the entities in the registry that do not react with anything.
	 * @return A list of all the non-reactive entities in the registry.
	 */
	public List<String> getProducts() {
		// Has the work already been done?
		if (products != null) {
			return products;
		}
		
		// Start by getting a list of everything
		products = getEntityList();
		
		// Remove ones that are reactants
		for (String compound : photolysis.keySet()) {
			products.remove(compound);
		}
		for (String compound : unimolecular.keySet()) {
			products.remove(compound);
		}
		for (String key : bimolecular.keySet()) {
			for (ReactionDescription reaction : (List<ReactionDescription>)bimolecular.get(key)) {
				for (String compound : reaction.getReactants()) {
					products.remove(compound);
				}
			}
		}
		
		// Return the results
		return products;		
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
		List<ReactionDescription> results = unimolecular.get(molecule.getFormula());
		return (results == null) ? null : Collections.unmodifiableList(results);
	}
	
	/**
	 * Load the contents of the indicated file into the registry.
	 * 
	 * @param fileName The name and path of the file to be loaded.
	 */
	public void load(String fileName) throws IOException {
		List<ReactionDescription> reactions = Parser.parseReactions(fileName); 
		for (ReactionDescription reaction : reactions) {
			// Note what we are currently loading
			String message = "Loading " + reaction.toString();						
			if (reaction.getReactants().size() == 1) {
				// This is a unimolecular reaction
				addUnimolecularReaction(reaction);
				message += " (unimolecular)";
			} else if (reaction.getReactants().contains("UV")) {
				// This is a photolysis reaction
				addPhotolysisReaction(reaction);
				message += " (photolysis)";
			} else {
				// Must be a bimolecular reaction
				addBimolecularReaction(reaction);
				message += " (bimolecular)";
			}
			
			// TODO Make this optional
			System.out.println(message);
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
