package edu.mtu.compound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mtu.catalog.ReactionDescription;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.primitives.Int3D;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.schedule.Schedule;

/**
 * This class provides a means for a chemical species to react with other species.
 */
public class Reaction {
	
	// TODO Find a better place to put this
	private static final String[] disolved = { "O2", "H2O" };
	
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
	 * Have the chemical species disproportionate according to its reaction rate.
	 * 
	 * @param molecule of the chemical for the disproportionation.
	 * @param cell where the reaction should take place.
	 */
	public void disproportionate(DisproportionatingMolecule molecule) {

		// Note the age
		int age = molecule.updateAge();

		// Keep track of the odds
		boolean probabilistic = false;
		ArrayList<Double> reactionOdds = new ArrayList<Double>();
		
		// Find the indicies and odds of valid reactions
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (int ndx = 0; ndx < molecule.getReactions().size(); ndx++) {
			// Check to see if this reaction shouldn't occur yet
			ReactionDescription reaction = molecule.getReactions().get(ndx);
			if (reaction.getReactionRate() > age) {
				continue;
			}
			
			// Note the odds
			double odds = reaction.getReactionOdds();
			probabilistic = probabilistic || (odds < 1);
			reactionOdds.add(odds);			
			
			// Note the index for removal
			indicies.add(ndx);
		}
				
		if (probabilistic) {
			doProbabilistic(molecule, indicies, reactionOdds);
		} else {
			// Process the reactions
			for (int index : indicies) {
				ReactionDescription reaction = molecule.getReactions().get(index);
				// Create the products for the reaction
				Reactor reactor = Reactor.getInstance();
				Int3D location = reactor.getLocation(molecule);
				for (String product : reaction.getProducts()) {			
					create(product, location);
				}
			}
		}
			
		// Remove the reactions that occurred
		Collections.sort(indicies, Collections.reverseOrder());
		for (int ndx : indicies) {
			molecule.getReactions().remove(ndx);
		}
	}
	
	private void doProbabilistic(DisproportionatingMolecule molecule, ArrayList<Integer> indicies, ArrayList<Double> reactionOdds) {
		// Add the odds so we can do a single dice roll
		int size = reactionOdds.size();
		double value = 0;
		for (int ndx = 1; ndx < size; ndx++) {
			value = reactionOdds.get(ndx) + reactionOdds.get(ndx - 1);
			reactionOdds.set(ndx, value);
		}
		if (value != 1.0) {
			throw new IllegalArgumentException("Total odds of the reaction '" + molecule.getFormula() + "' cannot exceed 1.0");
		}
		double selected = ChemSim.getInstance().random.nextDouble();
		
		// Select the correct reaction
		double previous = 0.0;
		for (int ndx = 0; ndx < reactionOdds.size(); ndx++) {
			
			// If the selected value is in the range, then create the products and return
			if (previous <= selected && selected <= reactionOdds.get(ndx)) {
				ReactionDescription reaction = molecule.getReactions().get(indicies.get(ndx));
				Int3D location = Reactor.getInstance().getLocation(molecule);
				for (String product : reaction.getProducts()) {			
					create(product, location);
				}
				return;
			}
			
			previous = reactionOdds.get(ndx);
		}
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param species of chemical for the reaction.
	 * @return True if a reaction occured, false otherwise.
	 */
	public boolean react(Molecule molecule) {
		// Perform any relevant dispropriation reactions
		if (molecule instanceof DisproportionatingMolecule) {
			 disproportionate((DisproportionatingMolecule)molecule);
			 return true;
		}
		
		// First, see if there are any bimolecular reactions to take place
		if (bimolecularReaction(molecule)) {
			return true;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (unimolecularDecay(molecule)) {
			return true;
		}
		
		// Third, see if photolysis needs to take place
		return photolysis(molecule);
	}
	
	/**
	 * Create a molecule of the given type at the given location.
	 */
	private void create(String formula, Int3D location) {
		Molecule entity = new Molecule(formula);
		ChemSim.getSchedule().insert(entity);
		ChemSim.getTracker().update(formula, 1);
		Reactor.getInstance().insert(entity, location);
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
		for (Object reactant : Reactor.getInstance().getMolecules(molecule)) {
			
			// Break when we encounter a null
			if (reactant == null) {
				break;
			}
			
			if (reactant.equals(molecule)) {
				continue;
			}
									
			// Process the reactants and press on if a match is not found
			if (process(molecule, (Molecule)reactant, reactions)) {
				return true;
			}
		}
		
		// Check to see if there are any dissolved molecule we should be aware of
		for (String reactant : disolved) {
			if (process(molecule, new DissolvedMolecule(reactant), reactions)) {
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
		
		// Check to see if the reaction occurred based upon decay rates
		double decay = ChemSim.getProperties().getHydrogenPeroxideDecay();
		if (ChemSim.getInstance().random.nextDouble() > decay) {
			return false;
		}
		
		// Add the products at this location
		Reactor reactor = Reactor.getInstance();
		Int3D location = reactor.getLocation(molecule);
		for (String product : products) {
			create(product, location);
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
			ChemSim.getTracker().update(product.getFormula(), 1);
		} else {
			// A standard reaction is occurring
			for (String product : matched.get(0).getProducts()) {
				create(product, location);
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
		List<ReactionDescription> reactions = ReactionRegistry.getInstance().getUnimolecularReaction(molecule);
		if (reactions == null) {
			return false;
		}
		
		// Return the results of processing the reactions
		return process(molecule, null, reactions);
	}
}
