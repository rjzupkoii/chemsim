package edu.mtu.primitives;

import java.util.Set;

import edu.mtu.compound.Molecule;
import gnu.trove.map.hash.THashMap;
import sim.util.Bag;
import sim.util.Int3D;

/**
 * A sparse volume consists of entities that exist in an integer lattice. 
 * Entities are assumed to only occupy one point in the lattice; however,
 * multiple entities can occupy the same point in space. The volume is 
 * partitioned to optimize distance based searches by avoiding scanning
 * the entire entity list. 
 * 
 * In order to ensure that objects are as performant as possible the volume
 * needs to be initialized with what the expected peak usage will be. This 
 * means that there may be a lot of empty space in it, but overall the 
 * execution should be faster since we aren't reallocating the space. 
 * 
 * This approach borrows heavily from the SparseGrid3D class developed by 
 * Sean Luke (GMU) for MASON. 
 */
public class Sparse3DLattice {

	/* Note that for now we are using Int3D from MASON, the class may need to be upgraded though. */
	private THashMap<Object, LocationAndIndex> entityMap;
	
	// The lattice map partitions the lattice into n^3 sections which cuts down
	// on the amount of work that needs to be done when searching it
	private THashMap<LocationAndIndex, Bag>[][][] latticeMap;

	// The number of partitions along one direction in the lattice 
	private int partitions;
	
	// The size is the divisor to use to find the partition based upon the coordinates
	private int partitionSize;
	
	
	/**
	 * Private constructor.
	 */
	private Sparse3DLattice() {	}
	
	/**
	 * Create a sparse 3d lattice structure for use.
	 * 
	 * @param maxEntities The maximum number of entities the lattice is expected to contain.
	 * @param dimension The size of the lattice along all three dimensions.
	 * @return The initialized lattice.
	 */
	public static Sparse3DLattice create3DLattice(int maxEntities, int dimension) {
		return create3DLattice(maxEntities, dimension, dimension, dimension);
	}
	
	/**
	 * Create a new sparse 3d lattice structure for use.
	 * 
	 * @param maxEntities The maximum number of entities the lattice is expected to contain.
	 * @param x The size of the lattice along the x dimension. 
	 * @param y The size of the lattice along the y dimension.
	 * @param y The size of the lattice along the z dimension.
	 * @return The initialized lattice.
	 */
	public static Sparse3DLattice create3DLattice(int maxEntities, int x, int y, int z) {
		Sparse3DLattice lattice = new Sparse3DLattice();
		
		// TODO How this is done needs to be refined
		// Determine the partition size based upon the dimensions
		lattice.partitionSize = 100;
		lattice.partitions = (int)(x / lattice.partitionSize);

		// Assume a uniform distribution of entities
		int allocation = (int)(maxEntities / Math.pow(lattice.partitions, 3)); 
		
		// Allocate and return
		lattice.entityMap = new THashMap<Object, LocationAndIndex>(maxEntities);
		lattice.latticeMap[lattice.partitions][lattice.partitions][lattice.partitions] = new THashMap<LocationAndIndex, Bag>(allocation);
		return lattice;
	}

	public Set<Object> getAllObjects() {
		return entityMap.keySet();
	}
	
	/**
	 * Get the location of the given object.
	 * 
	 * @param object To retrieve the location of.
	 * @return The location as Int3D or null if it does not exist.e
	 */
	public Int3D getObjectLocation(final Object object) {
		LocationAndIndex lai = entityMap.get(object);
		return (lai == null) ? null : lai.location;
	}
	
	public Bag getObjectsAtLocation(Int3D location) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void remove(Molecule molecule) {
		// TODO Auto-generated method stub
		
	}
	
	public void setObjectLocation(Molecule molecule, Int3D location) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Helper class that provides the location of the object in the lattice and
	 * the index of it in a bag.
	 */
	private static class LocationAndIndex {
		private Int3D location;
		private int index;
		
		public LocationAndIndex(final Int3D location, final int index) {
			this.location = location;
			this.index = index;
		}
		
		public Int3D getLocation() {
			return location;
		}
		
		public int getIndex() {
			return index;
		}
	}
}
