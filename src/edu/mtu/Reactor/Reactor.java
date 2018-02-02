package edu.mtu.Reactor;

import java.io.IOException;

import edu.mtu.compound.Molecule;
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
	
	private static Reactor instance = new Reactor();
	
	private long moleculeCount;

	private Int3D container;
	private SparseGrid3D grid; 
	
	/**
	 * Constructor.
	 */
	private Reactor() { 
		try {
			// Start by determining how much space we have to work with, note
			// that this is based upon free memory to account for program over
			// head that we have no control over
			System.gc();
			long heapSize = Runtime.getRuntime().freeMemory();
			
			// Calculate out how many molecules we can create
			Molecule reference = new Molecule("DEADBEEF");
			long size = SizeOf.iterativeSizeOf(reference);
			moleculeCount = (long) ((heapSize * 0.9) / size);
		} catch (IllegalArgumentException | IllegalAccessException | IOException ex) {
			System.err.println("Fatal Error while constructring Reactor");
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Get an instance of the reactor.
	 */
	public static Reactor getInstance() { 
		return instance;
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
	 * Get the molecules at the given location in the reactor.
	 */
	public Molecule[] getMolecules(Int3D location) {
		Bag bag = grid.getObjectsAtLocation(location);
		return (Molecule[])bag.toArray();
	}
			
	/**
	 * Initialize the reactor with the given dimensions.
	 */
	public void initalize(int width, int height, int length) {
		container = new Int3D(width, height, length);
		grid = new SparseGrid3D(width, height, length);
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
