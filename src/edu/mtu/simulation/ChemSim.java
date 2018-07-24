package edu.mtu.simulation;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.decay.DecayFactory;
import edu.mtu.simulation.schedule.Schedule;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.CensusTracking;
import edu.mtu.simulation.tracking.Converter;
import edu.mtu.simulation.tracking.TrackEnties;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class ChemSim implements Simulation {
				
	// TODO Come up with a better way of doing this
	public final static String VERSION = "0.4.177"; 
	
	// Scale the decay by the given time unit, 1 = sec, 60 = minute
	public static final int SCALING = 60;
	
	// Format the number in scientific notation, two significant digits
	private final static NumberFormat scientific = new DecimalFormat("0.##E0");
	
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
	private XoRoShiRo128PlusRandom random;
	
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
			
			// Set the odds, assume 1.0 if we have an error
			try {
				double odds = Parser.parseHydroxylPercentage(fileName);
				properties.setHydroxylPercentage(odds);
			} catch (IOException | IllegalArgumentException ex) {
				System.err.print("WARNING: error encountered loading hydroxyl retention odds, assuming 1.0");
				System.err.println(ex.getMessage());
			}

			// Initialize the tracker(s)
			fileName = simulation.getResultsFileName();
			tracker = new TrackEnties(fileName, simulation.getOverWriteResults());
			
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
		// Update the HO* decay
		long hydrogenPeroxide = tracker.getCount("H2O2");
		double decay = 0;
		if (hydrogenPeroxide != 0) {
			double quantity = properties.getDecayModel().getDecayQuantity(count, "H2O2", hydrogenPeroxide);	
			decay = (double)quantity / hydrogenPeroxide;
		}
		properties.setHydrogenPeroxideDecay(decay);
					
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
		
		// Check to see if we can terminate, but let the simulation warm up first
		if (count < 10) {
			return;
		}
		for (String molecule : SimulationProperties.getInstance().getTerminationOn()) {
			if (tracker.getCount(molecule) == 0) {
				System.out.println(molecule + " is exausted, terminating...");
				schedule.stop();
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
		return instance;
	}
	
	/**
	 * Get the properties that are associated with this simulation.
	 */
	public static ModelProperities getProperties() {
		return instance.properties;
	}
	
	/**
	 * Get the random number generator.
	 */
	public static XoRoShiRo128PlusRandom getRandom() {
		return instance.random;
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
		System.out.println("Molecule to mol scalar: " + scaling + "\n");
						
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
				
		// Now add all of the molecules to the schedule
		System.out.println("Adding molecules to the schedule...");
		Reactor reactor = Reactor.getInstance();
		int[] container = reactor.dimensions;
		for (Molecule molecule : moleclues) {
			int x = random.nextInt(container[0]), y = random.nextInt(container[1]), z = random.nextInt(container[2]);
			reactor.grid.setObjectLocation(molecule, new int[] { x, y, z });
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
		System.out.println("Time Step (sec): " + SimulationProperties.getInstance().getTimeStepLength());
		System.out.println("Hydroxyl Retention: " + properties.getHydroxylPercentage());
		int[] container = Reactor.getInstance().dimensions;
		System.out.println("Reactor Dimensions (nm): " + container[0] + ", " + container[1] + ", " + container[2]);
		
		// Print report of reactions
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss");
		String reactions = SimulationProperties.getInstance().getReactionsFileName();
		System.out.println("\nReactions: " + reactions + " [" + dateFormat.format(new File(reactions).lastModified()) + "]");
		System.out.println(report);
				
	}
}
