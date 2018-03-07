package edu.mtu.reactor;

import java.util.List;

import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.simulation.SimulationProperties;
import net.sourceforge.sizeof.SizeOf;
import sim.field.grid.SparseGrid3D;
import sim.util.Bag;
import sim.util.Int3D;

/**
 * The reactor is the container that the experiment takes place in. As a 
 * simplification, the container is assumed to be square.
 * 
 * Note that in the interest of performance, this code ignores the need
 * to check state. It assumes that methods will only be called when they
 * should be called.
 */
public class Reactor {
	
	public final static double AvogadrosNumber = 6.02214085774E23;
	public final static double MemoryOverhead = 0.9;
	
	private static Reactor instance = new Reactor();
	
	private long moleculeCount;
	private long moleculeSize;

	private Int3D container;
	private SparseGrid3D grid; 
	
	/**
	 * Constructor.
	 */
	private Reactor() { }
	
	/**
	 * Get an instance of the reactor.
	 */
	public static Reactor getInstance() { 
		return instance;
	}
	
	/**
	 * Calculate the dimensions (assuming cubic) of the reactor based upon the list of compounds provided. 
	 * @param compounds parsed out when the experimental inputs are loaded.
	 * @return The dimensions a long a single axis in nanometers (nm).
	 */
	public static int calculateSize(List<ChemicalDto> compounds, long molecules) {
		double count = 0;
		for (ChemicalDto compound : compounds) {
			count += compound.mols;
		}
		double result = Math.cbrt((Math.pow(10.0, 24.0) * (double)molecules) / (count * AvogadrosNumber));
		return (int)Math.ceil(result);
	}
	
	/**
	 * Get the dimensions of the reactor.
	 */
	public Int3D getContainer() {
		return container;
	}
	
	public Int3D getLocation(Molecule molecule) {
		return grid.getObjectLocation(molecule);
	}
	
	/**
	 * Get the maximum number of molecules that can be allocated.
	 */
	public long getMaximumMolecules() {
		return moleculeCount;
	}
	
	/**
	 * Returns all molecules present in the reactor.
	 */
	public Molecule[] getMolecules() {
		Bag bag = grid.getAllObjects();
		Molecule[] array = new Molecule[bag.numObjs];
		bag.toArray(array);
		return array;
	}
	
	/**
	 * Get the molecules at the given location in the reactor.
	 */
	public Molecule[] getMolecules(Int3D location) {
		Bag bag = grid.getObjectsAtLocation(location);
		Molecule[] array = new Molecule[bag.numObjs];
		bag.toArray(array);
		return array;
	}
	
	/**
	 * Return the estimated total size of a molecule, in bytes.
	 */
	public long getMoleculeSize() {
		return moleculeSize;
	}
			
	/**
	 * Initialize the reactor with the given dimensions.
	 * 
	 * @param compounds a list of compounds that are going to be fed into the reactor.
	 */
	public void initalize(List<ChemicalDto> compounds) {
		try {
			// Start by determining how much space we have to work with, note
			// that this is based upon free memory to account for program over
			// head that we have no control over
			long heapSize = Runtime.getRuntime().maxMemory();
			
			// Calculate out how many molecules we can create, note that the molecule 
			// will exist in the sparse matrix and the schedule as well
			moleculeSize = SizeOf.deepSizeOf(new Molecule("CH3COCH2OH")) * 3;
			moleculeCount = (long) ((heapSize * MemoryOverhead) / moleculeSize);
			
			// Check to see if a molecule limit was enforced
			if (SimulationProperties.getInstance().getMoleculeLimit() != SimulationProperties.NO_LIMIT) {
				moleculeCount = SimulationProperties.getInstance().getMoleculeLimit();
			}
			
			// Use the maximum molecule count to estimate a size for the reactor
			int dimension = calculateSize(compounds, moleculeCount);
			container = new Int3D(dimension, dimension, dimension);
			grid = new SparseGrid3D(dimension, dimension, dimension);
			
		} catch (IllegalArgumentException ex) {
			System.err.println("Fatal Error while initalizing the Reactor");
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Insert the given molecule at the given location.
	 */
	public void insert(Molecule molecule, Int3D location) {
		grid.setObjectLocation(molecule, location);
	}
	
	/**
	 * Remove the molecule from the grid.
	 */
	public void remove(Molecule molecule) {
		grid.remove(molecule);
	}
	
	/**
	 * Set the molecule location.
	 */
	public void setLocation(Molecule molecule, Int3D location) {
		grid.setObjectLocation(molecule, location);
	}
}
