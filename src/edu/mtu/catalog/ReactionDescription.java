package edu.mtu.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import edu.mtu.compound.Molecule;

/**
 * This class represents a single chemical equation.
 */
public class ReactionDescription {
	
	private HashMap<Integer, String> dictionary = new HashMap<Integer, String>(); 
	
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
	 * Get the formula that maps to the given hash.
	 */
	public String getFormula(int hash) {
		return dictionary.get(hash);
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
}
