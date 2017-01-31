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
	
	private SparseGrid3D compounds;
	
	/**
	 * Constructor.
	 */
	public ChemSim(long seed) {
		super(seed);
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
	
	public int getMoleculesPerMole() {
		return moleculesPerMole;
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createCompounds(Class compoundName, int quantity) throws Exception {
		Constructor<?> ctor = compoundName.getConstructor(Int3D.class);
		for (int ndx = 0; ndx < quantity; ndx++) {
			Int3D movementVector = new Int3D(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
			Compound compound = (Compound)ctor.newInstance(new Object[] { movementVector });
			schedule.scheduleRepeating(compound);
			compounds.setObjectLocation(compound, new Int3D(random.nextInt(GridWidth), random.nextInt(GridHeight), random.nextInt(GridLength)));
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
