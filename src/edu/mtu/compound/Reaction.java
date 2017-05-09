package edu.mtu.compound;

import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.ChemSim;
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
	
	private boolean bimolecularReaction(Species species) {
		
		// TODO Write this method
		
		return false;
	}
	
	private void createAt(String formula, Int3D location) {
		ChemSim state = ChemSim.getInstance();
		Species species = new Species(formula);
		species.setStoppable(state.schedule.scheduleRepeating(species));
		state.getCompounds().setObjectLocation(species, location);
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
		
		// TODO Otherwise, first check to see if it should occur
		
		// Decay the species based upon it's reaction with UV
		Int3D location = ChemSim.getInstance().getCompounds().getObjectLocation(species);
		for (String product : products) {
			createAt(product, location);
		}
		species.dispose();
		return true;
	}	
	
	private boolean unimolecularDecay(Species species) {
		
		// TOOD Write this method
		
		return false;
	}
}
