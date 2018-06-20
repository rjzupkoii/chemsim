package edu.mtu.compound;

import edu.mtu.catalog.ReactionRegistry;
import edu.mtu.primitives.Int3D;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.schedule.Steppable;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class Molecule extends Steppable{

	private boolean hasReactants;
	private Int3D dimensions;
	private String formula;
		
	/**
	 * Constructor.
	 */
	public Molecule(String formula) {
		this.formula = formula;
		dimensions = Reactor.getInstance().dimensions;
		hasReactants = ReactionRegistry.hasReactants(formula);
	}
	
	/**
	 * Constructor, mostly for probing memory space.
	 */
	public Molecule(String formula, Int3D dimensions, boolean hasReactants) {
		this.formula = formula;
		this.dimensions = dimensions;
		this.hasReactants = hasReactants;
	}
	
	@Override
	public void doAction() {
		
		// If no reactants exist for us, then dispose of ourselves
		if (!hasReactants) {
			dispose(false);
			return;
		}
			
		if (react()) {
			// If a reaction occurred, dispose of this molecule
			dispose();
		} else {
			move();
		}		
	}
		
	/**
	 * Dispose of this molecule.
	 */
	public void dispose() {
		dispose(true);
	}
	
	/**
	 * Dispose of this molecule.
	 * 
	 * @param update True if the count should be decremented, false otherwise.
	 */
	private void dispose(boolean update) {
		if (update) {
			ChemSim.getTracker().update(formula, -1);
		}
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
	private void move() {
				
		// Get our current location
		Int3D location = Reactor.getInstance().getLocation(this);
		
		// Generate the random values for the walk 
		XoRoShiRo128PlusRandom random = ChemSim.getInstance().random;
		double walkX = random.nextDoubleFast();
		double walkY = random.nextDoubleFast();
		double walkZ = random.nextDoubleFast();
		
		// Apply the values, note that we are discarding everything outside of one standard deviation
		int x = location.x + (walkX >= 0.5 ? 1 : 0);
		x += (walkX < 0.5) ? -1 : 0;
		
		int y = location.y + ((walkY >= 0.5) ? 1 : 0);
		y += (walkY < 0.5) ? -1 : 0;
		
		int z = location.z + ((walkZ >= 0.5) ? 1 : 0);
		z += (walkZ < 0.5) ? -1 : 0;
		
		// Adjust the location as needed so we stay in the bounds of the container
		x = (x > dimensions.x) ? dimensions.x : x;
		x = (x < 0) ? 0 : x;
		
		y = (y > dimensions.y) ? dimensions.y : y;
		y = (y < 0) ? 0 : y;
		
		z = (z > dimensions.z) ? dimensions.z : z;
		z = (z < 0) ? 0 : z;
		
		// Set the new location		
		Reactor.getInstance().setLocation(this, new Int3D(x, y, z));
	}

	/**
	 * See if a reaction should occur for this molecule at the location given. 
	 * 
	 *  @return True if something happened, false otherwise.
	 */
	private boolean react() {
		return Reaction.getInstance().react(this);
	}
}
