package edu.mtu.simulation.agent;

import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.ChemSimProperties;
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
		
		// Determine how much hydrogen peroxide should decay on the next time step
		CompoundBehavior behavior = ChemSim.getBehavior();
		if (inspector.getHydrogenPeroxideCount() != 0) {
			behavior.setHydrogenPeroxideDecay(behavior.getHydrogenPeroxideDecayQuantity() / (double)inspector.getHydrogenPeroxideCount());
		} else {
			behavior.setHydrogenPeroxideDecay(0);
		}
				
		// Check to see if acetone is exhausted
		if (inspector.getAcetoneCount() == 0) {
			state.finish();
		}
		
		// Check to see if Hydroxyl pathways have been exhausted
		if (inspector.getHydrogenPeroxideCount() == 0 && inspector.getHydroxylRadicalCount() == 0 &&
			inspector.getPeroxyRadicalCount() == 0 &&
			inspector.getCarbonCenteredRadicalCount() == 0) {
			state.finish();
		}
	}

}
