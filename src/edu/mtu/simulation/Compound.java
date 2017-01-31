package edu.mtu.simulation;

import java.awt.Color;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int3D;

/**
 * The compound is the base class for all of chemical entities that we are simulating.
 */
@SuppressWarnings("serial")
public abstract class Compound implements Steppable {

	protected int[] movementVector;
		
	/**
	 * Perform the chemical reaction that are associated with UV exposure.
	 */
	protected abstract void doUVExposure();
	
	public Compound(Int3D movementVector) {
		this.movementVector = new int[3];
		this.movementVector[0] = movementVector.x;
		this.movementVector[1] = movementVector.y;
		this.movementVector[2] = movementVector.z;
	}
	
	@Override
	public void step(SimState state) {
		// TODO Perform any UV exposure 
		if (state.random.nextInt(10) == 1) {
			doUVExposure();
		}
		
		// Move the compound in space
		Int3D location = ((ChemSim)state).getCompounds().getObjectLocation(this);
		location = doMovement(location, ((ChemSim)state).getContainer());
		((ChemSim)state).getCompounds().setObjectLocation(this, location);
		
		// TODO Check to see if the compound has bumped into another one
		
		// TODO Perform any interactions between the compounds
		
	}
	
	/**
	 * Get the color to use when rendering the compound.
	 */
	public static Color getColor() 
	{
		throw new IllegalStateException();
	}
	
	/**
	 * Clear this compound from the simulation.
	 */
	protected void decay() {
		// TODO Flag this object to be removed from the simulation
	}
	
	/**
	 * Do the movement for this compound. This should only be called by the step function.
	 */
	// TODO Upgrade this to be Brownian Motion
	protected Int3D doMovement(Int3D location, Int3D container) {
		// Move the particle
		int x = location.x + movementVector[0];
		int y = location.y + movementVector[1];
		int z = location.z + movementVector[2];
		 
		// Check for a collision with the container
		if (x < 0) { x++; movementVector[0] = -movementVector[0]; }
		else if (x >= container.x) { x--; movementVector[0] = -movementVector[0]; }
		
		if (y < 0) { x++; movementVector[1] = -movementVector[1]; }
		else if (y >= container.y) { x--; movementVector[1] = -movementVector[1]; }

		if (z < 0) { x++; movementVector[2] = -movementVector[2]; }
		else if (z >= container.z) { x--; movementVector[2] = -movementVector[2]; }
		
		return new Int3D(x, y, z);
	}
	
	/**
	 * Generate a new compound of the given type, using the location of this compound
	 * as its initial vector.
	 */
	@SuppressWarnings("rawtypes")
	protected void generate(Class compoundName) {
		// TODO Generate the new compound and add it to the simulation
	}
}
