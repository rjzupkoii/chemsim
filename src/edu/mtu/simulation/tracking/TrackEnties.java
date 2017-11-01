package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.CompoundInspector;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This steppable provides a means of tracking the count of chemical entities 
 * that exist in the model.
 */
@SuppressWarnings("serial")
public class TrackEnties implements Steppable {

	private BufferedCsvWriter writer; 
	private List<String> entities;
		
	/**
	 * Constructor, prepare the list of entities.
	 */
	public TrackEnties(String fileName) {
		entities = ReactionRegistry.getInstance().getEntityList();
		if (entities.size() == 0) {
			System.err.println("No entities were found.");
			System.exit(-1);
		}
		Collections.sort(entities);
				
		try {
			// Prepare the tracking file
			writer = new BufferedCsvWriter(fileName, false);
			writer.write(entities);
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
			writer.close();
		} catch (IOException ex) {
			// Wrapping up, do nothing
		}
	}
	
	@Override
	public void step(SimState state) {
		try {
			long avogadroNumber = Reactor.getInstance().getAvogadroNumber();
			for (String entity : entities) {
				writer.write(CompoundInspector.countSpecies(entity) / (double)avogadroNumber);
			}
			writer.newline();
		} catch (IOException ex) {
			System.err.println("Unable to write to the tracking file at, " + writer.getFileName());
			System.exit(-1);
		}
	}
}
