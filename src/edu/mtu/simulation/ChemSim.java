package edu.mtu.simulation;

import java.io.IOException;
import java.util.List;

import javax.activity.InvalidActivityException;

import ec.util.MersenneTwisterFast;
import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.schedule.Schedule;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.TrackEnties;
import net.sourceforge.sizeof.SizeOf;

public class ChemSim implements Simulation {
				
	// The properties for the simulation, managed by MASON
	private ChemSimProperties properties;
	
	// Singleton instance of the simulation
	private static ChemSim instance = new ChemSim();
	
	private Schedule schedule = new Schedule();
	
	// Entity count tracker for the simulation
	private TrackEnties tracker;
	
	/**
	 * Random number generator that is tied to the simulation. 
	 */
	public MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	private ChemSim() {
		properties = new ChemSimProperties();
	}
		
	/**
	 * Setup and start the simulation
	 */
	public void initialize(long seed) {
		try {
			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load(properties.getReactionsFileName());
						
			// Initialize the model
			random = new MersenneTwisterFast(seed);
			initializeModel();
			
			// TODO Load the file name from someplace else
			tracker = new TrackEnties("results.csv", properties.getOverWriteResults());
//			tracker.step(this);
//			this.schedule.scheduleRepeating(tracker);
//			this.schedule.scheduleRepeating(new Monitor());			
			
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Start the simulation.
	 */
	@Override
	public void start(int timeSteps) {
		
	}
	
	/**
	 * Complete the simulation.
	 */
	@Override
	public void finish(boolean terminated) {
		if (tracker != null) {
			tracker.complete();
		}
	}
	
	/**
	 * Get a reference to the ChemSim singleton.
	 */
	public static ChemSim getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}		
		return instance;
	}
	
	/**
	 * Get the properties that are associated with this simulation.
	 */
	public static ChemSimProperties getProperties() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance.properties;
	}
	
	/**
	 * Get the schedule that is currently running.
	 */
	public static Schedule getSchedule() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance.schedule;
	}
	
	/**
	 * Creates entities of of the given species in a uniformly distributed fashion.
	 */
	private void createEntities(Molecule molecule, long count) throws InvalidActivityException {
		
		// TODO Write this method
		throw new UnsupportedOperationException();
		
	}
			
	/**
	 * Initialize the model by loading the initial chemicals in the correct ratio.
	 */
	private void initializeModel() throws IOException {
		// Create the initial compounds in the model
		List<ChemicalDto> chemicals = Parser.parseChemicals(properties.getChemicalsFileName());
				
		// Add each of the chemicals to the model, assume they are well mixed
		Reactor reactor = Reactor.getInstance();
		System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory() + "b");
		System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory() + "b");
		System.out.println("Max Molecule Count: " + reactor.getMaximumMolecules());
		
		for (ChemicalDto chemical : chemicals) {
			// Add the molecules to the model
			Molecule molecule = new Molecule(chemical.formula);
			
			// TODO find the count
			long count = 0;
			createEntities(molecule, count);
		}		
	}
				
	/**
	 * Main entry point for non-UI model.
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {
				
		// Configure SizeOf, note that the program MUST be invoked with -javaagent:lib/SizeOf.jar
		SizeOf.skipStaticField(true);
		SizeOf.setMinSizeToLog(10);
		
		// Initialize the simulation
		long seed = System.currentTimeMillis();
		ChemSim instance = ChemSim.getInstance();
		instance.initialize(seed);
				
		// Run the simulation and exit
		instance.start(100);
		System.exit(0);
	}
}
