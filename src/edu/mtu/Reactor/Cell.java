 package edu.mtu.Reactor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
	private Map<Species, BigDecimal> speciesList; 
	private Point3D location;
	
	/**
	 * Constructor.
	 */
	public Cell(Point3D location, double volume) {
		this.volume = volume;
		this.location = location;
	}
	
	@Override
	public void step(SimState state) {
		// Reactions
		for (Species key : speciesList.keySet()) {
			Reaction.getInstance().react(key, this);
		}
		
		// Diffusion
		for (Species key : speciesList.keySet()) {
			diffuse(state, key);
		}
	}
	
	/**
	 * Update the given species quantity with the indicated quantity.
	 */
	public void add(Species species, BigDecimal value) {
		BigDecimal current = speciesList.get(species);
		speciesList.put(species, current.add(value));
	}
	
	/**
	 * Move the compound to a new location using a random walk.
	 */
	private void diffuse(SimState state, Species species) {
		// Get the size of the reactor
		Reactor reactor = Reactor.getInstance();
		int size = reactor.getCellCount();
		if (size == 0) {
			throw new IllegalStateException("The reactor has not been initialized.");
		}
		
		// Generate the random values for the walk 
		MersenneTwisterFast random = state.random;
		double walkX = random.nextGaussian();
		double walkY = random.nextGaussian();
		double walkZ = random.nextGaussian();
		
		// Apply the values, note that we are discarding everything outside of one standard deviation
		int x = (int)(location.getX() + ((walkX > 0 && walkX <= 1) ? 1 : 0));
		x += (walkX <= 0 && walkX >= -1) ? -1 : 0;
		
		int y = (int)(location.getY() + ((walkY > 0 && walkY <= 1) ? 1 : 0));
		y += (walkY <= 0 && walkY >= -1) ? -1 : 0;
		
		int z = (int)(location.getZ() + ((walkZ > 0 && walkZ <= 1) ? 1 : 0));
		z += (walkZ <= 0 && walkZ >= -1) ? -1 : 0;
		
		// Adjust the location as needed so we stay in the bounds of the container
		x = (x > size) ? size : x;
		x = (x < 0) ? 0 : x;
		
		y = (y > size) ? size : y;
		y = (y < 0) ? 0 : y;
		
		z = (z > size) ? size : z;
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
	 * Return a list of all species with at least one entity.s
	 */
	public List<Species> getMolecules() {
		ArrayList<Species> results = new ArrayList<Species>();
		for (Species species : speciesList.keySet()) {
			if (speciesList.get(species).compareTo(BigDecimal.ZERO) > 0) {
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
	
	public void remove(Species species) {
		speciesList.put(species, BigDecimal.ZERO);
	}
	
	public void remove(Species species, BigDecimal value) {
		BigDecimal current = speciesList.get(species);
		speciesList.put(species, current.add(value));
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
		BigDecimal percentage = new BigDecimal(state.random.nextGaussian());
		BigDecimal current = speciesList.get(species); 
		BigDecimal transfer = current.multiply(percentage);
		
		// Move the mols
		speciesList.put(species, current.subtract(transfer));
		target.add(species, transfer);		
	}
}
