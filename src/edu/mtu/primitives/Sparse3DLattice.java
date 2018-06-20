package edu.mtu.primitives;

import java.util.Set;

import gnu.trove.map.hash.THashMap;
import sim.util.Bag;

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

	// TODO This is a point where tuning can take place
	// Size of the bags when we start
	private final static int INITIAL_BAG_SIZE = 16;
	
	/* Note that for now we are using Int3D from MASON, the class may need to be upgraded though. */
	private THashMap<Object, LocationAndIndex> entityMap;
	
	// The lattice map partitions the lattice into n^3 sections which cuts down
	// on the amount of work that needs to be done when searching it
	private Lattice[][][] latticeMap;

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
		lattice.partitions = (int)(x / lattice.partitionSize) + 1;

		// Assume a uniform distribution of entities
		int allocation = (int)(maxEntities / Math.pow(lattice.partitions, 3)); 
				
		// Allocate and return
		lattice.entityMap = new THashMap<Object, LocationAndIndex>(maxEntities);
		lattice.latticeMap = new Lattice[lattice.partitions][lattice.partitions][lattice.partitions];
		for (int ndx = 0; ndx < lattice.partitions; ndx++) {
			for (int ndy = 0; ndy < lattice.partitions; ndy++) {
				for (int ndz = 0; ndz < lattice.partitions; ndz++) {
					lattice.latticeMap[ndx][ndy][ndz] = new Lattice(allocation);
				}
			}
		}
		return lattice;
	}

	/**
	 * Get all of the objects in the lattice.
	 */
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
	
	/**
	 * Get all of the objects at the given location.
	 * 
	 * @param location To retrieve the objects from.
	 * @return The bag of objects or null.
	 */
	public Bag getObjectsAtLocation(Int3D location) {
		// Index to the correct HashMap
		Int3D index = latticeIndex(location);
		
		// Retrieve, check the bag, and return
		Bag bag = latticeMap[index.x][index.y][index.z].lattice.get(location);
		return (bag == null || bag.numObjs == 0) ? null : bag;
	}
	
	/**
	 * Remove the object if it exists. 
	 * 
	 * @param object to be removed.
	 * @return The location of the object, or null if it doesn't exist.
	 */
	public Int3D remove(Object object) {
		
		// Start by removing the object finding out where it is located
		LocationAndIndex lai = entityMap.remove(object);
		if (lai == null) {
			return null;
		}
				
		// Remove from the the location lattice
		Bag bag = latticeMap[lai.index.x][lai.index.y][lai.index.z].lattice.get(lai.location);
		bag.remove(object);
		
		// TODO Determine if bag resizing or removal should take place
		
		// Return the location
		return lai.location;		
	}
	
	/**
	 * Add or update the location of the object in the lattice.
	 * 
	 * @param object to be added or updated.
	 * @param location of the object in the lattice.
	 */
	public void setObjectLocation(Object object, Int3D location) {
		
		// Start by checking our conditions
		if (object == null) {
			throw new RuntimeException("Cannot add null to a the lattice.");
		}
		if (location == null) {
			throw new RuntimeException("The location in the lattice cannot be null.");
		}
		
		// Check to see if the object already exists
		LocationAndIndex lai = entityMap.get(object);
				
		if (lai == null) {
			// No location returned, must be a new object
			lai = new LocationAndIndex(location, latticeIndex(location));
			entityMap.put(object, lai);
		} else {
			// Return if there is no update
			if (lai.location.equals(location)) {
				return;
			}
			
			// We have a location, so we are updating
			// Start by removing the object from the old bag
			Bag bag = latticeMap[lai.index.x][lai.index.y][lai.index.z].lattice.get(lai.location);
			bag.remove(object);
			
			// Update our location
			lai.location = location;
			lai.index = latticeIndex(location);			
		}
		
		// Update the bag in the lattice
		Bag bag = latticeMap[lai.index.x][lai.index.y][lai.index.z].lattice.get(location);
		if (bag == null) {
			bag = new Bag(INITIAL_BAG_SIZE);
			latticeMap[lai.index.x][lai.index.y][lai.index.z].lattice.put(lai.location, bag);
		} 
		bag.add(object);
	}
	
	/**
	 * Calculate the location of the lattice section based upon the location.
	 *  
	 * It's good practice to keep this code in one spot since everything is 
	 * dependent upon it. In theory this should be in-lined by javac as well. 
	 */
	private final Int3D latticeIndex(Int3D location) {
		int x = (int)(location.x / partitionSize);
		int y = (int)(location.y / partitionSize);
		int z = (int)(location.z / partitionSize);
		
		return new Int3D(x, y, z, 0);
	}
	
	/**
	 * Java does not approve of using generics in arrays, so wrap the hashmap to get around that.
	 */
	private static class Lattice {
		private THashMap<Int3D, Bag> lattice;
		
		public Lattice(int maxEntities) {
			lattice = new THashMap<Int3D, Bag>(maxEntities);
		}
	}
	
	/**
	 * Helper class that provides the location of the object in the lattice and
	 * the index of it in a bag.
	 */
	private static class LocationAndIndex {
		private Int3D location;
		private Int3D index;
		
		public LocationAndIndex(final Int3D location, final Int3D index) {
			this.location = location;
			this.index = index;
		}
	}
}
