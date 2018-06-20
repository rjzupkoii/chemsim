package edu.mtu.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.Parser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * This singleton contains a look up of the reactions in the simulation. In order to account for 
 * disproportionation each of the species is associated with a list of formulas.
 */
public class ReactionRegistry {

	/**
	 * Dissolved molecules that are always present in the reactor.
	 */
	public static final DissolvedMolecule[] disolved = { 
			new DissolvedMolecule("O2"),
			new DissolvedMolecule("H2O")
	};
	
	private static ReactionRegistry instance = new ReactionRegistry();
	
	private Map<String, Boolean> hasReactants = new HashMap<String, Boolean>();
	private Map<String, Boolean> hasDissolvedReactants = new HashMap<String, Boolean>();
	
	// Photolysis is [Reactant] + UV -> [Product] + ... + [Product] 
	private Map<String, List<String>> photolysis;
		
	// Bimolecular reactions are [Reactant] + [Reactant] -> [Product] + ... + [Product]
	private Map<String, List<ReactionDescription>> bimolecular;
	
	// Unimolecular reactions are [Reactant] -> [Product] + ... + [Product]
	private Map<String, List<ReactionDescription>> unimolecular;
	
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
	private void addBimolecularReaction(ReactionDescription reaction, Map<String, List<ReactionDescription>> working ) {
		// Add the reactions to the registry
		for (String reactant : reaction.getReactants()) {
			if (!working.containsKey(reactant)) {
				working.put(reactant, new ArrayList<ReactionDescription>());
			}
			if (!((ArrayList<ReactionDescription>)working.get(reactant)).contains(reaction)) {
				((ArrayList<ReactionDescription>)working.get(reactant)).add(reaction);
			}
		}
	}
	
	/**
	 * Add the given photolysis reaction to the registry.
	 */
	private void addPhotolysisReaction(ReactionDescription reaction, Map<String, List<String>> working) throws InvalidActivityException {
		String reactant = reaction.getReactants().get(0);
		if (reactant.toUpperCase().equals("UV")) {
			reactant = reaction.getReactants().get(1);
		}
		if (working.containsKey(reactant)) {
			throw new InvalidActivityException("Reaction registry already contains photolysis products for " + reactant);
		}
		
		working.put(reactant, reaction.getProducts());
	}
	
	/**
	 * Add the given unimolecular reaction to the registry.
	 */
	private void addUnimolecularReaction(ReactionDescription reaction, Map<String, List<ReactionDescription>> working) throws InvalidActivityException {
		// Make sure the key is valid
		String key = reaction.getReactants().get(0);
		if (key.toUpperCase() == "UV") {
			throw new IllegalArgumentException("A photolysis reaction cannot be unimolecular");
		}
				
		if (!working.containsKey(key)) {
			working.put(key, new ArrayList<ReactionDescription>());
		}
		
		((ArrayList<ReactionDescription>)working.get(key)).add(reaction);
	}
	
	/**
	 * Clear the current contents of the registry.
	 */
	public void clear() {
		bimolecular = null;
		photolysis = null;
		unimolecular = null;
		hasReactants.clear();
		hasDissolvedReactants.clear();
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public static List<ReactionDescription> getBimolecularReaction(Molecule molecule) {
		return instance.bimolecular.get(molecule.getFormula());
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
	 * Returns the photolysis products for the chemical species or null.
	 */
	public List<String> getPhotolysisReaction(String formula) {
		return photolysis.get(formula);
	}
	
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public static List<String> getPhotolysisReaction(Molecule molecule) {
		return instance.photolysis.get(molecule.getFormula());
	}
		
	/**
	 * Returns the list of unimolecular reactions for the chemical species or null.
	 */
	public static List<ReactionDescription> getUnimolecularReaction(Molecule molecule) {
		return instance.unimolecular.get(molecule.getFormula());
	}
	
	/**
	 * 
	 * @param formula
	 * @return
	 */
	public static boolean hasDissolvedReactants(String formula) {
		assert (instance != null);
		
		if (!instance.hasDissolvedReactants.containsKey(formula)) {
			for (ReactionDescription reaction : (List<ReactionDescription>)instance.bimolecular.get(formula)) {
				for (String compound : reaction.getReactants()) {
					for (DissolvedMolecule molecule : disolved) {
						if (molecule.getFormula().equals(compound)) {
							instance.hasDissolvedReactants.put(formula, true);
							return true;
						}
					}
				}
			}
			instance.hasDissolvedReactants.put(formula, false);
		}
		
		return instance.hasDissolvedReactants.get(formula);
	}
	
	/**
	 * 
	 * @param formula
	 * @return
	 */
	public static boolean hasReactants(String formula) {
		assert (instance != null);
		
		// If the key doesn't already exist, then scan the known formulas to 
		// determine if it is a product or a reactant
		if (!instance.hasReactants.containsKey(formula)) {
			// Start by getting a list of everything
			List<String> products = instance.getEntityList();
			
			// Remove ones that are reactants
			for (String compound : instance.photolysis.keySet()) {
				products.remove(compound);
			}
			for (String compound : instance.unimolecular.keySet()) {
				products.remove(compound);
			}
			for (String key : instance.bimolecular.keySet()) {
				for (ReactionDescription reaction : (List<ReactionDescription>)instance.bimolecular.get(key)) {
					for (String compound : reaction.getReactants()) {
						products.remove(compound);
					}
				}
			}
			
			// Note the results
			instance.hasReactants.put(formula, !products.contains(formula));
		}
		
		// Return the result
		return instance.hasReactants.get(formula);
	}
	
	/**
	 * Load the contents of the indicated file into the registry.
	 * 
	 * @param fileName The name and path of the file to be loaded.
	 */
	public void load(String fileName) throws IOException {
		
		// Define our working maps
		Map<String, List<ReactionDescription>> bimolecular = new Object2ObjectOpenHashMap<String, List<ReactionDescription>>();
		Map<String, List<String>> photoysis = new Object2ObjectOpenHashMap<String, List<String>>();
		Map<String, List<ReactionDescription>> unimolecular = new Object2ObjectOpenHashMap<String, List<ReactionDescription>>();
		
		List<ReactionDescription> reactions = Parser.parseReactions(fileName); 
		for (ReactionDescription reaction : reactions) {
			// Note what we are currently loading
			String message = "Loading " + reaction.toString() + " (";						
			if (reaction.getReactants().size() == 1) {
				// This is a unimolecular reaction
				addUnimolecularReaction(reaction, unimolecular);
				message += "unimolecular";
			} else if (reaction.getReactants().contains("UV")) {
				// This is a photolysis reaction
				addPhotolysisReaction(reaction, photoysis);
				message += "photolysis";
			} else {
				// Must be a bimolecular reaction
				addBimolecularReaction(reaction, bimolecular);
				message += "bimolecular";
			}
			if (reaction.getReactionOdds() != 1.0) {
				message += ", " + reaction.getReactionOdds();
			}
			message += ")";
			
			// TODO Make this optional
			System.out.println(message);
		}
		
		// Everything is loaded, now lock it down
		this.bimolecular = Collections.unmodifiableMap(new Object2ObjectOpenHashMap<String, List<ReactionDescription>>(bimolecular));
		this.photolysis = Collections.unmodifiableMap(new Object2ObjectOpenHashMap<String, List<String>>(photoysis));
		this.unimolecular = Collections.unmodifiableMap(new Object2ObjectOpenHashMap<String, List<ReactionDescription>>(unimolecular));
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
