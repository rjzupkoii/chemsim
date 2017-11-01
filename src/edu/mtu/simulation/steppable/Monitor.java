package edu.mtu.simulation.steppable;

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
						
		// Check to see if acetone is exhausted
		/*long count = inspector.getAcetoneCount(); 
		if (count == 0) {
			state.finish();
		}*/
				
		// Check to see if hydroxyl pathways have been exhausted
		/*if (inspector.getHydrogenPeroxideCount() == 0 &&	
			inspector.getPeroxyRadicalCount() == 0) {
			state.finish();
		}*/
	}
}
