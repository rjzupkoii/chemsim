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
	private int report;
	
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
			report = simulation.getReportInterval();

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
		System.out.println("\n" + LocalDateTime.now() + ": Starting simulation...");
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
		boolean flush = (count % report == 0);
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
		
		// Do all of the up-front calculations
		calcluateParameters(chemicals);
				
		// Add the chemicals to the model
		Reactor reactor = Reactor.getInstance();
		Int3D container = reactor.dimensions;
		for (ChemicalDto chemical : chemicals) {
	
			System.out.println("Generating " + chemical.count + " molecules of " + chemical.formula);			
			for (int ndx = 0; ndx < chemical.count; ndx++) {
				int x = random.nextInt(container.x), y = random.nextInt(container.y), z = random.nextInt(container.z);
				Molecule molecule = new Molecule(chemical.formula);
				reactor.grid.setObjectLocation(molecule, x, y, z);
				schedule.insert(molecule);
			}
			
			// Set the baseline quantity
			tracker.update(chemical.formula, chemical.count);
		}
	}
	
	private void calcluateParameters(List<ChemicalDto> input) {
		final double k = 1.00E+08;
		final double k_diff = 1.10E+10;
		
		// Starting by calculating out our constants, k_chem and r which is based on Pogson et al., 2006
		double k_chem = (k * k_diff) / (k + k_diff);
		double delta_t = properties.getTimeStepLength();
		double r = Math.cbrt((3 * k_chem * delta_t) / (4 * Math.PI * Math.pow(10, 3) * Reactor.AvogadrosNumber));	// meters
		int r_nm = (int)(r * 1E+9);
		properties.setInteractionRadius(r_nm);
		
		// Calculate and note the scaling factor to from molecules back to mols 
		double scaling = findIntitalCount(input);
		properties.setMoleculeToMol(scaling);
		
		// Print all of the parameters to the console
		Int3D container = Reactor.getInstance().dimensions;
		System.out.println("k_chem: " + scientific.format(k_chem) + "\t\tΔt (sec): " + delta_t);
		System.out.println("Interaction radius (nm): " + r_nm );	
		System.out.println("Reactor Dimensions (nm): " + container.x + ", " + container.x + ", " + container.x);
		System.out.println("Molecule to mol scalar: " + scaling + "\n");
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
	 * Display basic system / JVM information.
	 */
	private void printHeader() {
		long size = Reactor.getInstance().getMoleculeSize();
		long maxMolecules = Reactor.getInstance().getMaximumMolecules();
		System.out.println("\n" + LocalDateTime.now());		
		if (SimulationProperties.getInstance().getMoleculeLimit() != SimulationProperties.NO_LIMIT) {
			System.out.println("WARNING: Molecule count limited by configuration");
		}
		System.out.println("Max Memory:         " + Runtime.getRuntime().maxMemory() + "b");
		System.out.println("Molecule Size:      " + size + "b");
		System.out.println("Staring Molecule Limit: " + scientific.format(maxMolecules) + " (" + size * maxMolecules + "b)\n");		
	}
}
