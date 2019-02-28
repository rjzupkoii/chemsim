package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides counts the entities that are in the model based upon the 
 * count at model initialization and when they are added to the model.
 */
public class TrackEnties extends Tracker {
	private Map<String, Long> counts;
	
	/**
	 * Constructor, prepare the list of entities.
	 */
	public TrackEnties(String fileName, boolean overwrite) {
		super(fileName, overwrite);
	}
	
	/**
	 * Get the count of molecules for the given formula.
	 */
	public long getCount(String formula) {
		if (counts.containsKey(formula)) {
			return counts.get(formula);
		}
		return 0;		
	}
	
	/**
	 * Prepare to start tracking entities.
	 */
	@Override
	protected void prepare() {
		super.prepare();
				
		// Prepare the counts
		counts = new HashMap<String, Long>();
		for (String entity : entities) {
			counts.put(entity, 0L);
		}
	}
	
	/**
	 * Write the contents to the CSV file and set the counts to zero.
	 */
	public void reset(boolean flush, double timeStep) {
		try {
			writer.write(timeStep);
			for (String entity : entities) {
				writer.write(counts.get(entity));
			}
			writer.newline();
			if (flush) {
				writer.flush();
			}
		} catch (IOException ex) {
			// Since we don't expect this to happen and don't have a means of recovering
			// treat this as a fatal error
			System.err.println("IOException occured while resetting the entity counts");
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Update the total for the given entity by the given count. 
	 */
	public void update(String formula, long count) {
		if (counts.containsKey(formula)) {
			long value = counts.get(formula);
			counts.put(formula, value + count);
		}
	}
	
	/**
	 * Zeros the count of the given entity.
	 */
	public void zero(String formula) {
		if (counts.containsKey(formula)) {
			counts.put(formula, 0l);
		}
	}
}
