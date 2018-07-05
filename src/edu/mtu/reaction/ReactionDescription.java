package edu.mtu.reaction;

import java.util.Arrays;
import java.util.List;

import edu.mtu.compound.Molecule;
import edu.mtu.reactor.Reactor;
import edu.mtu.simulation.SimulationProperties;

/**
 * This class represents a single chemical equation.
 */
public class ReactionDescription implements Cloneable {
		
	public final double k_diff = 1.10E+10;
	
	private String[] products;
	private String[] reactants;
	private int[] reactantHashes;
	
	private double k = 0.0;
	private int interactionRadius = 0;
	private double reactionOdds = 1.0;	// Default is one to allow the field to be optional
				
	/**
	 * Constructor.
	 */
	private ReactionDescription() { }
	
	/**
	 * Constructor.
	 */
	public ReactionDescription(List<String> reactants, List<String> products, double reactionRate) {
		setReactants(reactants);
		setProducts(products);
		k = reactionRate;
		interactionRadius = calcluateInteractionRadius();
	}
	
	/**
	 * Constructor.
	 */
	public ReactionDescription(List<String> reactants, List<String> products, double reactionRate, double reactionOdds) {
		setReactants(reactants);
		setProducts(products);
		k = reactionRate;
		this.reactionOdds = reactionOdds;
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
	 * Check to see if the reactants are part of this reaction description.
	 */
	public boolean checkReactants(Molecule a, Molecule b) {
		// Invalid call
		if (a == null && b == null) return false;
		
		// One reactant
		if (reactants.length == 1) {											
			// Two molecules are invalid
			if (a != null && b != null) return false;							
			// Check that a is equal, assume b is null
			if (a != null && a.sameEntity(reactantHashes[0])) return true;		
			// Check that b is equal, assume as is null
			if (b.sameEntity(reactantHashes[0])) return true;					
			
			// Not a match
			return false;														
		}
		
		// Should have two molecules now
		if (a == null || b == null) return false;
		// Check same order as array
		if (a.sameEntity(reactantHashes[0]) && b.sameEntity(reactantHashes[1])) return true;
		// Check reverse order of array
		if (b.sameEntity(reactantHashes[0]) && a.sameEntity(reactantHashes[1])) return true;
		
		// Not a match
		return false;
	}
			
	/**
	 * Get the interaction radius for this reaction in an integer lattice.
	 */
	public int getInteractionRadius() {
		return interactionRadius;
	}
	
	/**
	 * Get the products of this equation.
	 */
	public String[] getProducts() {
		return products;
	}

	/**
	 * Get the reactants of this equation.
	 */
	public String[] getReactants() {
		return reactants;
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
	public double getReactionOdds() {
		return reactionOdds;
	}
	
	/**
	 * Set the reactants for this reaction.
	 */
	private void setReactants(List<String> value) {
		if (value.size() > 2) {
			throw new IllegalArgumentException("The number of reactants should not exceed two.");
		}
				
		// Note the reactants and their hash
		reactants = new String[value.size()];
		reactants = value.toArray(reactants);
		reactantHashes = new int[reactants.length];
		for (int ndx = 0; ndx < reactants.length; ndx++) {
			reactantHashes[ndx] = reactants[ndx].hashCode();
		}
	}
	
	/**
	 * Set the products for this reaction.
	 */
	private void setProducts(List<String> value) {
		products = new String[value.size()];
		products = value.toArray(products);
	}
	
	@Override
	public ReactionDescription clone() {
		ReactionDescription copy = new ReactionDescription();
		copy.products = this.products.clone();
		copy.reactants = this.reactants.clone();
		copy.reactantHashes = this.reactantHashes.clone();		
		copy.reactionOdds = this.reactionOdds;
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
		if (!(obj instanceof ReactionDescription)) {
			return false;
		}
		
		// Cast and compare the data
		ReactionDescription rd = (ReactionDescription)obj;
		if (!Arrays.equals(this.products, rd.products)) {
			return false;
		}
		if (!Arrays.equals(this.reactants, rd.reactants)) {
			return false;
		}
		return (k == rd.k);
	}
	
	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		for (int ndx = 0; ndx < reactants.length; ndx++) {
			message.append(reactants[ndx]);
			if ((ndx + 1) != reactants.length) {
				message.append(" + ");
			}
		}
		message.append(" -> ");
		for (int ndx = 0; ndx < products.length; ndx++) {
			message.append(products[ndx]);
			if ((ndx + 1) != products.length) {
				message.append(" + ");
			}
		}
		message.append(", r = " + interactionRadius);
		return message.toString();
	}
}
 