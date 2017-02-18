package edu.mtu.simulation;

import java.lang.reflect.Constructor;

import edu.mtu.compound.Acetate;
import edu.mtu.compound.HydrogenPeroxide;
import edu.mtu.simulation.agent.Compound;
import edu.mtu.simulation.agent.Monitor;
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
			createCompounds(Acetate.class, properties.getAcetateMoles() * properties.getMoleculesPerMole());
			int hydrogenPeroxideCount = properties.getHydrogenPeroxideMoles() * properties.getMoleculesPerMole();
			createCompounds(HydrogenPeroxide.class, hydrogenPeroxideCount);
			
			// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
			// this means we need to determine the odds that any individual 
			// hydrogen peroxide agent will be removed each time step based upon
			// the new population which requires us knowing the initial decay
			getProperties().setHydrogenPeroxideDecayQuantity(Math.round(hydrogenPeroxideCount * getProperties().getUvIntensity()));
			
			// Add the monitor agent to the simulation
			schedule.scheduleRepeating(new Monitor(), compounds.size(), 1);
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
	 * Create the compound and add it to the schedule in the quantity given.
	 */
	@SuppressWarnings("rawtypes")
	private void createCompounds(Class compoundName, int quantity) throws Exception {
		for (int ndx = 0; ndx < quantity; ndx++) {
			createCompoundAt(compoundName, new Int3D(random.nextInt(GridWidth), random.nextInt(GridHeight), random.nextInt(GridLength)));
		}
	}
	
	/**
	 * Create the compound at the given location and add it to the schedule.
	 * In the process, make sure they are given access to the Stoppable object
	 * needed to remove themselves from the model.
	 * 
	 * @param compoundName The class name of the compound to be added.
	 * @param location The location in space (x, y, c) to create the compound at.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Compound createCompoundAt(Class compoundName, Int3D location) {
		Compound compound = null;
		
		try {
			Constructor<?> ctor = compoundName.getConstructor(Int3D.class);
			Int3D movementVector = new Int3D(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
			compound = (Compound)ctor.newInstance(new Object[] { movementVector });
			compound.setStoppable(schedule.scheduleRepeating(compound));
			compounds.setObjectLocation(compound, location);
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		} 
		
		// Return the compound, keep the compiler happy
		return compound;
		
	}

	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		doLoop(ChemSim.class, args);
		System.exit(0);
	}	
}
