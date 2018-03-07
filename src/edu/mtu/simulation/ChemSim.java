package edu.mtu.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.schedule.Schedule;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.CensusTracking;
import edu.mtu.simulation.tracking.TrackEnties;
import edu.mtu.system.EchoStream;
import net.sourceforge.sizeof.SizeOf;
import sim.util.Int3D;

public class ChemSim implements Simulation {
				
	private static final boolean CENSUS = false;
	
	// Padding to add to the time steps to act as a buffer
	private static final long PADDING = 500;
	
	// Divisor for time steps to report on
	private static final long REPORT = 100;
	
	// Scale the decay by the given time unit, 1 = sec, 60 = minute
	private static final int SCALING = 60;
	
	// The properties for the simulation, managed by MASON
	private ModelProperities properties;
	
	// Singleton instance of the simulation
	private static ChemSim instance = new ChemSim();
	
	private Schedule schedule = new Schedule();
	
	// Entity count tracker for the simulation
	private CensusTracking census;
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

			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load(simulation.getReactionsFileName());
			
			// Load the experimental parameters for the model
			String fileName = SimulationProperties.getInstance().getChemicalsFileName();
			double rate = Parser.parseRate(fileName);
			double volume = Parser.parseVolume(fileName);
			List<ChemicalDto> compounds = Parser.parseChemicals(fileName);

			// Initialize the tracker(s)
			fileName = simulation.getResultsFileName();
			tracker = new TrackEnties(fileName, simulation.getOverWriteResults());
			if (CENSUS) {
				System.out.println("WARNING: counducting census of molecules, model will run slow.");
				census = new CensusTracking("census.csv", simulation.getOverWriteResults());
			}
			
			// Initialize the model
			random = new MersenneTwisterFast(seed);
			Reactor.getInstance().initalize(compounds);
			printHeader();
			
			// Load the compounds into the model
			initializeModel(compounds, rate, volume);		
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
	public void start(long timeSteps) {
		System.out.println("\nStarting simulation...");
		schedule.start(this, timeSteps);
	}
	
	/**
	 * Note that one time step has been completed.
	 */
	@Override
	public void step(long count, long total) {
		// Update the decay
		long hydrogenPeroxide = tracker.getCount("H2O2");
		if (hydrogenPeroxide != 0) {
			properties.setHydrogenPeroxideDecay((double)properties.getHydrogenPeroxideDecayQuantity() / hydrogenPeroxide);
		} else {
			properties.setHydrogenPeroxideDecay(0);
		}
		
		// Check to see if we can terminate
		if (hydrogenPeroxide == 0 && tracker.getCount("HO*") == 0) {
			System.out.println("Hydroxyl Radical source exhausted, terminating...");
			schedule.stop();
		}
		if (tracker.getCount("CH3COCH3") == 0) {
			System.out.println("Acetone source exhasusted, termianting...");
			schedule.stop();
		}		
		
		// Update the census if need be
		if (census != null) {
			census.count();
		}
		
		// Reset the tracker and note the step
		boolean flush = (count % REPORT == 0);
		tracker.reset(flush);
		if (flush) {
			System.out.println(count + " of " + total);
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
		tracker.complete();		
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
	private void initializeModel(List<ChemicalDto> chemicals, double rate, double volume) throws IOException {
										
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
			long count = chemical.count * multiplier;			
			System.out.println("Generating " + count + " molecules of " + chemical.formula);
			
			// TODO Is there a better place to do this?
			if (chemical.formula.equals("H2O2")) {
				// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
				// this means we need to determine the odds that any individual 
				// hydrogen peroxide agent will be removed each time step based upon
				// the new population which requires us knowing the initial decay
				int decay = (int)Math.ceil(Math.abs((count * rate * volume) / chemical.mols)) * SCALING;
				properties.setHydrogenPeroxideDecayQuantity(decay);
				
				// Since we know the decay rate we can calculate the running time
				long time = ((chemical.count * multiplier) / decay) + PADDING;
				properties.setTimeSteps(time);
			}
			
			for (int ndx = 0; ndx < count; ndx++) {
				Int3D location = new Int3D(random.nextInt(container.x), random.nextInt(container.y), random.nextInt(container.z));
				Molecule molecule = new Molecule(chemical.formula);
				reactor.insert(molecule, location);
				schedule.insert(molecule);
			}
			
			// Set the baseline quantity
			tracker.update(chemical.formula, count);
		}
		System.out.println("Calculated decay rate of " + properties.getHydrogenPeroxideDecayQuantity());
		System.out.println("Estimated running time of " + (properties.getTimeSteps() - PADDING) + " time steps, padded to " + properties.getTimeSteps());
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
		System.out.println("Reactor Dimensions (nm): " + container.x + ", " + container.x + ", " + container.x);
	}
	
	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {
				
		// Echo to the console file
		FileOutputStream out = new FileOutputStream("console.txt");
		EchoStream echo = new EchoStream(out);
		System.setOut(echo);
		
		// Configure SizeOf, note that the program MUST be invoked with -javaagent:lib/SizeOf.jar
		SizeOf.skipStaticField(true);
		SizeOf.setMinSizeToLog(10);
		
		// Initialize the simulation
		long seed = System.currentTimeMillis();
		ChemSim instance = ChemSim.getInstance();
		instance.initialize(seed);
				
		// Run the simulation and exit
		long timeSteps = ChemSim.getProperties().getTimeSteps();
		if (timeSteps != 0) {
			instance.start(timeSteps);
		} else {
			
		}
		System.exit(0);
	}
}
