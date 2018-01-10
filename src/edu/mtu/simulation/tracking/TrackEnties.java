package edu.mtu.simulation.tracking;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.ChemSimProperties;

/**
 * This steppable provides a means of tracking the count of chemical entities 
 * that exist in the model.
 */
public class TrackEnties {

	private BufferedCsvWriter writer; 
	private List<String> entities;
		
	/**
	 * Constructor, prepare the list of entities.
	 */
	public TrackEnties(String fileName, boolean overwrite) {
		entities = ReactionRegistry.getInstance().getEntityList();
		if (entities.size() == 0) {
			System.err.println("No entities were found.");
			System.exit(-1);
		}
		Collections.sort(entities);
				
		try {
			// Prepare the tracking file
			writer = new BufferedCsvWriter(fileName, overwrite);
			
			// Note some values
			ChemSimProperties properties = ChemSim.getProperties();
					
			// Note the experimental conditions
			writer.write("Reactor Volume");
			writer.write(properties.getReactorVolume());
			writer.newline();
			writer.write("Cells");
			writer.write(Math.pow(properties.getCellCount(), 3));
			writer.newline();
			
			// Note formulaic properties
			writer.write("H2O2 Decay, mol/L*sec");
			writer.write(properties.getHydrogenPeroxideDecay());
			writer.newline();
			writer.write("Hydroxyl Adjustment");
			writer.write(properties.getHydroxylAdjustment());
			writer.newline();
			writer.write("Acetone Adjustment");
			writer.write(properties.getAcetoneAdjustment());
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
			writer.close();
		} catch (IOException ex) {
			// Wrapping up, do nothing
		}
	}
}
