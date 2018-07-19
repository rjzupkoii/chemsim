package edu.mtu.compound;

import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.ModelProperities;
import edu.mtu.simulation.SimulationProperties;

public class Acetone extends Molecule {

	public Acetone() {
		super("CH3COCH3");
	}
	
	@Override
	public void doAction(int step) {
		// Constrain how often acetone reacts
		if (ChemSim.getRandom().nextDouble() > updateAcetoneOdds(step)) {
			return;
		}
		
		// Attempt to react, then move
		if (react()) {
			dispose();
		} else if (step % 60 == 0) {
			move();
		}
	}
	
	private double updateAcetoneOdds(int step) {
				
		ModelProperities properties = ChemSim.getProperties();
		double duration = SimulationProperties.getInstance().getDuration();
				
		// Scale out the current concentration, mM/L
		long count = ChemSim.getTracker().getCount("CH3COCH3");
		double mols = ((count / properties.getMoleculeToMol()) * 1000) / 1.8;
		
		// Predict target concentration, mM/L
		double t = step / duration;
		double target = 1.33 * Math.exp(-7.65E-03 * t);
		
		// If we are above the target, the odds are zero
		if (mols < target) {
			return 0;
		}
		
		// Find the difference in the target concentration, scale to mol/reactor
		double diff = ((mols - target) / 1000) * 1.8;
		
		// Now scale mol/reactor to molecules/timestep
		double molecueles = diff * properties.getMoleculeToMol();
				
		// The odds is based upon the number being required to change the concentration vs. the number being created
		return (molecueles / count);	
	}
}
