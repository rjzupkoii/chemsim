package edu.mtu.primitives;

import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

	private final static int ENTITY_MULTIPLIER = 2;
	private final static int PARTITION_MULTIPLIER = 3;
	
	// Parameters for sizing and resizing bags, this is a point where tuning can take place
	private final static int INITIAL_BAG_SIZE = 16;
	private final static int LARGE_BAG_RATIO = 4;
	private final static int MIN_BAG_SIZE = 32;
	private final static int REPLACEMENT_BAG_RATIO = 2;
	
	/* Note that for now we are using Int3D from MASON, the class may need to be upgraded though. */
	private Map<Object, LocationAndIndex> entityMap;
	
	// The lattice map partitions the lattice into n^3 sections which cuts down
	// on the amount of work that needs to be done when searching it
	private Lattice[][][] latticeMap;

	// The number of partitions along one direction in the lattice 
	private int partitions;
	
	// The size is the divisor to use to find the partition based upon the coordinates
	private static int partitionSize;
	
	// The size of the partitioned hash tables, used for hashing
	private static int allocation;
		
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
		partitionSize = 100;
		lattice.partitions = (int)(x / partitionSize) + 1;

		// Assume a uniform distribution of entities
		allocation = (int)(maxEntities / Math.pow(lattice.partitions, 3)) * PARTITION_MULTIPLIER; 
				
		// Allocate and return
		lattice.entityMap = new Object2ObjectOpenHashMap<Object, LocationAndIndex>(maxEntities * ENTITY_MULTIPLIER);
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
	 * Get the all of the objects that share the location of the given object.
	 * 
	 * @param object To base the location on.
	 * @return The bag of objects, or null if the original object was not found.
	 */
	public Bag getColocatedObjects(final Object object) {
		LocationAndIndex lai = entityMap.get(object);
		return (lai == null) ? null : lai.colocated;
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
	public Bag getObjectsAtLocation(final Int3D location) {
		// Calculate the location of the lattice section based upon the location.
		int ix = (int)(location.x / partitionSize);
		int iy = (int)(location.y / partitionSize);
		int iz = (int)(location.x / partitionSize);			
		
		// Retrieve the bag, if any, and return
		return latticeMap[ix][iy][iz].lattice.get(location);
	}
	
	/**
	 * Hash the x, y, z coordinates provided based upon the internal hash table allocation. 
	 */
	public static int hashCoordinates(int x, int y, int z) {
		// TODO Evaluate this for performance
		final int p1 = 73856093, p2 = 19349663, p3 = 83492791;
		return (x * p1 ^ y * p2 ^ z * p3) % allocation;
	}
	
	/**
	 * Remove the object if it exists. 
	 * 
	 * @param object to be removed.
	 * @return The location of the object, or null if it doesn't exist.
	 */
	public Int3D remove(final Object object) {
		
		// Start by removing the object finding out where it is located
		LocationAndIndex lai = entityMap.remove(object);
		if (lai == null) {
			return null;
		}
				
		// Remove from the the location lattice
		Bag bag = lai.colocated;
		bag.remove(object);
		
		// Clear empty bags
		int count = bag.numObjs;
		if (count == 0) {
			bag.clear();
		}

		// Shrink oversized bags
        if (count >= MIN_BAG_SIZE && count * LARGE_BAG_RATIO <= bag.objs.length) {
        	bag.shrink(count * REPLACEMENT_BAG_RATIO); 
		}
		
		// Return the location
		return lai.location;		
	}
			
	/**
	 * Add or update the location of the object in the lattice.
	 * 
	 * @param object to be added or updated.
	 * @param x coordinate of the object.
	 * @param y coordinate of the object.
	 * @param z coordinate of the object.
	 */
	public void setObjectLocation(final Object object, int x, int y, int z) {
		Int3D location = new Int3D(x, y, z, hashCoordinates(x, y, z));
		setObjectLocation(object, location);
	}
	
	/**
	 * Add or update the location of the object in the lattice.
	 * 
	 * @param object to be added or updated.
	 * @param location of the object in the lattice.
	 */
	public void setObjectLocation(final Object object, final Int3D location) {
		// Start by checking our conditions		
		assert (object != null);
		
		// Check to see if the object already exists
		LocationAndIndex lai = entityMap.get(object);
				
		// Create an empty bag
		Bag bag = null;
		
		if (lai == null) {
			// No location returned, must be a new object
			lai = new LocationAndIndex(location);
			entityMap.put(object, lai);
		} else {
			// Return if there is no update
			if (lai.location.equals(location)) {
				return;
			}
			
			// We have a location, so we are updating
			bag = lai.colocated;
			bag.remove(object);
			
			// Clear empty bags
			int count = bag.numObjs;
			if (count == 0) {
				bag.clear();
			}

			// Shrink oversized bags
            if (count >= MIN_BAG_SIZE && count * LARGE_BAG_RATIO <= bag.objs.length) {
            	bag.shrink(count * REPLACEMENT_BAG_RATIO); 
			}
			
			// Update our location
			lai = new LocationAndIndex(location);
		}
		
		// Update the bag in the lattice at the new location
		Map<Int3D, Bag> lattice = latticeMap[lai.ix][lai.iy][lai.iz].lattice;
		bag = lattice.get(location);
		if (bag == null) {
			bag = new Bag(INITIAL_BAG_SIZE);
			lattice.put(location, bag);
		} 
		bag.add(object);
		lai.colocated = bag;
	}
		
	/**
	 * Java does not approve of using generics in arrays, so wrap the hashmap to get around that.
	 */
	private static class Lattice {
		private Map<Int3D, Bag> lattice;
		
		public Lattice(int maxEntities) {
			lattice = new Object2ObjectOpenHashMap<Int3D, Bag>(maxEntities);
		}
	}
	
	/**
	 * Helper class that provides the location of the object in the lattice and
	 * the index of it in a bag.
	 */
	private static class LocationAndIndex {
		private Bag colocated;
		private Int3D location;
		private int ix, iy, iz;
		
		public LocationAndIndex(final Int3D location) {
			this.location = location;
			ix = (int)(location.x / partitionSize);
			iy = (int)(location.y / partitionSize);
			iz = (int)(location.x / partitionSize);	
		}
	}
}
