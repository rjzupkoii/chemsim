package edu.mtu.simulation;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
		
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
