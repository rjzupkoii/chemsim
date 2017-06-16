package edu.mtu.simulation;

import edu.mtu.compound.Species;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
	
	public int getAcetoneCount() {
		return countSpecies("CH3COCH3");
	}
	
	public int getHydrogenPeroxideCount() {
		return countSpecies("H2O2");
	}

	public int getHydroxylRadicalCount() {
		return countSpecies("HO*");
	}
	
	public int getPeroxyRadicalCount() {
		return countSpecies("HO2");
	}
	
	/**
	 * Count the entities of the given species.
	 * 
	 * @param formula The formula of the species to count.
	 * @return The count of species in the model.
	 */
	public static int countSpecies(String formula) {
		int count = 0;

		try {
			for (Object object : ChemSim.getInstance().getCompounds().getAllObjects()) {
				count += (((Species)object).getFormula().equals(formula)) ? 1 : 0;
			}
		} catch (NullPointerException ex) {
			// Ignore these errors since they likely mean the model is not
			// running
		}
			
		return count;
	}
}
