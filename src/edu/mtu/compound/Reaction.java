package edu.mtu.compound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionDescription;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.schedule.Schedule;
import sim.util.Int3D;

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
	 * Calculate what the per cell decay quantity should be.
	 */
	public static double calculateDecayQuantity(int cells, double volume, double avagadroNumber, double mols) {
		double decay = (mols * volume) / Math.pow(cells, 3);
		return decay * avagadroNumber;
	}
		
	/**
	 * Have the chemical species disproportionate according to its reaction rate.
	 * 
	 * @param molecule of the chemical for the disproportionation.
	 * @param cell where the reaction should take place.
	 */
	public void disproportionate(DisproportionatingMolecule molecule) {

		// Note the age
		int age = molecule.updateAge();
		
		// Process potential products
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (int ndx = 0; ndx < molecule.getReactions().size(); ndx++) {
			// Check to see if this reaction shouldn't occur yet
			ReactionDescription reaction = molecule.getReactions().get(ndx);
			if (reaction.getReactionRate() > age) {
				continue;
			}
			
			// Note the index for removal
			indicies.add(ndx);
			
			// Create the products for the reaction
			Reactor reactor = Reactor.getInstance();
			Int3D location = reactor.getLocation(molecule);
			for (String product : reaction.getProducts()) {			
				Molecule entity = new Molecule(product);
				ChemSim.getSchedule().insert(entity);
				reactor.insert(entity, location);
			}
		}
		
		// Remove the reactions that occurred
		Collections.sort(indicies, Collections.reverseOrder());
		for (int ndx : indicies) {
			molecule.getReactions().remove(ndx);
		}
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param species of chemical for the reaction.
	 */
	public void react(Molecule molecule) {
		// Perform any relevant dispropriation reactions
		if (molecule instanceof DisproportionatingMolecule) {
			disproportionate((DisproportionatingMolecule)molecule);
			return;
		}
		
		// First, see if there are any bimolecular reactions to take place
		if (bimolecularReaction(molecule)) {
			return;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (unimolecularDecay(molecule)) {
			return;
		}
		
		// Third, see if photolysis needs to take place
		if (photolysis(molecule)) {
			return;
		}
	}
	
	/**
	 * Perform a bimolecular reaction on the given species.
	 */
	private boolean bimolecularReaction(Molecule molecule) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(molecule);
		if (reactions == null) {
			return false;
		}
		
		// Check to see what other species at this location react with the given one
		Reactor reactor = Reactor.getInstance();
		Int3D location = reactor.getLocation(molecule);
		for (Molecule reactant : reactor.getMolecules(location)) {
			if (reactant.equals(molecule)) {
				continue;
			}
									
			// Process the reactants and press on if a match is not found
			if (process(molecule, reactant, reactions)) {
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
	private boolean photolysis(Molecule molecule) {
		// Return if there are no products registered
		List<String> products = ReactionRegistry.getInstance().getPhotolysisReaction(molecule);
		if (products == null) {
			return false;
		}
		
		// Add the products at this location
		Reactor reactor = Reactor.getInstance();
		Int3D location = reactor.getLocation(molecule);
		for (String product : products) {
			Molecule entity = new Molecule(product);
			ChemSim.getSchedule().insert(entity);
			reactor.insert(entity, location);
		}
		
		// Note that a reaction occurred, molecule will dispose of itself
		return true;
	}	
		
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean process(Molecule molecule, Molecule reactant, List<ReactionDescription> reactions) {
		// Can this reaction occur?
		List<ReactionDescription> matched = new ArrayList<ReactionDescription>();
		for (ReactionDescription rd : reactions) {
			if (rd.checkReactants(molecule, reactant)) {
				matched.add(rd);
			}
		}
		if (matched.isEmpty()) {
			return false;
		}
		
		// Add the molecules to the model
		Reactor reactor = Reactor.getInstance();
		Int3D location = reactor.getLocation(molecule);
		Schedule schedule = ChemSim.getSchedule();
		if (matched.size() > 1) {
			// Disproportion is occurring
			Molecule product = DisproportionatingMolecule.create(molecule, reactant, reactions);
			schedule.insert(product);
			reactor.insert(product, location);
		} else {
			// A standard reaction is occurring
			for (String product : matched.get(0).getProducts()) {
				Molecule entity = new Molecule(product);
				ChemSim.getSchedule().insert(entity);
				reactor.insert(entity, location);
			}
		}
		
		// Clean up the reactant that was involved
		if (reactant != null) {
			reactant.dispose();
		}
		
		// The molecule will be disposed of by itself
		return true;
	}
		
	/**
	 * Perform a unimolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Molecule molecule) {
		// Get the possible reactions for this species
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getBiomolecularReaction(molecule);
		if (reactions == null) {
			return false;
		}
		
		// Return the results of processing the reactions
		return process(molecule, null, reactions);
	}
}
