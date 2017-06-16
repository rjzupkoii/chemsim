package edu.mtu.simulation;

import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.compound.Species;
import sim.engine.SimState;
import sim.field.grid.SparseGrid3D;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class ChemSim extends SimState {

	// The nature of the space we are working in
	public final static int GridWidth = 30;
	public final static int GridHeight = 30;
	public final static int GridLength = 30;
	
	// The compounds that are registered in the simulation
	private SparseGrid3D compounds;
	
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
			// Add all of the compounds to the grid in a random fashion
			compounds = new SparseGrid3D(GridWidth, GridHeight, GridLength);

			// TODO make this a model parameter
			// Import the reactions into the model
			ReactionRegistry instance = ReactionRegistry.getInstance();
			instance.clear();
			instance.load("tests/import.csv");
						
			// TODO load this data from the import
			// Create the initial compounds in the model
			int hydrogenPeroxideCount = properties.getHydrogenPeroxideMoles() * properties.getMoleculesPerMole(); 
			createEntities("H2O2", hydrogenPeroxideCount, true);
			createEntities("CH3COCH3", properties.getAcetoneMoles() * properties.getMoleculesPerMole(), false);			
			
			// TODO Figure out how to do this in a generalized fashion
			// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
			// this means we need to determine the odds that any individual 
			// hydrogen peroxide agent will be removed each time step based upon
			// the new population which requires us knowing the initial decay
			behavior = new CompoundBehavior();
			behavior.setHydrogenPeroxideDecayQuantity(Math.round(hydrogenPeroxideCount * getProperties().getUvIntensity()));
			
			// HACK
			behavior.setHydrogenPeroxideDecay(behavior.getHydrogenPeroxideDecayQuantity() / (double)hydrogenPeroxideCount);
			
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
	}
		
	public SparseGrid3D getCompounds() {
		return compounds;
	}

	public Int3D getContainer() {
		return new Int3D(GridWidth, GridHeight, GridLength);
	}
	
	public static CompoundBehavior getBehavior() {
		// We expect the class to already be instantiated
		if (instance == null) {
			throw new IllegalStateException();
		}
		
		return instance.behavior;
	}
	
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
	 * Create the chemical entities and add them to the model.
	 */
	private void createEntities(String formula, int quantity, boolean photosensitive) {
		for (int ndx = 0; ndx < quantity; ndx++) {
			Int3D location = new Int3D(random.nextInt(GridWidth), random.nextInt(GridHeight), random.nextInt(GridLength));
			Species species = new Species(formula);
			species.setPhotosensitive(photosensitive);
			species.setStoppable(schedule.scheduleRepeating(species));
			compounds.setObjectLocation(species, location);
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
