package edu.mtu.reaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.special.Erf;

import edu.mtu.compound.DisproportionatingMolecule;
import edu.mtu.compound.DissolvedMolecule;
import edu.mtu.compound.Molecule;
import edu.mtu.compound.MoleculeFactory;
import edu.mtu.primitives.Sparse3DLattice;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

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
	 * Conduct the acid dissociation that applies to the given reactant.
	 * 
	 * @param reactant to perform the acid dissociation of.
	 */
	public void doAcidDissociation(String reactant) {
		
		// Get the reaction
		AcidDissociation ad = ReactionRegistry.getInstance().getAcidDissociation(reactant);
		
		// Calculate the ratio of [A-] to [HA]
		double pH = ChemSim.getProperties().getPH();
		double ratio = Math.pow(10, -ad.pKa()) / Math.pow(10, -pH);
		
		// Get the count of [HA]
		long count = ChemSim.getTracker().getCount(reactant);
		
		// Calculate [HA], [A-], and [H+]
		long aMinus = (long)Math.floor(count * ratio);
		aMinus = (aMinus > count) ? count : aMinus;
		long hPlus = aMinus;
		long ha = count - aMinus;
		
		// Balance out the molecules in the system
		balanceMolecules(ad.acid(), ha);
		balanceMolecules(ad.conjugateBase(), aMinus);
		
		// Note the ion count
		ChemSim.getTracker().update(ad.hydrogenIon(), hPlus);
	}
	
	/**
	 * Balance the molecules that are in the system by either, adding or removing 
	 * them up to the given count.
	 */
	private void balanceMolecules(String reactant, long count) {

		// Note the difference
		long total = ChemSim.getTracker().getCount(reactant);
		
		// Do we need to remove molecules?
		Reactor reactor = Reactor.getInstance();
		for (; total > count; total--) {
			Molecule molecule = reactor.getFirst(reactant);
			molecule.dispose();
		}
		
		// We are adding molecules
		int[] container = reactor.dimensions;
		Random random = ChemSim.getInstance().getRandom();
		for (; total < count; total++) {
			int x = random.nextInt(container[0]), y = random.nextInt(container[1]), z = random.nextInt(container[2]);
			MoleculeFactory.create(reactant, new int[] { x, y, z });
		}
	}
				
	/**
	 * Have the chemical species disproportionate according to its reaction rate.
	 * 
	 * @param molecule of the chemical for the disproportionation.
	 * @param cell where the reaction should take place.
	 */
	public void disproportionate(DisproportionatingMolecule molecule) {
		
		// Local pointer to the reactions
		BasicReaction[] reactions = molecule.getReactions();
		int size = reactions.length;

		// Keep track of the odds
		boolean probabilistic = false;
		ArrayList<Double> reactionOdds = new ArrayList<Double>();
		
		// Find the indicies and odds of valid reactions
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (int ndx = 0; ndx < size; ndx++) {
			// Press on if the description is null
			if (reactions[ndx] == null) {
				continue;
			}
						
			// Note the odds
			double odds = reactions[ndx].getReactionRatio();
			probabilistic = probabilistic || (odds < 1);
			reactionOdds.add(odds);			
			
			// Note the index for removal
			indicies.add(ndx);
		}
				
		if (probabilistic) {
			doProbabilistic(molecule, reactions, indicies, reactionOdds);
		} else {
			// Process the reactions
			for (int index : indicies) {
				// Create the products for the reaction
				Reactor reactor = Reactor.getInstance();
				int[] location = reactor.getLocation(molecule);
				for (String formula : reactions[index].getProducts()) {			
					MoleculeFactory.create(formula, location);
				}
			}
		}
			
		// Null out the reactions that occurred
		for (int ndx : indicies) {
			reactions[ndx] = null;
		}
	}
	
	private void doProbabilistic(DisproportionatingMolecule molecule, BasicReaction[] reactions, ArrayList<Integer> indicies, ArrayList<Double> reactionOdds) {
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
		double selected = ((XoRoShiRo128PlusRandom)ChemSim.getInstance().getRandom()).nextDoubleFast();
		
		// Select the correct reaction
		double previous = 0.0;
		for (int ndx = 0; ndx < reactionOdds.size(); ndx++) {
			
			// If the selected value is in the range, then create the products and return
			if (previous <= selected && selected <= reactionOdds.get(ndx)) {
				int[] location = Reactor.getInstance().getLocation(molecule);
				for (String formula : reactions[indicies.get(ndx)].getProducts()) {			
					MoleculeFactory.create(formula, location);
				}
				return;
			}
			
			previous = reactionOdds.get(ndx);
		}
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param moleclue for the reaction.
	 * @return True if a reaction occurred, false otherwise.
	 */
	public boolean react(DisproportionatingMolecule moleclue) {
		 disproportionate(moleclue);
		 return true;
	}
	
	/**
	 * Have the chemical species indicated react with anything located here.
	 * 
	 * @param moleclue for the reaction.
	 * @return True if a reaction occurred, false otherwise.
	 */
	public boolean react(Molecule molecule) {
		// Finally, see if photolysis needs to take place
		if (molecule.hasPhotolysis() && photolysis(molecule)) {
			return true;
		}
		
		// First, see if there are any bimolecular reactions to take place
		if (molecule.hasBimoleculear() && bimolecularReaction(molecule)) {
			return true;
		}
		
		// Second, see if unimolecular decay needs to take place
		if (molecule.hasUnimolecular() && unimolecularDecay(molecule)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Perform a bimolecular reaction on the given species.
	 */
	private boolean bimolecularReaction(Molecule molecule) {
		
		// Is this a b-side molecule?
		if (molecule.isBSide()) {
			return false;
		}
		
		// Get the possible hashes
		Integer[] hashes = molecule.getReactantHashes(); 
		
		// Check to see if there are any dissolved molecule we should be aware of,
		// this comes first since a molecule that reacts with dissolved molecules
		// is unlikely to do anything else
		if (molecule.hasDissolvedReactants()) {		
			for (DissolvedMolecule reactant : ReactionRegistry.DissolvedMoleclues) {
				for (int formulaHash : hashes) {
					if (reactant.sameEntity(formulaHash)) {
						int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
						BasicReaction[] reactions = ReactionRegistry.getInstance().getBimolecularReaction(molecule);
						if (processList(molecule, reactant, location, reactions)) {
							return true;
						}
					}
				}
			}
		}
								
		// Collect some information
		Random random = ChemSim.getInstance().getRandom();
		int step = ChemSim.getSchedule().getTimeStep();		
		
		// Note our hash once
		int hash = molecule.getEntityTypeTag();
		
		// Get the possible interaction radii
		int[] radii = molecule.getInteractionRadii();

		// Note the current location
		Sparse3DLattice grid = Reactor.getInstance().grid;
		int[] location = grid.getObjectLocation(molecule);
		int x1 = location[0], y1 = location[1], z1 = location[2];
		
		for (int ndx = 0; ndx < hashes.length; ndx++) {
			// Since a molecule may react with others of the same species
			// check to see if we are looking at that right now. If so do 
			// a 50-50 flip to see if we should continue. This keeps Pogson's
			// equation balanced.
			if (hash == hashes[ndx] && random.nextInt(2) == 0) {
				continue;
			}
			
			// Find the first that matches
			Molecule match = (Molecule)grid.findFirstByTag(molecule, hashes[ndx], radii[ndx]);
			if (match == null) {
				continue;
			}
			
			// Is the molecule free?
			if (!match.isFree(step)) {
				continue;
			}			
			
			// Calculate the distance, but return immediately of we occupy the same space
			location = grid.getObjectLocation(match);
			int x = x1 - location[0];
			int y = y1 - location[1];
			int z = z1 - location[2];
			if (x == 0 && y == 0 && z == 0) {
				return processRadius(molecule, match, radii[ndx]);
			}
			double d = Math.sqrt(x*x + y*y + z*z);
			
			// Roll the dice
			if (random.nextGaussian() < Erf.erfc(d / radii[ndx])) {
				return processRadius(molecule, match, radii[ndx]);
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
		
		// Return if this isn't hydrogen peroxide
		if (!molecule.getFormula().equals("H2O2")) {
			return false;
		}
		
		// Check to see if the reaction occurred based upon decay rates
		double decay = ChemSim.getProperties().getDecayProbability();
		XoRoShiRo128PlusRandom random = (XoRoShiRo128PlusRandom)ChemSim.getInstance().getRandom();
		if (random.nextDoubleFast() > decay) {
			return false;
		}

		// Create the relevant products, note that hydroxyl gets special treatment
		int[] location = Reactor.getInstance().getLocation(molecule);
		for (String product : ReactionRegistry.getInstance().getPhotolysisReaction(molecule)) {
			MoleculeFactory.create(product, location);
		}
			
		// Note that a reaction occurred, molecule will dispose of itself
		return true;
	}	
		
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean processRadius(Molecule molecule, Molecule reactant, int radius) {
		// Find the correct reactions
		List<BasicReaction> matched = new ArrayList<BasicReaction>();
		BasicReaction[] reactions = ReactionRegistry.getInstance().getBimolecularReaction(molecule);
		for (BasicReaction rd : reactions) {
			if (rd.checkReactants(molecule, reactant) && rd.getInteractionRadius() == radius) {
				matched.add(rd);
			}
		}
		int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
		return react(molecule, reactant, location, matched);
	}
	
	/**
	 * Process the reactions that are possible for this entity.
	 */
	private boolean processList(Molecule molecule, Molecule reactant, int[] location, BasicReaction[] reactions) {
		List<BasicReaction> matched = new ArrayList<BasicReaction>();
		for (BasicReaction rd : reactions) {
			if (rd.checkReactants(molecule, reactant)) {
				matched.add(rd);
			}
		}
		return react(molecule, reactant, location, matched);
	}
		
	/**
	 * Do the steps related to the actual reaction.
	 */
	private boolean react(Molecule molecule, Molecule reactant, int[] location, List<BasicReaction> matched) {
		// Return on a bad call
		if (matched.size() == 0) {
			throw new IllegalAccessError(String.format("No matches found for %s, %s", molecule, reactant));
		}
		
		// Add the molecules to the model
		if (matched.size() > 1) {
			// Disproportion is occurring
			MoleculeFactory.create(molecule, reactant, matched, location);
		} else {
			// A standard reaction is occurring
			MoleculeFactory.create(matched.get(0).getProducts(), location);
		}
		
		// Clean up the reactant that was involved
		if (reactant != null) {
			reactant.dispose();
		}
		
		// The molecule will be dispose itself
		return true;
	}
	
	/**
	 * Perform a unimolecular reaction on the given species.
	 */
	private boolean unimolecularDecay(Molecule molecule) {
		int[] location = Reactor.getInstance().grid.getObjectLocation(molecule);
		BasicReaction[] reactions = ReactionRegistry.getInstance().getUnimolecularReaction(molecule);
		return processList(molecule, null, location, reactions);
	}
}
