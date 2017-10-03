package edu.mtu.simulation;

import edu.mtu.Reactor.Cell;
import edu.mtu.Reactor.Reactor;
import edu.mtu.compound.Species;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
	
	public long getAcetoneCount() {
		return countSpecies("CH3COCH3");
	}
	
	public long getHydrogenPeroxideCount() {
		return countSpecies("H2O2");
	}

	public long getHydroxylRadicalCount() {
		return countSpecies("HO*");
	}
	
	public long getPeroxyRadicalCount() {
		return countSpecies("HO2");
	}
	
	/**
	 * Count the entities of the given species.
	 * 
	 * @param formula The formula of the species to count.
	 * @return The count of species in the model.
	 */
	public static long countSpecies(String formula) {
		long count = 0;

		try {
			Species species = new Species(formula);
			for (Cell cell : Reactor.getInstance().getCells()) {
				count += cell.count(species);
			}
		} catch (NullPointerException ex) {
			// Ignore these errors since they likely mean the model is not
			// running
		}
			
		return count;
	}
}
