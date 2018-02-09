package edu.mtu.compound;

import ec.util.MersenneTwisterFast;
import edu.mtu.Reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.schedule.Steppable;
import sim.util.Int3D;

public class Molecule extends Steppable{

	private String formula;
		
	/**
	 * Constructor.
	 */
	public Molecule(String formula) {
		this.formula = formula;
	}
	
	@Override
	public void doAction() {
		// Attempt to move this molecule
		Reactor reactor = Reactor.getInstance();
		Int3D location = move(reactor.getLocation(this), reactor.getContainer());
		
		if (react(location)) {
			// If a reaction occurred, dispose of this molecule
			dispose();
		} else {
			// Otherwise, apply the movement
			reactor.setLocation(this, location);
			ChemSim.getTracker().update(formula, 1);
		}		
	}
		
	/**
	 * Dispose of this molecule.
	 */
	public void dispose() {
		Reactor.getInstance().remove(this);
		ChemSim.getSchedule().remove(this);
	}
	
	/**
	 * Get the formula of this molecule.
	 */
	public String getFormula() {
		return formula;
	}
	
	/**
	 * Calculate the new location for this molecule.
	 */
	private Int3D move(Int3D location, Int3D container) {
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
	 * See if a reaction should occur for this molecule at the location given. 
	 * 
	 *  @return True if something happened, false otherwise.
	 */
	private boolean react(Int3D location) {
		// TODO Write this method
		return false;
	}
}
