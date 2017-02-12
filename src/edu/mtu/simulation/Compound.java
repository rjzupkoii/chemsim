package edu.mtu.simulation;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.Int3D;

/**
 * The compound is the base class for all of chemical entities that we are simulating.
 */
@SuppressWarnings("serial")
public abstract class Compound implements Steppable {

	private boolean decayed = false;
	private Stoppable stoppable; 
		
	protected int[] movementVector;	
		
	/**
	 * Perform any delayed disproportionation reactions.
	 */
	protected abstract void doDisproportionation(MersenneTwisterFast random);

	/**
	 * Perform any chemical reactions that are associated with oxidation.
	 */
	protected abstract void doOxidation(MersenneTwisterFast random);
	
	/**
	 * Perform the chemical reaction that are associated with UV exposure.
	 */
	protected abstract void doUVExposure(MersenneTwisterFast random);
	
	/**
	 * Perform any interaction between this compound and the indicated compound.
	 */
	protected abstract boolean interact(Compound compound);
	
	public Compound(Int3D movementVector) {
		this.movementVector = new int[3];
		this.movementVector[0] = movementVector.x;
		this.movementVector[1] = movementVector.y;
		this.movementVector[2] = movementVector.z;
	}
	
	@Override
	public void step(SimState state) {
		// Move the compound in space
		ChemSim simulation = (ChemSim)state;
		Int3D location = simulation.getCompounds().getObjectLocation(this);
		location = doMovement(location, simulation.getContainer());
		simulation.getCompounds().setObjectLocation(this, location);

		// Perform any UV exposure
		if (state.random.nextDouble() < ChemSim.getProperties().getUvIntensity()) {
			doUVExposure(state.random);
			if (decayed) {
				return;
			}
		}
		
		// Perform any oxidation based upon current saturation
		if (state.random.nextDouble() < ChemSim.getProperties().getOxygenSaturation()) {
			doOxidation(state.random);
			if (decayed) {
				return;
			}
		}
		
		// Perform any delayed disproportionation reactions
		doDisproportionation(state.random);
		if (decayed) {
			return;
		}		
				
		// Check to see if the compound has bumped into another one
		Bag objects = simulation.getCompounds().getObjectsAtLocation(location);
		for (Object object : objects) {
			// If the object is this one, continue
			if (object.equals(this)) {
				continue;
			}
			
			// Otherwise, interact and break if an interaction occurred
			if (interact((Compound)object)) {
				break;
			}
		}
	}
	
	/**
	 * Get the color to use when rendering the compound.
	 */
	public static Color getColor() 
	{
		throw new IllegalStateException();
	}
	
	/**
	 * Set the simulation engine object needed to remove the compound from the schedule.
	 */
	public void setStoppable(Stoppable stoppable) {
		this.stoppable = stoppable;
	}
	
	/**
	 * Clear this compound from the simulation.
	 */
	private void decay() {
		// Remove the compound from the simulation
		ChemSim state = ChemSim.getInstance();
		state.getCompounds().remove(this);
		stoppable.stop();
		
		// Flag the object as decayed
		decayed = true;
	}
	
	/**
	 * Clear the compound from the simulation.
	 */
	protected void decay(Compound compound) {
		compound.decay();
	}
	
	/**
	 * Do the movement for this compound. This should only be called by the step function.
	 */
	// TODO Upgrade this to be proper Brownian Motion that takes into account any momentum
	// TODO that is imparted by collisions with other molecules. For now we are using a 
	// TODO three-dimensional random walk with the (x, y, z) movements being randomly 
	// TODO distributed in a Gaussian space.	
	protected Int3D doMovement(Int3D location, Int3D container) {
		// Generate the random values for the walk 
		MersenneTwisterFast random = ChemSim.getInstance().random;
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
		
		// Return the new location
		return new Int3D(x, y, z);
	}
	
	/**
	 * Generate a new compound of the given type, using the location of this compound
	 * as its initial vector.
	 */
	@SuppressWarnings("rawtypes")
	protected void generate(Class compoundName) {
		// Get the location of the compound in space
		ChemSim state = ChemSim.getInstance();
		Int3D location = state.getCompounds().getObjectLocation(this);
		if (location == null) {
			throw new IllegalStateException("Unable to get compound location. Did you call decay() before generate()?");
		}
		
		// Create the compound and move it
		Compound compound = state.createCompoundAt(compoundName, location);
		compound.doMovement(location, state.getContainer());
	}
}
