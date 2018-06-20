package edu.mtu.simulation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.primitives.Int3D;
import edu.mtu.primitives.Sparse3DLattice;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.decay.DecayFactory;
import edu.mtu.simulation.schedule.Schedule;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.CensusTracking;
import edu.mtu.simulation.tracking.Converter;
import edu.mtu.simulation.tracking.TrackEnties;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class ChemSim implements Simulation {
				
	private static final boolean CENSUS = false;
		
	// Divisor for time steps to report on
	private static final long REPORT = 100;
	
	// Scale the decay by the given time unit, 1 = sec, 60 = minute
	public static final int SCALING = 60;
	
	// The properties for the simulation
	private ModelProperities properties;
	
	// Singleton instance of the simulation and schedule
	private static ChemSim instance = new ChemSim();
	private Schedule schedule = new Schedule();
	
	// Entity count tracker for the simulation
	private CensusTracking census;
	private TrackEnties tracker;	
	
	/**
	 * Random number generator that is tied to the simulation. 
	 */
	public XoRoShiRo128PlusRandom random;
	
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

			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load(simulation.getReactionsFileName());
			
			// Load the experimental parameters for the model
			String fileName = SimulationProperties.getInstance().getChemicalsFileName();
			List<ChemicalDto> compounds = Parser.parseChemicals(fileName);

			// Initialize the tracker(s)
			fileName = simulation.getResultsFileName();
			tracker = new TrackEnties(fileName, simulation.getOverWriteResults());
			if (CENSUS) {
				System.out.println("WARNING: counducting census of molecules, model will run slow.");
				census = new CensusTracking("census.csv", simulation.getOverWriteResults());
			}
			
			// Initialize the model
			random = new XoRoShiRo128PlusRandom(seed);
			Reactor.initalize(compounds);
			printHeader();
			
			// Load the compounds and decay model
			initializeModel(compounds);
			DecayFactory.createDecayModel(properties);
			
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
		// Update the decay
		long hydrogenPeroxide = tracker.getCount("H2O2");
		double decay = 0;
		if (hydrogenPeroxide != 0) {
			long quantity = properties.getDecayModel().getDecayQuantity(count, "H2O2", hydrogenPeroxide);	
			decay = (double)quantity / hydrogenPeroxide;
		}
		properties.setHydrogenPeroxideDecay(decay);
			
		// Check to see if we can terminate
		for (String molecule : SimulationProperties.getInstance().getTerminationOn()) {
			if (tracker.getCount(molecule) == 0) {
				System.out.print(molecule + " is exausted, terminating...");
				schedule.stop();
			}
		}
		
		// Update the census if need be
		if (census != null) {
			census.count();
		}
		
		// Reset the tracker and note the step
		boolean flush = (count % REPORT == 0);
		tracker.reset(flush);
		if (flush) {
			System.out.println(LocalDateTime.now() + ": " + count + " of " + total);
		}
	}
	
	/**
	 * Complete the simulation.
	 */
	@Override
	public void finish(boolean terminated) {
		if (census != null) {
			census.complete();
			System.out.print("\nCensus results written to: census.csv");
		}		
		
		// Write the tracked molecule counts
		tracker.complete();
		String moleculear = SimulationProperties.getInstance().getResultsFileName();
		System.out.println("\nMolecule counts written to: " + moleculear);
		
		// Use the molecule counts to write out the molar counts
		String mols = SimulationProperties.getInstance().getMolarFileName();
		Converter.Convert(moleculear, mols, properties.getMoleculeToMol());
		System.out.println("Molar counts written to: " + mols);
		
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
	private void initializeModel(List<ChemicalDto> chemicals) throws IOException {
			
		// Scale the compouds and 
		double scaling = findIntitalCount(chemicals);
		
		// Calculate out the multiplier
		long total = 0;
		for (ChemicalDto entry : chemicals) {
			total += entry.count;
		}
		Reactor reactor = Reactor.getInstance();
		long multiplier = reactor.getMaximumMolecules() / total;
		
		// Add the chemicals to the model
		Int3D container = reactor.dimensions;
		for (ChemicalDto chemical : chemicals) {
			long count = chemical.count * multiplier;			
			System.out.println("Generating " + count + " molecules of " + chemical.formula);			
			for (int ndx = 0; ndx < count; ndx++) {
				int x = random.nextInt(container.x), y = random.nextInt(container.y), z = random.nextInt(container.z);
				int hash = Sparse3DLattice.hashCoordinates(x, y, z);
				Int3D location = new Int3D(x, y, z, hash);
				Molecule molecule = new Molecule(chemical.formula);
				reactor.insert(molecule, location);
				schedule.insert(molecule);
			}
			
			// Set the baseline quantity
			tracker.update(chemical.formula, count);
		}
		
		// Find the scaling factor to go from molecules back to mols
		scaling *= multiplier;
		properties.setMoleculeToMol(scaling);
		
		System.out.println("Molecule to mol scalar: " + scaling);
	}
	
	/**
	 * Find the proportions for the chemicals input, return the scaling applied
	 */
	private double findIntitalCount(List<ChemicalDto> input) {
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
		
		// Apply scaling to the adjusted molar values
		double scaling = Math.pow(10, Math.abs(exponent) + 1);		
		for (int ndx = 0; ndx < input.size(); ndx++) {
			input.get(ndx).count = (long)(input.get(ndx).mols * scaling); 
		}
		return scaling;
	} 

	/**
	 * Display basic system / JVM information.
	 */
	private void printHeader() {
		long size = Reactor.getInstance().getMoleculeSize();
		long maxMolecules = Reactor.getInstance().getMaximumMolecules();
		Int3D container = Reactor.getInstance().dimensions;
		System.out.println("\n" + LocalDateTime.now());		
		System.out.println("\nMax Memory:         " + Runtime.getRuntime().maxMemory() + "b");
		System.out.println("Molecule Size:      " + size + "b");
		System.out.println("Max Molecule Count: " + maxMolecules + " (" + size * maxMolecules + "b)");
		if (SimulationProperties.getInstance().getMoleculeLimit() != SimulationProperties.NO_LIMIT) {
			System.out.println("WARNING: Molecule count limited by configuration");
		}
		System.out.println("Reactor Dimensions (nm): " + container.x + ", " + container.x + ", " + container.x);
	}
}
