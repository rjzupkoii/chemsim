package edu.mtu.primitives;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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

	// Multiplier for setting the initial hash map size
	private final static int ENTITY_MULTIPLIER = 2;
	
	// Parameters for sizing and resizing bags, this is a point where tuning can take place
	private final static int INITIAL_BAG_SIZE = 8;
	private final static int LARGE_BAG_RATIO = 4;
	private final static int MIN_BAG_SIZE = 16;
	private final static int REPLACEMENT_BAG_RATIO = 2;

	// This map allows us to find where the entity is and colocated entities in O(c)
	private Map<Entity, LocationAndIndex> entityMap;

	// This map allows us to search for other entities by probing the space, since we are hashing
	// the coordinates we really only need that hash value to find entities
	private Map<Integer, Bag> latticeMap;
	
	// This map allows us to search the lattice for bags containing 
	private Map<Integer, ArrayDeque<Entity>> tagMap;
	
	// The size of the partitioned hash tables, used for hashing
	private int allocation;
		
	/**
	 * Private constructor.
	 */
	private Sparse3DLattice() {	}
		
	/**
	 * Create a new sparse 3d lattice structure for use.
	 * 
	 * @param maxEntities The maximum number of entities the lattice is expected to contain.
	 * @return The initialized lattice.
	 */
	public static Sparse3DLattice create3DLattice(int maxEntities, int[] tags) {
		Sparse3DLattice lattice = new Sparse3DLattice();
		
		// Assume a uniform distribution of entities
		lattice.allocation = maxEntities * ENTITY_MULTIPLIER;
				
		// Allocate the entity map, note that while we know exactly how many tags we
		// can expect to see, we need to be pessimistic about the allocation of the 
		// tagged entity maps
		lattice.entityMap = new Object2ObjectOpenHashMap<Entity, LocationAndIndex>(lattice.allocation);
		lattice.latticeMap = new Int2ObjectOpenHashMap<Bag>(lattice.allocation);
		lattice.tagMap = new Int2ObjectOpenHashMap<ArrayDeque<Entity>>(tags.length);
		for (int key : tags) {
			if (lattice.tagMap.containsKey(key)) {
				throw new IllegalAccessError("Key collision {hash = " + key + "} while allocating the tagMap.");
			}
			lattice.tagMap.put(key, new ArrayDeque<Entity>());
		}				
		return lattice;
	}
		
	/**
	 * Find the first entity with the given tag in the radius from the given entity.
	 * This method starts by first ensuring any of the given entity type exist followed
	 * by then checking for entities colocated with it. At that point it will then
	 * scan all of the entities with the same type to see if one is found within
	 * the given search radius.
	 * 
	 * @param entity to base the search on.
	 * @param tag to search for.
	 * @param radius defining the sphere.
	 * @return The first entity with a matching tag in the sphere, or null.
	 */
	public Entity findFirstByTag(final Entity entity, final Integer tag, final int radius) {
				
		// Start by peeking to see if there are any entities with the given tag
		if (tagMap.get(tag).isEmpty()) {
			return null;
		}
		
		// Get our location and check this bag		
		LocationAndIndex lai = entityMap.get(entity);
		int size = lai.colocated.numObjs;
		for (int ndx = 0; ndx < size; ndx++) {
			Entity checking = (Entity)lai.colocated.objs[ndx];
			if (checking.equals(entity)) {
				continue;
			}
			if (checking.getEntityTypeTag() == tag) {
				return checking;
			}
		}
		
		// Return if the radius is zero, this indicates the colocated entities only
		if (radius == 0) {
			return null;
		}
		
		// Calculate how big the search space is for a geometric search
		// and chose our approach accordingly
		int points = (int)((4 / 3) * Math.PI * Math.pow(radius, 3));
		if (tagMap.get(tag).size() > points) {
			return distanceBasedSearch(entity, tag, radius, lai.location[0], lai.location[1], lai.location[2]);
		}
		return tagBasedSearch(entity, tag, radius, lai.location[0], lai.location[1], lai.location[2]);
	}
	
	/**
	 * Search for an entity with the given tag, using the tags hash. 
	 */
	protected Entity tagBasedSearch(final Entity entity, final Integer tag, final int radius, final int x1, final int y1, final int z1) {
		// Get the last entity so when know when to stop
		ArrayDeque<Entity> entities = tagMap.get(tag);
		Entity last = entities.peekLast();
		
		// Scan all entities of the given type, since we are using the queue
		// we need to be aware that it may contain stale entities in it. We
		// will know if we are looking at one because it will not be present
		// in the entity map any more.
		Entity current = null;
		while (!last.equals(current)) {
			// Get an entity to check
			current = entities.pop();
			
			// Get it's location, if the location is null then do nothing
			LocationAndIndex lai = entityMap.get(current);
			if (lai != null) {
				// Make sure valid entities are restored
				entities.add(current);
				
				// Press on if we are looking at the parent entity
				if (entity.equals(current)) {
					continue;
				}
				
				// Calculate the Euclidean distance, d = sqrt((x1 - x2)^2 + (y1 - y2)^2 + (z1 - z2)^2)
				int x = x1 - lai.location[0];
				int y = y1 - lai.location[1];
				int z = z1 - lai.location[2];
				double d = Math.sqrt(x*x + y*y + z*z);
				
				// Check and return if we are good
				if (d <= radius) {
					return current;
				}
			}
		}
		return null;
	}
	
	/**
	 * Search for an entity with the given tag, based upon the geometry of the system. 
	 * 
	 * Overall this code is O(n^3) where n is the number of points in the integer sphere,
	 * but in practice, it may return faster if there is a match close to the origin.
	 */
	protected Entity distanceBasedSearch(Entity entity, int tag, int radius, int x1, int y1, int z1) {		
		// The first search is based upon a cube around the entity's point in 
		// space, so we need to find how big the cube that can fit in the circle
		double hypotenuse = Math.sqrt(radius * radius + radius * radius);
		int limit = radius - (int)Math.sqrt(Math.pow(hypotenuse - radius, 2) / 2);
			
		// Check increasing cubes based on the origin until the limit, the step loop
		// defines the cube's size, while the inner two loops trace the surfaces
		for (int step = 1; step < limit; step++) {
			int x2 = x1 + step;
			for (int y2 = y1 + 1; y2 < y1 + step; y2++) {
				for (int z2 = z1 + 1; z2 < z1 + step; z2++) {
					
					// These next three loops allow us to translate along 
					// all of the quadrants of the cube
					for (int xsign = -1; xsign <= 1; xsign += 2) {
						for (int ysign = -1; ysign <= 1; ysign += 2) {
							for (int zsign = -1; zsign <= 1; zsign += 2) {
								
								// Now we get to actually check the point
								Entity result = checkPoint(entity, tag, x2 * xsign, y2 * ysign, z2 * zsign);
								if (result != null) {
									return result;
								}			
							}	
						}	
					}
					
				}
			}
		}
		
		// Pre-build the list of points defining the geometry
		int size = 2 * (radius - limit) + 2;
		int values[] = new int[size];
		int index = 0;
		for (int value = limit; value <= radius; value++) {
			values[index++] = value;
			values[index++] = -value;
		}
		
		// Check the remainder of the sphere, we need to check the points to 
		// make sure they are part of the sphere though
		for (int x2 = 0; x2 < size; x2++) {
			for (int y2 = 0; y2 < size; y2++) {
				for(int z2 = 0; z2 < size; z2++) {
					
					// First we need to know if the point is in the sphere, 
					// check this by the Euclidean distance
					int x = x1 - values[x2];
					int y = y1 - values[y2];
					int z = z1 - values[z2];
					double d = Math.sqrt(x*x + y*y + z*z);
					
					// It's not, so press on
					if (d > radius) {
						continue;
					}
					
					// It is, so check the point
					Entity result = checkPoint(entity, tag, x2, y2, z2);
					if (result != null) {
						return result;
					}
				}
			}
		}
		
		// Nothing was found
		return null;
	}

	/**
	 * Check that the entity at the given point is a valid match, returns true if it is, false otherwise.
	 */
	private Entity checkPoint(Entity entity, int tag, int x, int y, int z) {
		int hash = hashCoordinates(x, y, z);
		Bag bag = latticeMap.get(hash);
		
		// Must be something there
		if (bag == null) {
			return null;
		}
		
		// Check the entities in the location
		int size = bag.numObjs;
		for (int ndx = 0; ndx < size; ndx++) {
			// Can't be the same entity
			Entity check = (Entity)bag.get(ndx);
			if (check.equals(entity)) {
				continue;
			}
			
			// Do the tags match?			
			if (check.getEntityTypeTag() == tag) {
				return check;
			}
		}
		
		// Nothing found
		return null;		
	}

	/**
	 * Get all of the objects in the lattice.
	 */
	public Set<Entity> getAllObjects() {
		return entityMap.keySet();	
	}
			
	/**
	 * Get the all of the objects that share the location of the given object.
	 * 
	 * @param object To base the location on.
	 * @return The bag of objects, or null if the original object was not found.
	 */
	public Bag getColocatedObjects(final Entity object) {
		LocationAndIndex lai = entityMap.get(object);
		return (lai == null) ? null : lai.colocated;
	}
	
	/**
	 * Get the first entity with the given tag.
	 * 
	 * @param tag used to identify the entity.
	 * @return The first entity of the given type, or null if there are none.
	 */
	public Entity getFirstEntity(final int tag) {
		// Note the correct tag map based upon the tag
		ArrayDeque<Entity> entities = tagMap.get(tag);
		
		// Stale references can be left behind, so make sure the first entity we peek
		// still has a valid LAI present
		LocationAndIndex lai = null;
		while (lai == null && entities.size() > 0) {
			lai = entityMap.get(entities.peek());
			if (lai == null) {
				entities.pop();
			}
		}
		
		// Return the first entity in the queue.
		return entities.peek();
	}
	
	/**
	 * Get the location of the given object.
	 * 
	 * @param object To retrieve the location of.
	 * @return The location as Int3D or null if it does not exist.e
	 */
	public int[] getObjectLocation(final Entity object) {
		LocationAndIndex lai = entityMap.get(object);
		return (lai == null) ? null : lai.location;
	}
	
	/**
	 * Get all of the objects at the given location.
	 * 
	 * @param location To retrieve the objects from.
	 * @return The bag of objects or null.
	 */
	public Bag getObjectsAtLocation(final int[] location) {
		Integer hash = hashCoordinates(location[0], location[1], location[2]);
		return latticeMap.get(hash);
	}
	
	/**
	 * Hash the x, y, z coordinates provided based upon the internal hash table allocation.
	 * 
	 *  Based upon Teschner et al., 2003
	 */
	private Integer hashCoordinates(int x, int y, int z) {
		final int p1 = 73856093, p2 = 19349663, p3 = 83492791;
		return (x * p1 ^ y * p2 ^ z * p3) % allocation;
	}
	
	/**
	 * Remove the object if it exists. 
	 * 
	 * @param object to be removed.
	 * @return The location of the object, or null if it doesn't exist.
	 */
	public int[] remove(final Entity object) {
		
		// Start by removing the object finding out where it is located
		LocationAndIndex lai = entityMap.remove(object);
		if (lai == null) {
			// This should never actually occur
			throw new IllegalStateException("Attempted to remove an object not in the entityMap.");
		}
				
		// Remove from the the location lattice
		Bag bag = lai.colocated;
		bag.remove(object);
		
		// If nothing is left in the bag, clear it's memory
		int count = bag.numObjs;
		if (count == 0) {
			bag.clear();
		}

		// Shrink oversized bags
        if (count >= MIN_BAG_SIZE && count * LARGE_BAG_RATIO <= bag.objs.length) {
        	bag.shrink(count * REPLACEMENT_BAG_RATIO); 
		}
		
		// Return the location, be sure to release memory
        int[] location = lai.location;
        lai = null;
        return location;
	}
	
	/**
	 * Add or update the location of the object in the lattice.
	 * 
	 * @param object to be added or updated.
	 * @param location of the object in the lattice.
	 */
	public void setObjectLocation(final Entity object, final int[] location) {
		// Start by checking our conditions		
		if (object == null) {
			throw new IllegalStateException("Attempting to insert null into lattice.");
		}
		
		// Create an empty bag
		Bag bag = null;		
		
		// Check to see if the object already exists
		LocationAndIndex lai = entityMap.get(object);
		
		if (lai == null) {
			// No location returned, must be a new object
			lai = new LocationAndIndex();
			lai.location = location;
			entityMap.put(object, lai);
			tagMap.get(object.getEntityTypeTag()).add(object);
		} else {
			// Return if there is no update
			if (lai.location[0] == location[0] && lai.location[1] == location[1] && lai.location[2] == location[2]) {
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
            lai.location = location;
		}
		
		// Update the bag in the lattice at the new location
		Integer hash = hashCoordinates(location[0], location[1], location[2]);
		bag = latticeMap.get(hash);
		if (bag == null) {
			bag = new Bag(INITIAL_BAG_SIZE);
			latticeMap.put(hash, bag);
		} 
		bag.add(object);
		lai.colocated = bag;
	}
			
	/**
	 * Helper class that provides the location of the object in the lattice and
	 * the index of it in a bag.
	 */
	private static class LocationAndIndex {
		private Bag colocated;
		private int[] location;
	}
}
