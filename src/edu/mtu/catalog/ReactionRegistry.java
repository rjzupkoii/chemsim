package edu.mtu.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.Parser;

/**
 * This singleton contains a look up of the reactions in the simulation. In order to account for 
 * disproportionation each of the species is associated with a list of formulas.
 */
public class ReactionRegistry {
	
	// TODO Preprocess so we can return an array describing the molecule, ex. boolean[] { bimolecular, unimolecular, photolysis, product }
	
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
	private Map<String, String[]> photolysis;
		
	// Bimolecular reactions are [Reactant] + [Reactant] -> [Product] + ... + [Product]
	private Map<String, ReactionDescription[]> bimolecular;
	
	// Unimolecular reactions are [Reactant] -> [Product] + ... + [Product]
	private Map<String, ReactionDescription[]> unimolecular;
	
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
	private void addPhotolysisReaction(ReactionDescription reaction, Map<String, String[]> working) throws InvalidActivityException {
		String reactant = reaction.getReactants()[0];
		if (reactant.toUpperCase().equals("UV")) {
			reactant = reaction.getReactants()[1];
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
		String key = reaction.getReactants()[0];
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
	public static ReactionDescription[] getBimolecularReaction(Molecule molecule) {
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
	public String[] getPhotolysisReaction(String formula) {
		return photolysis.get(formula);
	}
	
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public static String[] getPhotolysisReaction(Molecule molecule) {
		return instance.photolysis.get(molecule.getFormula());
	}
		
	/**
	 * Returns the list of unimolecular reactions for the chemical species or null.
	 */
	public static ReactionDescription[] getUnimolecularReaction(Molecule molecule) {
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
			for (ReactionDescription reaction : instance.bimolecular.get(formula)) {
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
				for (ReactionDescription reaction : instance.bimolecular.get(key)) {
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
		Map<String, List<ReactionDescription>> bimolecular = new HashMap<String, List<ReactionDescription>>();
		Map<String, String[]> photoysis = new HashMap<String, String[]>();
		Map<String, List<ReactionDescription>> unimolecular = new HashMap<String, List<ReactionDescription>>();
		
		List<ReactionDescription> reactions = Parser.parseReactions(fileName); 
		for (ReactionDescription reaction : reactions) {
			// Note what we are currently loading
			String message = "Loading " + reaction.toString() + " (";						
			if (reaction.getReactants().length == 1) {
				// This is a unimolecular reaction
				addUnimolecularReaction(reaction, unimolecular);
				message += "unimolecular";
			} else if (Arrays.asList(reaction.getReactants()).contains("UV")) {
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
		this.photolysis = Collections.unmodifiableMap(new HashMap<String, String[]>(photoysis));
		this.bimolecular = fixMap(bimolecular);
		this.unimolecular = fixMap(unimolecular);
	}
	
	private Map<String, ReactionDescription[]> fixMap(Map<String, List<ReactionDescription>> source) {
		Map<String, ReactionDescription[]> working = new HashMap<String, ReactionDescription[]>();
		for (String key : source.keySet()) {
			ReactionDescription[] rd = new ReactionDescription[source.get(key).size()];
			for (int ndx = 0; ndx < source.get(key).size(); ndx++) {
				rd[ndx] = source.get(key).get(ndx);
			}
			working.put(key, rd);
		}
		return Collections.unmodifiableMap(new HashMap<String, ReactionDescription[]>(working));
	}
	
	/**
	 * Extract the unique entity names from the collection.
	 */
	private HashSet<String> extractEntities(Map<String, ReactionDescription[]> reactions) {
		HashSet<String> entities = new HashSet<String>();
		for (String key : reactions.keySet()) {
			entities.add(key);
			for (ReactionDescription value : reactions.get(key)) {
				for (String formula : value.getReactants()) {
					entities.add(formula);
				}
				for (String formula : value.getProducts()) {
					entities.add(formula);
				}
			}
		}
		return entities;
	}
}
