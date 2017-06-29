package edu.mtu.simulation.steppable;

import java.util.Iterator;

import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.CompoundBehavior;
import edu.mtu.simulation.CompoundInspector;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent monitors the simulation and stops it when the stopping
 * conditions are met. Currently the stopping conditions are as follows:
 * 1. Acetate is exhausted
 * 2. All hydroxyl radical pathways are exhausted
 */
@SuppressWarnings("serial")
public class Monitor implements Steppable {
	
	@Override
	public void step(SimState state) {
		CompoundInspector inspector = new CompoundInspector();
		
		// Update the decay odds for the appropriate compounds
		updateBehavior();
				
		// Check to see if acetone is exhausted
		if (inspector.getAcetoneCount() == 0) {
			state.finish();
		}
		
		// Check to see if Hydroxyl pathways have been exhausted
		if (inspector.getHydrogenPeroxideCount() == 0 && 
			inspector.getHydroxylRadicalCount() == 0 && 
			inspector.getPeroxyRadicalCount() == 0) {
			state.finish();
		}
	}
	
	/**
	 * Update the compound behavior based upon how many compounds were removed this timestep.
	 */
	private void updateBehavior() {
		CompoundBehavior behavior = ChemSim.getBehavior();
		
		// Get the iterator and return if there is nothing to do
		Iterator<String> iterator = behavior.getDecayingCompounds();
		if (iterator == null) {
			return;
		}
		
		// Update the odds for the next time step for each of the species
		while (iterator.hasNext()) {
			String formula = iterator.next();
			long decay = behavior.getDecayQuantity(formula);
			long quantity = CompoundInspector.countSpecies(formula); 
			double odds = (quantity != 0) ? decay / (double)quantity : 0;
			behavior.setDecayOdds(formula, odds);
		}
	}
}
