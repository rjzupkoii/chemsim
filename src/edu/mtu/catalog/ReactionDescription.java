package edu.mtu.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.mtu.compound.Molecule;

/**
 * This class represents a single chemical equation.
 */
public class ReactionDescription {
		
	private List<String> products = new ArrayList<String>();
	private List<String> reactants = new ArrayList<String>();
	
	private double reactionRate = 0.0;
				
	/**
	 * Constructor.
	 */
	public ReactionDescription(List<String> reactants, List<String> products) {
		setReactants(reactants);
		this.products = products;
	}
	
	/**
	 * Constructor.
	 */
	public ReactionDescription(List<String> reactants, List<String> products, double reactionRate) {
		setReactants(reactants);
		this.products = products;
		this.reactionRate = reactionRate;
	}

	/**
	 * Constructor.
	 */
	public ReactionDescription(String[] reactants, String[] products, double reactionRate) {
		setReactants(Arrays.asList(reactants));
		this.products = Arrays.asList(products);
		this.reactionRate = reactionRate;
	}

	/**
	 * Check to see if the reactants are part of this reaction description.
	 */
	public boolean checkReactants(Molecule one, Molecule two) {
		boolean partOne = one.getFormula().equals(reactants.get(0));
		boolean partTwo = two.getFormula().equals(reactants.get(1));
		return partOne && partTwo;
	}
		
	/**
	 * Get the products of this equation.
	 */
	public List<String> getProducts() {
		return products;
	}

	/**
	 * Get the reactants of this equation.
	 */
	public List<String> getReactants() {
		return reactants;
	}
	
	/**
	 * Get the reaction rate.
	 */
	public double getReactionRate() {
		return reactionRate;
	}
	
	/**
	 * Set the reactants for this reaction.
	 */
	private void setReactants(List<String> value) {
		if (value.size() > 2) {
			throw new IllegalArgumentException("The number of reactants should not exceed two.");
		}
		reactants = value;
	}
	
	/**
	 * Set the reaction rate.
	 */
	public void setReactionRate(double value) {
		reactionRate = value;
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
		if (!Arrays.equals(this.products.toArray(), rd.products.toArray())) {
			return false;
		}
		if (!Arrays.equals(this.reactants.toArray(), rd.reactants.toArray())) {
			return false;
		}
		return (reactionRate == rd.reactionRate);
	}
	
	@Override
	public String toString() {
		String message = "";
		for (int ndx = 0; ndx < reactants.size(); ndx++) {
			message += reactants.get(ndx);
			if (ndx %2 == 0 && ndx != reactants.size() - 1) {
				message += " + ";
			}
		}
		message += " -> ";
		for (int ndx = 0; ndx < products.size(); ndx++) {
			message += products.get(ndx);
			if (ndx %2 == 0 && ndx != products.size() - 1) {
				message += " + ";
			}
		}
		return message;
	}
}
