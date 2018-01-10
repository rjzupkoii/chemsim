package edu.mtu.Reactor;

import javax.activity.InvalidActivityException;

import edu.mtu.compound.Species;

/**
 * The reactor is the container that the experiment takes place in. As a 
 * simplification, the container is assumed to be square.
 */
public class Reactor {
	
	private static Reactor instance = new Reactor();
		
	private double volume;
		
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
	 * Creates entities of of the given species in a uniformly distributed fashion.
	 * 
	 * @param species The chemical species to create.
	 * @param mols The number of moles to create.
	 */
	public void createEntities(Species species, double mols) throws InvalidActivityException {
		
		// TODO Write this method
		throw new UnsupportedOperationException();
	}
		
	/**
	 * Get the volume of the container in milliliters (ml).
	 */
	public double getVolume() {
		return volume;
	}
}
