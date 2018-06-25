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
	private Int3D dimensions;
	private Sparse3DLattice grid;
	
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
			dimensions = Reactor.getInstance().dimensions;
			grid = Reactor.getInstance().grid;
		}
	}
	
	/**
	 * Constructor, mostly for probing memory space.
	 */
	public Molecule(String formula, Int3D dimensions) {
		this.formula = formula;
		this.dimensions = dimensions;
	}
		
	@Override
	public void doAction(int step) {	
		if (formulaHash == 306217752) {
			if (react()) {
				// If a reaction occurred, dispose of this molecule
				dispose();
			} else {
				move();
			}
			return;
		}
		
		if (md.isRadical) {			
			// Radicals move, but don't react which saves some
			move();
		} else if (react()) {
			dispose();
		} else if (step % 60 == 0) {
			// Allow the remaining molecules to move periodically to randomize things
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
