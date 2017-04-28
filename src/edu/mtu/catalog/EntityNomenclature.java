package edu.mtu.catalog;

/**
 * This singleton provides a way of looking up a chemical formula and 
 * determining if there is a common name associated with it.
 */
public class EntityNomenclature {

	private static EntityNomenclature instance = new EntityNomenclature();
	
	/**
	 * Singleton constructor.
	 */
	private EntityNomenclature() { }
	
	/**
	 * Get the instance of the singleton.
	 */
	public static EntityNomenclature getInstance() {
		return instance;
	}
}
