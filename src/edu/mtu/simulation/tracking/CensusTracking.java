package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.compound.Molecule;
import edu.mtu.reactor.Reactor;

/**
 * This class conducts a census of the molecules in the model at the end of each 
 * time step, it is not tuned for performance and instead favors accuracy. Due to
 * the limitations of the model, the census cannot track the products so it is 
 * best used to validate the reactants are being counted correctly.
 * 
 * NOT recommended for use in time critical application. 
 */
public class CensusTracking extends Tracker {	
	/**
	 * Constructor, prepare the list of entities.
	 */
	public CensusTracking(String fileName, boolean overwrite) {
		super(fileName, overwrite);
	}
	
	/**
	 * Iteratively count all of the molecules in the model.
	 */
	public void count() {
		try {
			// Count the entities
			Map<String, Long> counts = new HashMap<String, Long>();
			Molecule[] molecules = Reactor.getInstance().getMolecules();
			for (Molecule molecule : molecules) {
				if (!counts.containsKey(molecule.getFormula())) {
					counts.put(molecule.getFormula(), 0L);
				}
				long value = counts.get(molecule.getFormula()) + 1;
				counts.put(molecule.getFormula(), value);
			}
							
			// Write them to the file
			for (String entity : entities) {
				// Write a sentinel value for products
				if (!ReactionRegistry.getInstance().hasReactants(entity)) {
					writer.write(-1);
					continue;
				}
				if (counts.containsKey(entity)) {
					writer.write(counts.get(entity));
				} else {
					writer.write(0);
				}
			}
			writer.newline();
			writer.flush();
		} catch (IOException ex) {
			// Since we don't expect this to happen and don't have a means of recovering
			// treat this as a fatal error
			System.err.println("IOException occured while resetting the entity counts");
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}
}
