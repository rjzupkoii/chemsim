package edu.mtu.catalog;

import java.util.Arrays;
import java.util.List;

import edu.mtu.compound.Molecule;

/**
 * This class represents a single chemical equation.
 */
public class ReactionDescription implements Cloneable {
		
	private String[] products;
	private String[] reactants;
	private int[] reactantHashes;
	
	private double reactionRate = 0.0;
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
		this.reactionRate = reactionRate;
	}
	
	/**
	 * Constructor.
	 */
	public ReactionDescription(List<String> reactants, List<String> products, double reactionRate, double reactionOdds) {
		setReactants(reactants);
		setProducts(products);
		this.reactionRate = reactionRate;
		this.reactionOdds = reactionOdds;
	}
	
	/**
	 * Check to see if the reactants are part of this reaction description.
	 */
	public boolean checkReactants(Molecule one, Molecule two) {
		//boolean partOne = one.getFormula().equals(reactants.get(0));
		//boolean partTwo = (two != null) ? two.getFormula().equals(reactants.get(1)) : reactants.size() == 1;
		
		boolean partOne = one.sameEntity(reactantHashes[0]);
		boolean partTwo = (two != null) ? two.sameEntity(reactantHashes[1]) : reactants.length == 1;		
		return partOne && partTwo;
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
		return reactionRate;
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
		for (int ndx = 0; ndx < 1; ndx++) {
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
		copy.reactionRate = this.reactionRate;
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
		return (reactionRate == rd.reactionRate);
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
		return message.toString();
	}
}
 