 package edu.mtu.Reactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.util.MersenneTwisterFast;
import edu.mtu.compound.Reaction;
import edu.mtu.compound.Species;
import javafx.geometry.Point3D;
import sim.engine.SimState;
import sim.engine.Steppable;

/***
 * This class represents a single point within the reactor.
 */
@SuppressWarnings("serial")
public class Cell implements Steppable {

	private double volume;
	private Map<Species, Long> speciesList;
	private Point3D location;
	
	/**
	 * Constructor.
	 */
	public Cell(Point3D location, double volume) {
		speciesList = new HashMap<Species, Long>();
		this.volume = volume;
		this.location = location;
	}
	
	@Override
	public void step(SimState state) {
		if (speciesList.isEmpty()) {
			System.err.println(String.format("Cell (%d, %d, %d) has an empty species list.", 
					(int)location.getX(), (int)location.getY(), (int)location.getZ()));
			return;
		}
		
		// Reactions
		ArrayList<Species> keys = new ArrayList<Species>(speciesList.keySet());
		for (Species key : keys) {
			Reaction.getInstance().react(key, this);
		}
		
		// Diffusion
//		keys = new ArrayList<Species>(speciesList.keySet());
//		for (Species key : keys) {
//			diffuse(state, key);
//		}
	}
	
	/**
	 * Update the given species quantity with the indicated quantity.
	 */
	public void add(Species species, long value) {
		if (speciesList == null) {
			throw new IllegalStateException("The reactor has not been initialized."); 
		}
		if (value < 0) {
			throw new IllegalArgumentException(
					String.format("The value (%d) for %s cannot be less than zero", value, species.getFormula()));
		}
		
		if (!speciesList.containsKey(species)) {
			speciesList.put(species, value);
		} else {
			long current = speciesList.get(species);
			speciesList.put(species, current + value);
		}
	}
		
	/**
	 * Get the number of molecules of the given species.
	 */
	public long count(Species species) {
		boolean check = speciesList.containsKey(species);
		return check ? speciesList.get(species) : 0;
	}
	
	/**
	 * Move the compound to a new location using a random walk.
	 */
	private void diffuse(SimState state, Species species) {
		// Get the size of the reactor
		Reactor reactor = Reactor.getInstance();
		long size = reactor.getCellWidth();
		if (size == 0) {
			throw new IllegalStateException("The reactor has not been initialized.");
		}
		
		// Generate the random values for the walk
		MersenneTwisterFast random = state.random;
		double walkX = random.nextGaussian();
		double walkY = random.nextGaussian();
		double walkZ = random.nextGaussian();
		
		// Apply the values, note that we are discarding everything outside of one standard deviation
		long x = (long)(location.getX() + ((walkX > 0 && walkX <= 1) ? 1 : 0));
		x += (walkX <= 0 && walkX >= -1) ? -1 : 0;
		
		long y = (long)(location.getY() + ((walkY > 0 && walkY <= 1) ? 1 : 0));
		y += (walkY <= 0 && walkY >= -1) ? -1 : 0;
		
		long z = (long)(location.getZ() + ((walkZ > 0 && walkZ <= 1) ? 1 : 0));
		z += (walkZ <= 0 && walkZ >= -1) ? -1 : 0;
		
		// Adjust the location as needed so we stay in the bounds of the container
		x = (x >= size) ? size - 1 : x;
		x = (x < 0) ? 0 : x;
		
		y = (y >= size) ? size - 1: y;
		y = (y < 0) ? 0 : y;
		
		z = (z >= size) ? size - 1 : z;
		z = (z < 0) ? 0 : z;
		
		// Return if we haven't actually moved
		if (x == location.getX() && y == location.getY() && z == location.getZ()) {
			return;
		}
		
		// Get the neighboring cell and make the transfer
		Cell target = reactor.getCell(new Point3D(x, y, z));
		transfer(state, species, target);
	}
	
	/**
	 * Return a list of all species with at least one entity.
	 */
	public List<Species> getMolecules() {
		ArrayList<Species> results = new ArrayList<Species>();
		for (Species species : speciesList.keySet()) {
			if (speciesList.get(species) != 0) {
				results.add(species);
			}
		}
		return results;
	}
	
	/**
	 * Get the volume of this cell.
	 */
	public double getVolume() {
		return volume;
	}
	
	/**
	 * Removes all of the species form the model.
	 */
	public void remove(Species species) {
		speciesList.put(species, 0l);
	}
	
	/**
	 * Removes the given quantity of the species from the model. 
	 */
	public void remove(Species species, long value) {
		if (speciesList == null) {
			throw new IllegalStateException("The reactor has not been initialized."); 
		}
		if (value < 0) {
			throw new IllegalArgumentException("The value cannot be less than zero");
		}
		
		if (!speciesList.containsKey(species)) {
			throw new IllegalStateException(String.format("%s does not exist as a species", species.getFormula()));
		} else {
			long current = speciesList.get(species);
			long update = current - value;
			update = (update < 0) ? 0 : update;
			speciesList.put(species, update);
		}
	}
	
	/**
	 * Diffuse some moles of given species from this cell to the target 
	 * 
	 * @param state The current state of the simulation.
	 * @param species The species to be diffused.
	 * @param target The target cell to receive the species.
	 */
	private void transfer(SimState state, Species species, Cell target) {
		// Calculation how much diffusion
		double percentage = Math.abs(state.random.nextGaussian());
		long current = speciesList.get(species); 
		if (current < 0) {
			// TODO This is likely due to a bounds error
			System.err.println(String.format("Less than zero on %s", species.getFormula()));
		}
		long transfer = (long)(current * percentage);
		
		// Move the mols
		remove(species, transfer);
		target.add(species, transfer);		
	}
}
