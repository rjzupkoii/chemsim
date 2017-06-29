package edu.mtu.compound;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import edu.mtu.simulation.ChemSim;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int3D;

/**
 * This class represents a chemical entity.
 */
@SuppressWarnings("serial")
public class Species implements Steppable {

	private boolean photosensitive = false;
	private String formula;

	private Stoppable stoppable;
	
	/**
	 * Constructor.
	 */
	public Species(String formula) {
		this.formula = formula;
	}
	
	public static boolean areEqual(Species one, Species two) {
		// Not equal if one is null
		if (one == null || two == null) {
			return false;
		}
		
		// The given formulas should be the same
		return one.getFormula().equals(two.getFormula());
	}
	
	public static boolean areEqual(String formula, Species species) {
		// Not equal if the species is null
		if (species == null) {
			return false;
		}
		
		// The given formulas should be the same
		return formula.equals(species.getFormula());
	}
	
	/**
	 * Remove this chemical species from the simulation.
	 */
	public void dispose() {
		ChemSim state = ChemSim.getInstance();
		state.getMolecules().remove(this);
		stoppable.stop();
	}
	
	/**
	 * Get the color of this species.
	 * 
	 * @return The color to be used in visualizations.
	 */
	public Color getColor() {
		// TODO Figure out how we want to do this.
		return Color.BLUE;
	}
	
	/**
	 * Get the formula of this entity.
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * Get the flag to indicate if this species is photosensitive.
	 */
	public boolean getPhotosensitive() {
		return photosensitive;
	}
		
	/**
	 * Set the flag to indicate if this species is photosensitive.
	 */
	public void setPhotosensitive(boolean value) {
		photosensitive = value;
	}
	
	/**
	 * Set the simulation engine object needed to remove this chemical species from the schedule.
	 */
	public void setStoppable(Stoppable value) {
		stoppable = value;
	}
	
	@Override
	public void step(SimState state) {
		// Move the compound in space
		move((ChemSim)state);		
		
		// Perform any reaction
		Reaction.getInstance().react(this);		
	}
	
	/**
	 * Move the compound to a new location using a random walk.
	 */
	private void move(ChemSim state) {
		// Get the location of the compound
		Int3D location = state.getMolecules().getObjectLocation(this);
		Int3D container = state.getContainer();
		
		// Generate the random values for the walk 
		MersenneTwisterFast random = state.random;
		double walkX = random.nextGaussian();
		double walkY = random.nextGaussian();
		double walkZ = random.nextGaussian();
		
		// Apply the values, note that we are discarding everything outside of one standard deviation
		int x = location.x + ((walkX > 0 && walkX <= 1) ? 1 : 0);
		x += (walkX <= 0 && walkX >= -1) ? -1 : 0;
		
		int y = location.y + ((walkY > 0 && walkY <= 1) ? 1 : 0);
		y += (walkY <= 0 && walkY >= -1) ? -1 : 0;
		
		int z = location.z + ((walkZ > 0 && walkZ <= 1) ? 1 : 0);
		z += (walkZ <= 0 && walkZ >= -1) ? -1 : 0;
		
		// Adjust the location as needed so we stay in the bounds of the container
		x = (x > container.x) ? container.x : x;
		x = (x < 0) ? 0 : x;
		
		y = (y > container.y) ? container.y : y;
		y = (y < 0) ? 0 : y;
		
		z = (z > container.z) ? container.z : z;
		z = (z < 0) ? 0 : z;
		
		// Set the new location
		state.getMolecules().setObjectLocation(this, new Int3D(x, y, z));
	}
}
