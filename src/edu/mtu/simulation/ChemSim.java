package edu.mtu.simulation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.primitives.Int3D;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.decay.DecayFactory;
import edu.mtu.simulation.schedule.Schedule;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.CensusTracking;
import edu.mtu.simulation.tracking.Converter;
import edu.mtu.simulation.tracking.TrackEnties;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class ChemSim implements Simulation {
				
	// TDOO Come up with a better way of doing this
	public final static double VERSION = 0.1; 
	
	private static final boolean CENSUS = false;
		
	// Format the number in scientific notation, two significant digits
	private final static NumberFormat scientific = new DecimalFormat("0.##E0");
		
	// Scale the decay by the given time unit, 1 = sec, 60 = minute
	public static final int SCALING = 60;
		
	// Singleton instance of the simulation and schedule
	private static ChemSim instance = new ChemSim();
	private Schedule schedule = new Schedule();
	
	// The properties for the simulation
	private ModelProperities properties;
	private int reportInterval;
	private double sampleInterval;
	
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
			
			// Calculate the report interval, we only echo on the minute
			sampleInterval = (60 / simulation.getTimeStepLength());
			reportInterval = (int)sampleInterval * simulation.getReportInterval();

			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			String report = instance.load(simulation.getReactionsFileName());
			
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
			printHeader(report);
			
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
		// Note we call step since the scheduler doesn't call until t+1
		System.out.println("\n" + LocalDateTime.now() + ": Starting simulation...");
		step(0, timeSteps);
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
			double quantity = properties.getDecayModel().getDecayQuantity(count, "H2O2", hydrogenPeroxide);	
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
						
		// Sample the count and report if need be
		if (count % sampleInterval == 0) {
			boolean flush = (count % reportInterval == 0);
			tracker.reset(flush);
			if (flush) {
				System.out.println(LocalDateTime.now() + ": " + count + " of " + total);
			}
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
		
		// Calculate and note the scaling factor to from molecules back to mols 
		double scaling = findIntitalCount(chemicals);
		properties.setMoleculeToMol(scaling);
						
		// Start by generating all of the initial molecules
		int size = 0;
		for (ChemicalDto chemical : chemicals) {
			size += chemical.count;
		}
		int ndx = 0;
		Molecule[] moleclues = new Molecule[size];
		for (ChemicalDto chemical : chemicals) {
			System.out.println("Generating " + chemical.count + " molecules of " + chemical.formula);
			for (int count = 0; count < chemical.count; count++) {
				moleclues[ndx++] = new Molecule(chemical.formula);
			}
			tracker.update(chemical.formula, chemical.count);
		}
		
		// Use a Fisher–Yates shuffle them so we have a random distribution of activation in the schedule
		System.out.println("Shuffleing moleclues...");
		for (ndx = size - 1; ndx > 0; ndx--)
	    {
	      int index = random.nextInt(ndx + 1);
	      Molecule swap = moleclues[index];
	      moleclues[index] = moleclues[ndx];
	      moleclues[ndx] = swap;
	    }
		
		// Now add all of the molecules to the schedule
		System.out.println("Adding moleclues to the schedule...");
		Reactor reactor = Reactor.getInstance();
		Int3D container = reactor.dimensions;
		for (Molecule molecule : moleclues) {
			int x = random.nextInt(container.x), y = random.nextInt(container.y), z = random.nextInt(container.z);
			reactor.grid.setObjectLocation(molecule, x, y, z);
			schedule.insert(molecule);
			
		}
	}
		
	/**
	 * Find the proportions for the chemicals input, return the scaling applied.
	 */
	private double findIntitalCount(List<ChemicalDto> input) {
		// Find the smallest exponent based upon the natural log
		int smallest = Integer.MAX_VALUE;
		for (ChemicalDto entry : input) {
			int exp = (int)Math.log(entry.mols);
			smallest = Math.min(exp, smallest);
		}
		
		// Calculate the scaling, note that this is closely related to find the 
		// mantissa of the input value, but subtracting one from the exponent 
		// is the same as dividing by ten and allows the actual sum of the 
		// multipliers a bit more space to work in
		long total = 0;
		double scaling = Math.pow(10, Math.abs(smallest) - 1);
		for (ChemicalDto entry : input) {
			entry.count = (long)Math.ceil(entry.mols * scaling);
			total += entry.count;
		}
		
		// Calculate out the multiplier and apply it
		long multiplier = Reactor.getInstance().getMaximumMolecules() / total;
		for (ChemicalDto entry : input) {
			entry.count *= multiplier;
		}
		
		// Return the scalar to go from moleclues to mols
		return multiplier * scaling;
	} 

	/**
	 * Display ChemSim header along with simulation information.
	 */
	private void printHeader(String report) {
		
		// Application versioning information
		System.out.println("ChemSim, version " + VERSION + "\n");
		
		// System and molecule information
		long size = Reactor.getInstance().getMoleculeSize();
		long maxMolecules = Reactor.getInstance().getMaximumMolecules();
		if (SimulationProperties.getInstance().getMoleculeLimit() != SimulationProperties.NO_LIMIT) {
			System.out.println("WARNING: Molecule count limited by configuration");
		}
		System.out.println("Max Memory:         " + Runtime.getRuntime().maxMemory() + "b");
		System.out.println("Molecule Size:      " + size + "b");
		System.out.println("Staring Molecule Limit: " + scientific.format(maxMolecules) + " (" + size * maxMolecules + "b)\n");		
		
		// Print the reactor information
		Int3D container = Reactor.getInstance().dimensions;
		System.out.println("Reactor Dimensions (nm): " + container.x + ", " + container.x + ", " + container.x);
		System.out.println("Molecule to mol scalar: " + properties.getMoleculeToMol() + "\n");
		
		// Print report of reactions
		System.out.println("Reactions: \n" + report);
				
	}
}
