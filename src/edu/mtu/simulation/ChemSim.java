package edu.mtu.simulation;

import java.lang.reflect.Constructor;

import edu.mtu.compound.Acetate;
import edu.mtu.compound.HydrogenPeroxide;
import sim.engine.SimState;
import sim.field.grid.SparseGrid3D;
import sim.util.Int3D;

@SuppressWarnings("serial")
public class ChemSim extends SimState {

	// The nature of the space we are working in
	public final static int GridWidth = 30;
	public final static int GridHeight = 30;
	public final static int GridLength = 30;
	
	// Initial settings for the model
	private int moleculesPerMole = 100; 
	private int acetateMoles = 1;
	private int hydrogenPeroxideMoles = 15;
	private double uvIntensity = 0.025;
	
	private SparseGrid3D compounds;
	
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
			createCompounds(Acetate.class, acetateMoles * moleculesPerMole);
			createCompounds(HydrogenPeroxide.class, hydrogenPeroxideMoles * moleculesPerMole);
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	public int getAcetateMoles() {
		return acetateMoles;
	}
	
	public SparseGrid3D getCompounds() {
		return compounds;
	}

	public Int3D getContainer() {
		return new Int3D(GridWidth, GridHeight, GridLength);
	}
	
	public int getHydrogenPeroxideMoles() {
		return hydrogenPeroxideMoles;
	}
	
	public static ChemSim getInstance() {
		// We expect to be constructed by MASON, so no instance can be created before then
		if (instance == null) {
			throw new IllegalStateException();
		}
		
		return instance;
	}
	
	public int getMoleculesPerMole() {
		return moleculesPerMole;
	}
	
	public double getUvIntensity() {
		return uvIntensity;
	}

	public void setAcetateMoles(int value) {
		acetateMoles = value;
	}
		
	public void setHydrogenPeroxideMoles(int value) {
		hydrogenPeroxideMoles = value;
	}
	
	public void setMoleculesPerMole(int value) {
		moleculesPerMole = value;
	}
	
	public void setUvIntensity(double uvIntensity) {
		this.uvIntensity = uvIntensity;
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
