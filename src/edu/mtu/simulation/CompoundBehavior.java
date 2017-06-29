package edu.mtu.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class keeps track of how the compounds should behave as the model proceeds. 
 */
public class CompoundBehavior {
	
	private Map<String, Long> decayQuantity = new HashMap<String, Long>();
	private Map<String, Double> decayOdds = new HashMap<String, Double>();
	
	/**
	 * Returns an iterator with the decaying compounds or null if none are present.
	 */
	public Iterator<String> getDecayingCompounds() {
		if (decayQuantity.isEmpty()) {
			return null;
		}
		return decayQuantity.keySet().iterator();
	}
	
	/**
	 * Get the decay odds for the compound indicated.
	 */
	public double getDecayOdds(String formula) {
		if (!decayOdds.containsKey(formula)) {
			throw new IllegalArgumentException("The chemcial " + formula + " does not have decay odds present.");
		}
		return decayOdds.get(formula);
	}
	
	/**
	 * Get the decay quantity for the compound indicated.
	 */
	public long getDecayQuantity(String formula) {
		if (!decayQuantity.containsKey(formula)) {
			throw new IllegalArgumentException("The chemicla " + formula + " does not have a decay quantity present.");
		}
		return decayQuantity.get(formula);
	}

	/**
	 * Set the decay odds for the compound indicated.
	 */
	public void setDecayOdds(String formula, double odds) {
		decayOdds.put(formula, odds);
	}
	
	/**
	 * Set the decay quantity for the compound indicated.
	 */
	public void setDecayQuantity(String formula, long quantity) {
		decayQuantity.put(formula, quantity);
	}
}
