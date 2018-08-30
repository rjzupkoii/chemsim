package edu.mtu.reaction;

import java.util.List;

import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.SimulationProperties;

/**
 * This class represents a single chemical equation.
 */
public class BasicReaction extends ChemicalEquation implements Cloneable {
		
	public final double k_diff = 1.10E+10;
	
	private double k = 0.0;
	private int interactionRadius = 0;
	private double ratio = 1.0;
				
	/**
	 * Private constructor.
	 */
	private BasicReaction() { }
	
	/**
	 * Constructor.
	 */
	public BasicReaction(List<String> reactants, List<String> products, double reactionRate) {
		setReactants(reactants);
		setProducts(products);
		k = reactionRate;
		interactionRadius = calcluateInteractionRadius();
	}
	
	/**
	 * Constructor.
	 */
	public BasicReaction(List<String> reactants, List<String> products, double reactionRate, double ratio) {
		setReactants(reactants);
		setProducts(products);
		k = reactionRate;
		this.ratio = ratio;
		interactionRadius = calcluateInteractionRadius();
	}
	
	/**
	 * Calculate the interaction radius for the reaction which is modeled as 
	 * the distance to search around molecules for a reaction, realistically 
	 * this should be a double, but we are using an integer lattice, so we 
	 * are using an integer instead.
	 * 
	 * Source: Pogson et al., 2006
	 */
	private int calcluateInteractionRadius() {
		if (k <= 0) {
			return 0;
		}
		
		double k_chem = (k * k_diff) / (k + k_diff);
		double delta_t = SimulationProperties.getInstance().getTimeStepLength();
		double r = Math.cbrt((3 * k_chem * delta_t) / (4 * Math.PI * Math.pow(10, 3) * Reactor.AvogadrosNumber));	// meters
		int r_nm = (int)(r * 1E+9);
		return r_nm;
	}
			
	/**
	 * Get the interaction radius for this reaction in an integer lattice.
	 */
	public int getInteractionRadius() {
		return interactionRadius;
	}
	
	/**
	 * Get the reaction rate.
	 */
	public double getReactionRate() {
		return k;
	}
	
	/**
	 * Get the reaction odds.
	 */
	public double getReactionRatio() {
		return ratio;
	}
	
	@Override
	public BasicReaction clone() {
		BasicReaction copy = new BasicReaction();
		copy.products = this.products.clone();
		copy.reactants = this.reactants.clone();
		copy.reactantHashes = this.reactantHashes.clone();		
		copy.ratio = this.ratio;
		copy.k = this.k;
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		// Same object as this is true
		if (obj == this) {
			return true;
		}
		
		// Make sure the object is this class
		if (!(obj instanceof BasicReaction)) {
			return false;
		}
		
		// Check the k value
		if (k != ((BasicReaction)obj).k) {
			return false;
		}
		
		// Defer to the base for everything else
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return super.toString() + ", r = " + interactionRadius;
	}
}
 