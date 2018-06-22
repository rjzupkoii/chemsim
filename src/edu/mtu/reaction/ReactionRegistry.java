package edu.mtu.reaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.Parser;

/**
 * This singleton contains a look up of the reactions in the simulation. In order to account for 
 * disproportionation each of the species is associated with a list of formulas.
 */
public class ReactionRegistry {
	
	/**
	 * Dissolved molecules that are always present in the reactor.
	 */
	public static final DissolvedMolecule[] DissolvedMoleclues = { 
			new DissolvedMolecule("O2"),
			new DissolvedMolecule("H2O")
	};
	
	private static ReactionRegistry instance = new ReactionRegistry();

	// Mapping of all of the molecules and the basics of their reactions
	private Map<String, MoleculeDescription> moleculeDescriptions;

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
		moleculeDescriptions = null;
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public ReactionDescription[] getBimolecularReaction(Molecule molecule) {
		return bimolecular.get(molecule.getFormula());
	}
	
	/**
	 * Get a list of all the entities in the registry.
	 */
	public Set<String> getEntityList() {
		return moleculeDescriptions.keySet();
	}
				
	/**
	 * Returns the photolysis products for the chemical species or null.
	 */
	public String[] getPhotolysisReaction(Molecule molecule) {
		return photolysis.get(molecule.getFormula());
	}
		
	/**
	 * Returns the list of unimolecular reactions for the chemical species or null.
	 */
	public ReactionDescription[] getUnimolecularReaction(Molecule molecule) {
		return unimolecular.get(molecule.getFormula());
	}
	
	/**
	 * Get the molecule description for the given formula.
	 */
	public MoleculeDescription getMoleculeDescription(String formula) {
		return moleculeDescriptions.get(formula);
	}
		
	/**
	 * Check to see if the given formula has any reactants. 
	 */
	public boolean hasReactants(String formula) {
		return moleculeDescriptions.get(formula).hasReactants;
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
		
		// Build the molecule descriptions
		buildMoleculeDescriptions();
	}
	
	/**
	 * Helper function to convert working hashes with lists over to unmodifiable maps with arrays.
	 */
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
	 * Build out all of the molecule descriptions once so we don't have to do any processing again.
	 */
	private void buildMoleculeDescriptions() {
		// Start by finding all of our unique entities
		HashSet<String> entities = new HashSet<String>();
		for (String key : photolysis.keySet()) {
			entities.add(key);
			for (String value : photolysis.get(key)) {
				entities.add(value);
			}
		}
		entities.addAll(extractEntities(unimolecular));
		entities.addAll(extractEntities(bimolecular));
		
		// Now use that list to start building the descriptions
		moleculeDescriptions = new HashMap<String, MoleculeDescription>();
		for (String formula : entities) {
			MoleculeDescription md = new MoleculeDescription();
			md.hasBimolecular = bimolecular.containsKey(formula);
			md.hasPhotolysis = photolysis.containsKey(formula);
			md.hasUnimolecular = unimolecular.containsKey(formula);			
			md.hasReactants = (md.hasBimolecular || md.hasPhotolysis || md.hasUnimolecular);
			md.hasDissolvedReactants = checkDissolvedReactants(formula);
			md.reactsWith = extractReactants(formula);
			moleculeDescriptions.put(formula, md);
		}
	}
	
	/**
	 * Check to see if the given compound has any dissolved reactants.
	 */
	private boolean checkDissolvedReactants(String formula) {
		ReactionDescription[] rd = bimolecular.get(formula);
		if (rd != null) {
			for (ReactionDescription reaction : rd) {
				for (String compound : reaction.getReactants()) {
					for (DissolvedMolecule molecule : DissolvedMoleclues) {
						if (molecule.getFormula().equals(compound)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Get the list of reactants this compound reacts with
	 */
	private String[] extractReactants(String formula) {
		ReactionDescription[] rds = bimolecular.get(formula);
		if (rds == null) {
			return new String[0];
		}
		
		HashSet<String> entities = new HashSet<String>();
		for (ReactionDescription rd : rds) {
			String[] products = rd.getReactants();
			
			// Check for reactions with self
			if (products[0].equals(products[1])) {
				entities.add(products[0]);
			}
			
			// Otherwise add the other compound
			if (products[0].equals(formula)) {
				entities.add(products[1]);
			} else {
				entities.add(products[0]);
			}
		}
		
		String[] results = new String[entities.size()];
		results = entities.toArray(results);
		return results;
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
