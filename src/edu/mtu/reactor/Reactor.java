package edu.mtu.reactor;

import java.util.List;
import java.util.Set;

import edu.mtu.compound.Molecule;
import edu.mtu.parser.ChemicalDto;
import edu.mtu.primitives.Entity;
import edu.mtu.primitives.Sparse3DLattice;
import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.simulation.SimulationProperties;
import edu.mtu.util.FnvHash;
import net.sourceforge.sizeof.SizeOf;
import sim.util.Bag;

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
	
	private static Reactor instance;
	
	private int moleculeCount;
	private long moleculeSize;

	public final int[] dimensions;
	public Sparse3DLattice grid; 
	
	/**
	 * Constructor.
	 */
	private Reactor(int[] dimensions) { 
		this.dimensions = dimensions;
	}
	
	/**
	 * Get an instance of the reactor.
	 */
	public static Reactor getInstance() { 
		return instance;
	}
	
	/**
	 * Calculate the dimensions (assuming cubic) of the reactor based upon the list of compounds provided. 
	 * 
	 * @param compounds parsed out when the experimental inputs are loaded.
	 * @return The dimensions a long a single axis in nanometers (nm).
	 */
	public static int calculateSize(List<ChemicalDto> compounds, long molecules) {
		double mols = 0;
		for (ChemicalDto compound : compounds) {
			mols += compound.mols;
		}
		double result = Math.cbrt((double)molecules / (mols * AvogadrosNumber));	// m
		return (int)Math.ceil(result*Math.pow(10, 9));								// nm
	}
		
	public Molecule getFirst(String formula) {
		int hash = FnvHash.fnv1a32(formula);
		return (Molecule)grid.getFirstEntity(hash);
	}
	
	public int[] getLocation(Molecule molecule) {
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
		Set<Entity> objects = grid.getAllObjects();
		Molecule[] array = new Molecule[objects.size()];
		objects.toArray(array);
		return array;
	}
		
	/**
	 * Get the molecules at the same location as the given molecule.
	 */
	public Bag getMolecules(Molecule molecule) {
		return grid.getColocatedObjects(molecule);
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
	public static void initalize(List<ChemicalDto> compounds) {
		try {
			
			// Note the size and number of initial molecules
			long size = SizeOf.deepSizeOf(new Molecule("CH3COCH2OH", false)) * 3;
			int count = SimulationProperties.getInstance().getInitialMolecules();
			
			// Use the maximum molecule count to estimate a size for the reactor
			int dimension = calculateSize(compounds, count);
			
			// Create the reactor, set relevant values, and return
			int[] hashes = ReactionRegistry.getInstance().getEntityHashList();
			if (hashes == null) {
				throw new IllegalAccessError("Entity hash table is null.");
			}
			
			instance = new Reactor(new int[] { dimension, dimension, dimension });
			instance.grid = Sparse3DLattice.create3DLattice(count, hashes);
			instance.moleculeCount = count;
			instance.moleculeSize = size;
			
		} catch (IllegalArgumentException ex) {
			System.err.println("Fatal Error while initalizing the Reactor");
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Insert the given molecule at the given location.
	 */
	public void insert(Molecule molecule, int[] location) {
		grid.setObjectLocation(molecule, location);
	}
	
	/**
	 * Remove the molecule from the grid.
	 */
	public void remove(Molecule molecule) {
		grid.remove(molecule);
	}
}
