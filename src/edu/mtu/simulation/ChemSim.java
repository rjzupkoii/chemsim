package edu.mtu.simulation;

import java.io.IOException;
import java.util.List;

import edu.mtu.Reactor.Reactor;
import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Species;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.steppable.Monitor;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class ChemSim extends SimState {

	// The number of cells along one dimension
	public final static int Cells = 5;
	
	// TODO read this from a file, the volume of the container
	public final static double Volume = 1.8 * 1000;
			
	// The properties for the simulation, managed by MASON
	private ChemSimProperties properties;
				
	// Singleton instance of the simulation
	private static ChemSim instance;
	
	/**
	 * Constructor.
	 */
	public ChemSim(long seed) {
		super(seed);
		
		// This actually breaks the standard pattern for a singleton, but we only 
		// expect MASON to start one instance of the simulation. This also gives 
		// us access to the simulation state without having to pass it around.
		if (instance != null) {
			throw new IllegalStateException();
		}
		
		// Note the this object and the properties
		properties = new ChemSimProperties();
		instance = this;
	}
		
	/**
	 * Setup and start the simulation
	 */
	@Override
	public void start() {
		super.start();
		
		try {
			// Clear the container of molecules
			Reactor.getInstance().createCells(Cells, Volume, this);
			
			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load(properties.getReactionsFileName());
						
			// Initialize the model
			initializeModel();
			this.schedule.scheduleRepeating(new Monitor());			
			
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Get a reference to the ChemSim singleton.
	 */
	public static ChemSim getInstance() {
		// We expect to be constructed by MASON, so no instance can be created before then
		if (instance == null) {
			throw new IllegalStateException();
		}
		
		return instance;
	}
	
	/**
	 * Get the properties that are associated with this simulation.
	 */
	public static ChemSimProperties getProperties() {
		// We expect the class to already be instantiated
		if (instance == null) {
			throw new IllegalStateException();
		}
		
		return instance.properties;
	}
			
	/**
	 * Initialize the model by loading the initial chemicals in the correct ratio.
	 */
	private void initializeModel() throws IOException {
		// Create the initial compounds in the model
		List<ChemicalDto> chemicals = Parser.parseChemicals(properties.getChemicalsFileName());
		
		// Hold on to a reference to the registry
		ReactionRegistry registry = ReactionRegistry.getInstance();
						
		// Add each of the chemicals to the model, assume they are well mixed
		Reactor reactor = Reactor.getInstance();
		for (ChemicalDto chemical : chemicals) {
			// Add the molecules to the model
			Species species = registry.getSpecies(chemical.formula);
			int quantity = (int)(chemical.mols * properties.getMoleculesPerMole());
			reactor.createEntities(species, quantity);
		}		
	}
	
		
	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		doLoop(ChemSim.class, args);
		System.exit(0);
	}	
}
