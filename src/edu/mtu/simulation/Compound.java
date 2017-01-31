package edu.mtu.simulation;

import java.awt.Color;

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

	private Stoppable stoppable; 
	
	protected int[] movementVector;	
		
	/**
	 * Perform the chemical reaction that are associated with UV exposure.
	 */
	protected abstract void doUVExposure();
	
	/**
	 * Perform any interaction between this compound and the indicated compound.
	 */
	protected abstract void interact(Compound compound);
	
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
		if (state.random.nextDouble() < ChemSim.getInstance().getUvIntensity()) {
			doUVExposure();
			return;
		}
		
		// Check to see if the compound has bumped into another one
		Bag objects = simulation.getCompounds().getObjectsAtLocation(location);
		for (Object object : objects) {
			// If the object is this one, continue
			if (object.equals(this)) {
				continue;
			}
			
			// Otherwise, interact and break
			interact((Compound)object);
			break;
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
		ChemSim state = ChemSim.getInstance();
		state.getCompounds().remove(this);
		stoppable.stop();
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
