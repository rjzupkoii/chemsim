package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mtu.reaction.ReactionRegistry;

/**
 * This abstract class wraps the common methods used when tracking molecules.
 */
public abstract class Tracker {
	protected BufferedCsvWriter writer; 
	protected List<String> entities;
	
	/**
	 * Constructor, prepare the list of entities.
	 */
	public Tracker(String fileName, boolean overwrite) {				
		try {
			// Prepare the tracking file
			prepare();
			writer = new BufferedCsvWriter(fileName, overwrite);
			
			// Note the start time
			writer.write(LocalDateTime.now().toString());
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
	
	protected void prepare() {
		// Note the entities that may will appear in the model over the entire run
		entities = new ArrayList<String>(ReactionRegistry.getInstance().getEntityList());
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
	}
}
