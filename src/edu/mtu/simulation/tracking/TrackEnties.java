package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.ModelProperities;

/**
 * This steppable provides a means of tracking the count of chemical entities 
 * that exist in the model.
 */
public class TrackEnties {

	private BufferedCsvWriter writer; 
	private List<String> entities;
	private Map<String, Long> counts;
		
	/**
	 * Constructor, prepare the list of entities.
	 */
	public TrackEnties(String fileName, boolean overwrite) {				
		try {
			// Prepare the tracking file
			prepare();
			writer = new BufferedCsvWriter(fileName, overwrite);
			
			// Note some values
			ModelProperities properties = ChemSim.getProperties();
					
			// Note the experimental conditions
			writer.write("Reactor Volume");
			writer.write(properties.getReactorVolume());
			writer.newline();
			
			// Note formulaic properties
			writer.write("H2O2 Decay, mol/L*sec");
			writer.write(properties.getHydrogenPeroxideDecay());
			writer.newline();
			
			// Note the calculated values
			writer.write("H2O2 Decay, entities/L*sec");
			writer.write(properties.getHydrogenPeroxideDecay());
			writer.newline();
			
			// Write the names of the entities out
			writer.write(entities);
			writer.flush();
		} catch (IOException ex) {
			System.err.println(ex);
			System.err.println("Unable to create the tracking file at, " + fileName);
			System.exit(-1);
		}
	} 
	
	/**
	 * Finalize any work being done.
	 */
	public void complete() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			// Wrapping up, do nothing
		}
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
	private void prepare() {
		// Note the entities that may will appear in the model over the entire run
		entities = ReactionRegistry.getInstance().getEntityList();
		if (entities.size() == 0) {
			System.err.println("No entities were found.");
			System.exit(-1);
		}
		
		// Remove flag values that are not tracked from the list
		int index = entities.indexOf("UV");
		if (index != -1) {
			entities.remove(index);
		}
		Collections.sort(entities);
		
		// Prepare the counts
		counts = new HashMap<String, Long>();
		for (String entity : entities) {
			counts.put(entity, 0L);
		}
	}
	
	/**
	 * Write the contents to the CSV file and set the counts to zero.
	 */
	public void reset(boolean flush) {
		try {
			for (String entity : entities) {
				writer.write(counts.get(entity));
				counts.put(entity, 0L);
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
	public void update(String formula, int count) {
		long value = counts.get(formula);
		counts.put(formula, value + count);
	}
}
