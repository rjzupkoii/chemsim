package edu.mtu.simulation;

import java.io.IOException;
import java.util.List;

import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Species;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.steppable.Monitor;
import sim.engine.SimState;
import sim.field.grid.SparseGrid3D;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class ChemSim extends SimState {

	// The nature of the space we are working in
	public final static int GridWidth = 30;
	public final static int GridHeight = 30;
	public final static int GridLength = 30;
	
	// The molecules that are registered in the simulation
	private SparseGrid3D molecules;
	
	// The properties for the simulation, managed by MASON
	private ChemSimProperties properties;
	
	// The behavior of the compounds in this run of the simulation
	private CompoundBehavior behavior;
			
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
			molecules = new SparseGrid3D(GridWidth, GridHeight, GridLength);
			
			// Clear any behavior model that currently exists
			behavior = new CompoundBehavior();

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
	 * Get the compounds that are present in the model.
	 */
	public SparseGrid3D getMolecules() {
		return molecules;
	}

	/**
	 * Get the container that holds the model's compounds.
	 * @return
	 */
	public Int3D getContainer() {
		return new Int3D(GridWidth, GridHeight, GridLength);
	}
	
	/**
	 * Get a reference to the CompoundBehavior singleton that manages decay rates.
	 */
	public static CompoundBehavior getBehavior() {
		// We expect the class to already be instantiated
		if (instance == null) {
			throw new IllegalStateException();
		}
		
		return instance.behavior;
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
		ReactionRegistry instance = ReactionRegistry.getInstance();
				
		// Add each of the chemicals to the model, assume they are well mixed
		for (ChemicalDto chemical : chemicals) {
			// Add the molecules to the model
			boolean photosensitive = instance.getPhotolysisReaction(chemical.formula) != null;
			int quantity = (int)(chemical.mols * properties.getMoleculesPerMole());
			createEntities(chemical.formula, quantity, photosensitive);
			
			// Calculate it's linear decay rate, f(x) = C - r * t
			// 
			// Note that this means we need to determine the odds that any individual agent will be 
			// removed each time step based upon the new population which requires us knowing the 
			// initial decay
			long decay = Math.round(quantity * getProperties().getUvIntensity());
			double odds = quantity / (double)decay;
			behavior.setDecayQuantity(chemical.formula, decay);
			behavior.setDecayOdds(chemical.formula, odds);
		}		
	}
	
	/**
	 * Create the chemical entities and add them to the model.
	 */
	private void createEntities(String formula, int quantity, boolean photosensitive) {
		for (int ndx = 0; ndx < quantity; ndx++) {
			Int3D location = new Int3D(random.nextInt(GridWidth), random.nextInt(GridHeight), random.nextInt(GridLength));
			Species species = new Species(formula);
			species.setPhotosensitive(photosensitive);
			species.setStoppable(schedule.scheduleRepeating(species));
			molecules.setObjectLocation(species, location);
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
