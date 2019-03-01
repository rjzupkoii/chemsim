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
import edu.mtu.util.FnvHash;

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

	private int[] entityHashes;
		
	// Mapping of all of the molecules and the basics of their reactions
	private Map<String, MoleculeDescription> moleculeDescriptions;

	// Acid dissociation reactions are [Reactant] <=> [Product] + [Product], pKa = [Value
	private Map<String, AcidDissociation> acid;
		
	// Photolysis is [Reactant] + UV -> [Product] + ... + [Product] 
	private Map<String, String[]> photolysis;
		
	// Bimolecular reactions are [Reactant] + [Reactant] -> [Product] + ... + [Product]
	private Map<String, BasicReaction[]> bimolecular;
	
	// Unimolecular reactions are [Reactant] -> [Product] + ... + [Product]
	private Map<String, BasicReaction[]> unimolecular;
	
	// Track the molecules that appear on the B side of the equation
	private HashSet<String> bSides;
	
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
	 * Add the given acid dissociation reaction to the registry.
	 */
	private void addAcidDissociation(AcidDissociation reaction, Map<String, AcidDissociation> working) throws InvalidActivityException {
		// Check our parameters
		if (reaction.reactants.length > 1) {
			throw new InvalidActivityException("Acid dissociation can only contain one reactant.");
		}
		if (working.containsKey(reaction.reactants[0])) {
			throw new InvalidActivityException("Reaction registry already cotnains acid dissocitation for " + reaction.reactants[0]);
		}
				
		working.put(reaction.reactants[0], reaction);
	}

	/**
	 * Add the given bimolecular reaction to the registry, returns the reaction as a check string.
	 */
	private void addBimolecularReaction(BasicReaction reaction, Map<String, List<BasicReaction>> working ) {
		for (String reactant : reaction.getReactants()) {
			if (!working.containsKey(reactant)) {
				working.put(reactant, new ArrayList<BasicReaction>());
			}
			if (!((ArrayList<BasicReaction>)working.get(reactant)).contains(reaction)) {
				((ArrayList<BasicReaction>)working.get(reactant)).add(reaction);
			}
		}
	}
	
	/**
	 * Add the given photolysis reaction to the registry, returns the reaction as a check string.
	 */
	private void addPhotolysisReaction(BasicReaction reaction, Map<String, String[]> working) throws InvalidActivityException {
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
	 * Add the given unimolecular reaction to the registry, returns the check string
	 */
	private void addUnimolecularReaction(BasicReaction reaction, Map<String, List<BasicReaction>> working) throws InvalidActivityException {
		// Make sure the key is valid
		String key = reaction.getReactants()[0];
		if (key.toUpperCase() == "UV") {
			throw new IllegalArgumentException("A photolysis reaction cannot be unimolecular");
		}
				
		if (!working.containsKey(key)) {
			working.put(key, new ArrayList<BasicReaction>());
		}
		
		((ArrayList<BasicReaction>)working.get(key)).add(reaction);
	}
	
	/**
	 * Clear the current contents of the registry.
	 */
	public void clear() {
		bimolecular = null;
		bSides = null;
		photolysis = null;
		unimolecular = null;
		moleculeDescriptions = null;
	}
	
	/**
	 * Returns the set of molecules that have acid dissociation reactions.
	 */
	public Set<String> getAcidDissociationReactants() {
		return acid.keySet();
	}
	
	/**
	 * Returns the chemical equation associated with the given reactant. 
	 */
	public AcidDissociation getAcidDissociation(String reactant) {
		return acid.get(reactant);
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public BasicReaction[] getBimolecularReaction(Molecule molecule) {
		return bimolecular.get(molecule.getFormula());
	}
	
	/**
	 * Returns the list of bimolecular reactions for the chemical species or null.
	 */
	public BasicReaction[] getBimolecularReaction(String formula) {
		return bimolecular.get(formula);
	}
	
	/**
	 * Get a list of all the entities in the registry.
	 */
	public Set<String> getEntityList() {
		return moleculeDescriptions.keySet();
	}
		
	/**
	 * Get a list of all of the entity hashes in the registry.
	 */
	public int[] getEntityHashList() {
		return entityHashes;
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
	public BasicReaction[] getUnimolecularReaction(Molecule molecule) {
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
		MoleculeDescription result = moleculeDescriptions.get(formula);
		return (result == null) ? false : result.hasReactants;
	}
	
	/**
	 * Load the contents of the indicated file into the registry.
	 * 
	 * @param fileName The name and path of the file to be loaded.
	 * @return Returns a report of what was loaded.
	 */
	public String load(String fileName) throws IOException {
		
		// Define our working maps
		Map<String, AcidDissociation> acid = new HashMap<String, AcidDissociation>();
		Map<String, List<BasicReaction>> bimolecular = new HashMap<String, List<BasicReaction>>();
		Map<String, String[]> photoysis = new HashMap<String, String[]>();
		Map<String, List<BasicReaction>> unimolecular = new HashMap<String, List<BasicReaction>>();		
		
		
		HashSet<String> aSides = new HashSet<String>();
		bSides = new HashSet<String>();
		
		// Define a hash map so we can check for dispropration reaction, namely two of the same reactions		
		HashMap<Integer, Integer> disproportionationCheck = new HashMap<Integer, Integer>();

		StringBuilder message = new StringBuilder();
		List<ChemicalEquation> reactions = Parser.parseReactions(fileName); 
		for (ChemicalEquation ce : reactions) {
			
			// Check to see if this is acid dissociation
			if (ce instanceof AcidDissociation) {
				AcidDissociation ad = (AcidDissociation)ce;
				message.append(ad.toString() + " (acid dissociation)\n");
				addAcidDissociation(ad, acid);
				continue;
			}
						
			// Must be a basic reaction
			BasicReaction reaction = (BasicReaction)ce;
			
			// Update our a-side
			aSides.add(reaction.getReactants()[0]);
			
			message.append(reaction.toString() + " (");						
			if (reaction.getReactants().length == 1) {
				// This is a unimolecular reaction
				addUnimolecularReaction(reaction, unimolecular);
				message.append("unimolecular");
			} else if (Arrays.asList(reaction.getReactants()).contains("UV")) {
				// This is a photolysis reaction
				addPhotolysisReaction(reaction, photoysis);
				message.append("photolysis");
			} else {
				// Must be a bimolecular reaction
				addBimolecularReaction(reaction, bimolecular);
				message.append("bimolecular");
				
				// Update our b-side
				bSides.add(reaction.getReactants()[1]);
			}
			if (reaction.getReactionRatio() != 1.0) {
				message.append(", " + reaction.getReactionRatio());
			}
			message.append(")\n");
			
			// Update the disproportionation checking
			if (ce.reactants.length == 1) { 
				updateCheck(disproportionationCheck, ce.reactants[0]);
			} else {
				updateCheck(disproportionationCheck, ce.reactants[0] + " + " + ce.reactants[1]);
			}			
		}
		
		// Everything is loaded, now lock it down
		this.photolysis = Collections.unmodifiableMap(new HashMap<String, String[]>(photoysis));
		this.bimolecular = fixMap(bimolecular);
		this.unimolecular = fixMap(unimolecular);
		this.acid = Collections.unmodifiableMap(new HashMap<String, AcidDissociation>(acid));
			
		// Process the current HashSet of B sides and remove ones that are also
		// A sides. Double counted ones will be noted by Reaction.bimolecularReaction which 
		// will give the current molecule 50-50 odds of checking. 
		for (String aSide : aSides) {
			if (bSides.contains(aSide)) {
				bSides.remove(aSide);
			}
		}
				
		// Build the molecule descriptions
		buildMoleculeDescriptions();
		buildEntityHash(disproportionationCheck);
				
		// Return the report
		return message.toString();
	}
	
	/**
	 * Helper function to update the check map.
	 */
	private void updateCheck(HashMap<Integer, Integer> disproportionationCheck, String check) {
		int key = FnvHash.fnv1a32(check);
		if (!disproportionationCheck.containsKey(key)) {
			disproportionationCheck.put(key, 0);
		}
		int value = disproportionationCheck.get(key) + 1;
		disproportionationCheck.put(key, value);
	}

	/**
	 * Helper function to convert working hashes with lists over to unmodifiable maps with arrays.
	 */
	private Map<String, BasicReaction[]> fixMap(Map<String, List<BasicReaction>> source) {
		Map<String, BasicReaction[]> working = new HashMap<String, BasicReaction[]>();
		for (String key : source.keySet()) {
			BasicReaction[] rd = new BasicReaction[source.get(key).size()];
			for (int ndx = 0; ndx < source.get(key).size(); ndx++) {
				rd[ndx] = source.get(key).get(ndx);
			}
			working.put(key, rd);
		}
		return Collections.unmodifiableMap(new HashMap<String, BasicReaction[]>(working));
	}
	
	/**
	 * Build the array that contains the entity hashes that are present.
	 */
	private void buildEntityHash(HashMap<Integer, Integer> disproportionationCheck) {
		// Add all of the disproportionation hashes that have an appearance count greater than one
		HashSet<Integer> working = new HashSet<Integer>();
		for (int key : disproportionationCheck.keySet()) {
			int value = disproportionationCheck.get(key);
			if (value > 1) {
				working.add(key);
			}
		}
		
		for (String key : moleculeDescriptions.keySet()) {
			working.add(FnvHash.fnv1a32(key));
		}

		// Allocate an array and move the data over... deals with the Java idiosyncrasy between int and Integer
		entityHashes = new int[working.size()];
		int ndx = 0;
		for (int hash : working) {
			entityHashes[ndx++] = hash;
		}
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
		entities.addAll(extractBasic(unimolecular));
		entities.addAll(extractBasic(bimolecular));
		entities.addAll(extractAcid(acid));
		
		// Now use that list to start building the descriptions
		moleculeDescriptions = new HashMap<String, MoleculeDescription>();
		for (String formula : entities) {
			MoleculeDescription md = new MoleculeDescription();
			md.hasBimolecular = bimolecular.containsKey(formula);
			md.hasPhotolysis = photolysis.containsKey(formula);
			md.hasUnimolecular = unimolecular.containsKey(formula);			
			md.hasReactants = (md.hasBimolecular || md.hasPhotolysis || md.hasUnimolecular);
			md.hasDissolvedReactants = checkDissolvedReactants(formula);
			md.isRadical = formula.startsWith("*") || formula.endsWith("*");
			md.isBSide = bSides.contains(formula);
			extractReactants(formula, md);
			moleculeDescriptions.put(formula, md);
		}
	}
	
	/**
	 * Check to see if the given compound has any dissolved reactants.
	 */
	private boolean checkDissolvedReactants(String formula) {
		BasicReaction[] rd = bimolecular.get(formula);
		if (rd != null) {
			for (BasicReaction reaction : rd) {
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
	private void extractReactants(String formula, MoleculeDescription md) {
		BasicReaction[] rds = bimolecular.get(formula);
		if (rds == null) {
			md.reactsWithHash = new Integer[0];
			md.interactionRadius = new int[0];
			return;
		}
		
		ArrayList<Integer> entities = new ArrayList<Integer>();
		ArrayList<Integer> radii = new ArrayList<Integer>();
		for (BasicReaction rd : rds) {
			String[] products = rd.getReactants();

			// Note the hash to use
			int index = (products[0].equals(formula)) ? 1 : 0;
			int hash = FnvHash.fnv1a32(products[index]);
			
			// Set the values
			entities.add(hash);
			radii.add(rd.getInteractionRadius());				
		}
		
		// Java idiosyncrasy, going to a primitive array isn't that easy 
		md.reactsWithHash = new Integer[entities.size()];
		md.interactionRadius = new int[entities.size()];
		for (int ndx = 0; ndx < entities.size(); ndx++) {
			md.reactsWithHash[ndx] = entities.get(ndx);
			md.interactionRadius[ndx] = radii.get(ndx);
		}		
	}
	
	private HashSet<String> extractAcid(Map<String, AcidDissociation> reactions) {
		HashSet<String> entities = new HashSet<String>();
		for (String key : reactions.keySet()) {
			entities.add(key);
			for (String formula : reactions.get(key).getProducts()) {
				entities.add(formula);
			}
		}
		return entities;
	}
	
	/**
	 * Extract the unique entity names from the collection.
	 */
	private HashSet<String> extractBasic(Map<String, BasicReaction[]> reactions) {
		HashSet<String> entities = new HashSet<String>();
		for (String key : reactions.keySet()) {
			entities.add(key);
			for (BasicReaction value : reactions.get(key)) {
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
