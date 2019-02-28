package edu.mtu.compound;

import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.mtu.primitives.Entity;
import edu.mtu.primitives.Sparse3DLattice;
import edu.mtu.reaction.MoleculeDescription;
import edu.mtu.reaction.Reaction;
import edu.mtu.reaction.ReactionRegistry;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.SimulationProperties;
import edu.mtu.simulation.schedule.Steppable;
import edu.mtu.util.FnvHash;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class Molecule extends Steppable implements Entity {

	private MoleculeDescription md;
	
	// Pointer to the reactor we are working in
	protected Sparse3DLattice grid;
	
	// Dimensions of the grid
	private int dx, dy, dz;
	
	// Used to identify the molecule and find reactions
	private Integer formulaHash;
	private String formula;
	
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
		formulaHash = FnvHash.fnv1a32(formula);
		
		if (cache) {
			md = ReactionRegistry.getInstance().getMoleculeDescription(formula);
			dx = Reactor.getInstance().dimensions[0];
			dy = Reactor.getInstance().dimensions[1];
			dz = Reactor.getInstance().dimensions[2];
			grid = Reactor.getInstance().grid;
		}
	}
			
	@Override
	public void doAction(int step) {				
		if (react()) {
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
	
	public int[] getInteractionRadii() {
		return md.interactionRadius;
	}
	
	public Integer[] getReactantHashes() {
		return md.reactsWithHash;
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
		
	/**
	 * Calculate the new location for this molecule.
	 */
	protected void move() {

		// Get our current location use our own copy
		int[] location = grid.getObjectLocation(this).clone();
			
		// Find our speed with a bit of noise
		XoRoShiRo128PlusRandom random = (XoRoShiRo128PlusRandom)ChemSim.getInstance().getRandom();
		int speed = (int)Math.round((random.nextGaussian() * 1e-8 + 5.9e-7) * 1e9);
		
		// Find random random angles
		double theta = -Math.PI + 2 * Math.PI * random.nextDoubleFast();
		double phi = -Math.PI + 2 * Math.PI * random.nextDoubleFast();
		
		// Convert the coordinates
		SphericalCoordinates coords = new SphericalCoordinates(speed, theta, phi);
		Vector3D sphere = coords.getCartesian();
		
		// Apply the vector with the dt adjustment
		double dt = SimulationProperties.getInstance().getDeltaT();
		location[0] += (int)(dt * sphere.getX());
		location[1] += (int)(dt * sphere.getY());
		location[2] += (int)(dt * sphere.getZ());
				
		// Adjust the location as needed so we stay in the bounds of the container
		location[0] = (location[0] > dx) ? dx : location[0];
		location[0] = (location[0] < 0) ? 0 : location[0];
		
		location[1] = (location[1] > dy) ? dy : location[1];
		location[1] = (location[1] < 0) ? 0 : location[1];
		
		location[2] = (location[2] > dz) ? dz : location[2];
		location[2] = (location[2] < 0) ? 0 : location[2];
		
		// Set the new location
		grid.setObjectLocation(this, location);
	}

	/**
	 * See if a reaction should occur for this molecule at the location given. 
	 * 
	 *  @return True if something happened, false otherwise.
	 */
	protected boolean react() {
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
	public Integer getEntityTypeTag() {
		return formulaHash;
	}
	
	@Override
	public String toString() {
		return formula;
	}
}
