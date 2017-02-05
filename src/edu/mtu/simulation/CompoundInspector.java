package edu.mtu.simulation;

import edu.mtu.compound.Acetate;
import edu.mtu.compound.HydrogenPeroxide;
import edu.mtu.compound.radical.CarbonCenteredRadical;
import edu.mtu.compound.radical.Hydroxyl;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
	
	public int getAcetateCount() {
		return countClass(Acetate.class);
	}
	
	public int getHydrogenPeroxideCount() {
		return countClass(HydrogenPeroxide.class);
	}
	
	public int getHydroxylCount() {
		return countClass(Hydroxyl.class);
	}
	
	public int getPropyliumCount() {
		return countClass(CarbonCenteredRadical.class);
	}
	
	@SuppressWarnings("rawtypes")
	private int countClass(Class className) {
		int count = 0;
		
		try {
			for (Object object : ChemSim.getInstance().getCompounds().getAllObjects()) {
				count += (object.getClass() == className) ? 1 : 0;
			}
		} catch (NullPointerException ex) {
			// Ignore these errors since they likely mean the model is not running
		}
			
		return count;
	}
}
