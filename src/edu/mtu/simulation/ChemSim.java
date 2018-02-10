package edu.mtu.simulation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

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
import sim.util.Int3D;

public class ChemSim implements Simulation {
				
	// The properties for the simulation, managed by MASON
	private ModelProperities properties;
	
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
		properties = new ModelProperities();
	}
		
	/**
	 * Setup and start the simulation
	 */
	public void initialize(long seed) {
		try {
			// Note the properties
			SimulationProperties simulation = SimulationProperties.getInstance();

			// Initialize the model
			random = new MersenneTwisterFast(seed);
			Reactor.getInstance().initalize();
			printHeader();
			
			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load(simulation.getReactionsFileName());
			
			// Load the compounds into the model
			initializeModel();		
			
			// Initialize the tracker
			String fileName = simulation.getResultsFileName();
			tracker = new TrackEnties(fileName, simulation.getOverWriteResults());	
			
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
		System.out.println("\nStarting simulation...");
		schedule.start(this, timeSteps);
	}
	
	/**
	 * Note that one time step has been completed.
	 */
	@Override
	public void step(int count, int total) {
		tracker.reset();
		if (count % 10 == 0) {
			System.out.println(count + " of " + total);
		}
	}
	
	/**
	 * Complete the simulation.
	 */
	@Override
	public void finish(boolean terminated) {
		if (tracker != null) {
			tracker.complete();
		}
		System.out.println("\nSimulation results written to: " + SimulationProperties.getInstance().getResultsFileName());
		System.out.println("\n" + LocalDateTime.now());
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
	public static ModelProperities getProperties() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance.properties;
	}
	
	/**
	 * Get the schedule that is currently running.
	 */
	public static Schedule getSchedule() {
		return instance.schedule;
	}
		
	/**
	 * Get the tracker that is currently running.
	 */
	public static TrackEnties getTracker() {
		return instance.tracker;
	}
	
	/**
	 * Initialize the model by loading the initial chemicals in the correct ratio.
	 */
	private void initializeModel() throws IOException {
		// Create the initial compounds in the model
		List<ChemicalDto> chemicals = Parser.parseChemicals(SimulationProperties.getInstance().getChemicalsFileName());
										
		// Find the scaling for the chemicals
		chemicals = findIntitalCount(chemicals);
		
		// Calculate out the multiplier
		long total = 0;
		for (ChemicalDto entry : chemicals) {
			total += entry.count;
		}
		Reactor reactor = Reactor.getInstance();
		long multiplier = reactor.getMaximumMolecules() / total;
		
		// Add the chemicals to the model
		Int3D container = reactor.getContainer();
		for (ChemicalDto chemical : chemicals) {
			System.out.println("Generating " + chemical.count * multiplier + " molecules of " + chemical.formula);
			for (int ndx = 0; ndx < chemical.count * multiplier; ndx++) {
				Int3D location = new Int3D(random.nextInt(container.x), random.nextInt(container.y), random.nextInt(container.z));
				Molecule molecule = new Molecule(chemical.formula);
				reactor.insert(molecule, location);
				schedule.insert(molecule);
			}
		}	
	}
	
	/**
	 * Find the proportions for the chemicals input.
	 */
	private List<ChemicalDto> findIntitalCount(List<ChemicalDto> input) {
		// Find the smallest entry
		double smallest = Double.MAX_VALUE;
		for (ChemicalDto entry : input) {
			if (entry.mols < smallest) {
				smallest = entry.mols;
			}
		}
		
		// Find the exponent to offset the value
		NumberFormat format = new DecimalFormat("0.#E0");		
		String value = format.format(smallest);
		int exponent = Integer.parseInt(value.substring(value.indexOf("E") + 1));
		
		// TODO Add support to scale negative or positive values
		exponent = Math.abs(exponent) + 1;
		for (int ndx = 0; ndx < input.size(); ndx++) {
			input.get(ndx).count = (long)(input.get(ndx).mols * Math.pow(10, exponent)); 
		}
								
		// Scale based upon the smallest entry
		return input;
	} 

	/**
	 * Display basic system / JVM information.
	 */
	private void printHeader() {
		long size = Reactor.getInstance().getMoleculeSize();
		long maxMolecules = Reactor.getInstance().getMaximumMolecules();
		Int3D container = Reactor.getInstance().getContainer();
		System.out.println("\n" + LocalDateTime.now());		
		System.out.println("\nMax Memory:         " + Runtime.getRuntime().maxMemory() + "b");
		System.out.println("Molecule Size:      " + size + "b");
		System.out.println("Max Molecule Count: " + maxMolecules + " (" + size * maxMolecules + "b)");
		if (SimulationProperties.getInstance().getMoleculeLimit() != SimulationProperties.NO_LIMIT) {
			System.out.println("WARNING: Molecule count limited by configuration");
		}
		System.out.println("Reactor Dimensions: " + container.x + ", " + container.x + ", " + container.x);
	}
	
	/**
	 * Main entry point for non-UI model.
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
		instance.start(500);
		System.exit(0);
	}
}
