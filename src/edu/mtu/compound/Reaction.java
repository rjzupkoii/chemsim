package edu.mtu.compound;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.mtu.catalog.ReactionDescription;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.ChemSim;
import sim.util.Bag;
import sim.util.Int3D;

/**
 * This class provides a means for a chemical species to react with other species.
 */
public class Reaction {

	private MersenneTwisterFast random = null;
	private static Reaction instance = new Reaction();
	
	/**
	 * Singleton constructor.
	 */
	private Reaction() { 
		random = ChemSim.getInstance().random;
	}
	
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
	 */
	public void disproportionate(DisproportionatingSpecies species) {
		
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param species of chemical for the reaction.
	 */
	public void react(Species species) {
		// First, see if there are any bimolecular reactions to take place
		if (bimolecularReaction(species)) {
			return;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (unimolecularDecay(species)) {
			return;
		}
		
		// Third, see if photolysis needs to take place
		if (photolysis(species)) {
			return;
		}
	}
	
	/**
	 * Perform a bimolecular reaction on the given species.
	 */
	private boolean bimolecularReaction(Species species) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(species);
		if (reactions == null) {
			return false;
		}
		
		// Get other species at this location
		ChemSim state = ChemSim.getInstance();
		Int3D location = state.getCompounds().getObjectLocation(species);
		Bag bag = state.getCompounds().getObjectsAtLocation(location);
		if (bag.numObjs == 1) {
			return false;
		}
		
		for (int ndx = 0; ndx < bag.numObjs; ndx++) {
			// Press on if this reactant is the same as the species we were provided
			Species reactant = (Species)bag.get(ndx);
			if (reactant.equals(species)) {
				continue;
			}
			
			// Process the reactants and press on if a match is not found
			if (process(species, reactant, reactions, location)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Insert the species into the schedule at the given location.
	 */
	private void createAt(Species species, Int3D location) {
		ChemSim state = ChemSim.getInstance();
		species.setStoppable(state.schedule.scheduleRepeating(species));
		state.getCompounds().setObjectLocation(species, location);		
	}
	
	/**
	 * Create a species with the given formula at the indicated location.
	 */
	private void createAt(String formula, Int3D location) {
		Species species = new Species(formula);
		createAt(species, location);
	}
	
	/**
	 * Attempt photolysis on the species.
	 * 
	 * @param species to attempt photolysis on.
	 * @return True if it occurred, false otherwise..
	 */
	private boolean photolysis(Species species) {
		// Return if there is nothing to do
		if (!species.getPhotosensitive()) {
			return false;
		}
				
		// Return if there are no products registered
		List<String> products = ReactionRegistry.getInstance().getPhotolysisReaction(species);
		if (products == null) {
			return false;
		}
		
		// TODO Otherwise, first check to see if it should occur (dice roll)
		
		// Decay the species based upon it's reaction with UV
		Int3D location = ChemSim.getInstance().getCompounds().getObjectLocation(species);
		for (String product : products) {
			createAt(product, location);
		}
		species.dispose();
		return true;
	}	
	
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean process(Species species, Species reactant, List<ReactionDescription> reactions, Int3D location) {
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
		
		// Should a disproportionation occur?
		if (matched.size() > 1) {
			Species product = DisproportionatingSpecies.create(species, reactant, reactions);
			createAt(product, location);
			species.dispose();
			reactant.dispose();
			return true;
		}
		
		// A standard reaction is occurring
		for (String formula : matched.get(0).getProducts()) {
			createAt(formula, location);
		}
		species.dispose();
		if(reactant != null) {
			reactant.dispose();
		}
		return true;
	}
	
	/**
	 * Perform a unicolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Species species) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(species);
		if (reactions == null) {
			return false;
		}

		// Return the results of processing the reactions
		Int3D location = ChemSim.getInstance().getCompounds().getObjectLocation(species);
		return process(species, null, reactions, location);
	}
}
