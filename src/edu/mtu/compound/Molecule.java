package edu.mtu.compound;

import edu.mtu.reaction.MoleculeDescription;
import edu.mtu.reaction.Reaction;
import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.primitives.Entity;
import edu.mtu.primitives.Int3D;
import edu.mtu.primitives.Sparse3DLattice;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.schedule.Steppable;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class Molecule extends Steppable implements Entity {

	private MoleculeDescription md;
	
	// Pointer to the reactor we are working in
	private Sparse3DLattice grid;
	
	// Dimentions of the grid
	private int dx, dy, dz;
	
	// Used to identify the molecule and find reactions
	private int formulaHash;
	private String formula;
		
	/**
	 * Constructor.
	 */
	protected Molecule() { }
	
	/**
	 * Constructor.
	 */
	public Molecule(String formula) {
		this(formula, true);
	}
	
	/**
	 * Constructor, note if pointers should be cached or not.
	 */
	public Molecule(String formula, boolean cache) {
		this.formula = formula;
		formulaHash = formula.hashCode();
		if (cache) {
			md = ReactionRegistry.getInstance().getMoleculeDescription(formula);
			dx = Reactor.getInstance().dimensions.x;
			dy = Reactor.getInstance().dimensions.y;
			dz = Reactor.getInstance().dimensions.z;
			grid = Reactor.getInstance().grid;
		}
	}
			
	@Override
	public void doAction(int step) {
		// To save some time the model simplifies things a bit,
		// 1. Everything tries to react
		// 2. Radicals will always move if they don't react
		// 3. Everything else moves every minute of simulation time
		
		if (react()) {
			dispose();
		} else if (md.isRadical) {
			move();
		} else if (step % 60 == 0) {
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
	
	public boolean hasBimoleculear() {
		return md.hasBimolecular;
	}
	
	public boolean hasPhotolysis() {
		return md.hasPhotolysis;
	}
	
	public boolean hasUnimolecular() {
		return md.hasUnimolecular;
	}
	
	public boolean hasDissolvedReactants() {
		return md.hasDissolvedReactants;
	}
	
	public int[] getInteractionRadii() {
		return md.interactionRadius;
	}
	
	public int[] getReactantHashes() {
		return md.reactsWithHash;
	}
	
	/**
	 * Calculate the new location for this molecule.
	 */
	private void move() {
				
		// Get our current location
		Int3D location = grid.getObjectLocation(this);
		
		// Generate the random values for the walk 
		XoRoShiRo128PlusRandom random = ChemSim.getRandom();
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
		x = (x > dx) ? dx : x;
		x = (x < 0) ? 0 : x;
		
		y = (y > dy) ? dy : y;
		y = (y < 0) ? 0 : y;
		
		z = (z > dz) ? dz : z;
		z = (z < 0) ? 0 : z;
		
		// Set the new location
		grid.setObjectLocation(this, x, y, z);
	}

	/**
	 * See if a reaction should occur for this molecule at the location given. 
	 * 
	 *  @return True if something happened, false otherwise.
	 */
	private boolean react() {
		return Reaction.getInstance().react(this);
	}
	
	/**
	 * Check to see if the two molecules are the same chemical entity.
	 */
	public boolean sameEntity(Molecule moleclue) {
		return (formulaHash == moleclue.formulaHash);
	}
	
	/**
	 * Check to see if the two molecules are the same chemical entity.
	 */
	public boolean sameEntity(int formulaHash) {
		return (this.formulaHash == formulaHash);
	}

	@Override
	public int getEntityTypeTag() {
		return formulaHash;
	}
	
	@Override
	public String toString() {
		return formula;
	}
}
