package edu.mtu.simulation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.activity.InvalidActivityException;

import ec.util.MersenneTwisterFast;
import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Species;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.schedule.Simulation;
import edu.mtu.simulation.tracking.TrackEnties;

public class ChemSim implements Simulation {
				
	// The properties for the simulation, managed by MASON
	private ChemSimProperties properties;
	
	// Singleton instance of the simulation
	private static ChemSim instance = new ChemSim();
	
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
	 * Creates entities of of the given species in a uniformly distributed fashion.
	 */
	private void createEntities(Species species, long count) throws InvalidActivityException {
		
		// TODO Write this method
		throw new UnsupportedOperationException();
		
	}
			
	/**
	 * Initialize the model by loading the initial chemicals in the correct ratio.
	 */
	private void initializeModel() throws IOException {
		// Create the initial compounds in the model
		List<ChemicalDto> chemicals = Parser.parseChemicals(properties.getChemicalsFileName());
		
		// Hold on to a reference to the registry
		ReactionRegistry registry = ReactionRegistry.getInstance();
						
		// TODO Correct scaling
		
		// Scale the value to use for H2O2 decay
		double scale = 0.0;
		double decay = properties.getHydrogenPeroxideDecay() * 60;					// TODO Marker
		decay = scaleDecay(decay, scale, properties.getReactorVolume());		// scaled molecules/volume/sec
		properties.setHydrogenPeroxideDecay(decay);
		
		// Add each of the chemicals to the model, assume they are well mixed
		Reactor reactor = Reactor.getInstance();
		System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
		System.out.println("Max Molecule Count: " + reactor.getMaximumMolecules());
		
		for (ChemicalDto chemical : chemicals) {
			// Add the molecules to the model
			Species species = registry.getSpecies(chemical.formula);
			
			// TODO find the count
			long count = 0;
			createEntities(species, count);
		}		
	}
	
	/**
	 * Scale the decay given based upon the actual Avagadro's number and reactor size.
	 * 
	 * @param decay The hydrogen peroxide decay in mol/L/sec
	 * @param scaling The maximum value to use for scaling.
	 * @param volume The volume of the reactor in L.
	 * @return The new hydrogen peroxide decay in scaled molecules/volume/sec
	 */
	private double scaleDecay(double decay, double scaling, double volume) {
		BigDecimal avagadro = new BigDecimal("6.0221409e+23");
		
		// Calculate out what the decay is in terms of Avagadro's Number
		BigDecimal calculation = new BigDecimal(decay);
		calculation = calculation.multiply(avagadro);							// molecules/L/sec
		calculation = calculation.multiply(new BigDecimal(volume));				// molecules/volume/sec
		
		// Now scale that to the model's value, floor((scale * value) / Avagadro) 
		calculation = calculation.multiply(new BigDecimal(scaling));			// scale * value
		calculation = calculation.divide(avagadro);								// (scale * value) / Avagadro
		double result = Math.floor(calculation.doubleValue());					// floor((scale * value) / Avagadro)
		
		// Return the results
		return result;
	}
			
	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		
		// Initialize the simulation
		long seed = System.currentTimeMillis();
		ChemSim instance = ChemSim.getInstance();
		instance.initialize(seed);
				
		// Run the simulation and exit
		instance.start(100);
		System.exit(0);
	}
}
