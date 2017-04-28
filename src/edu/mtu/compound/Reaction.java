package edu.mtu.compound;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single chemical equation.
 */
public class Reaction {
	
	private Operation operation;
	private List<String> products = new ArrayList<String>();
	private List<String> reactants = new ArrayList<String>();
		
	/**
	 * Get the operation undertaken in this equation.
	 */
	public Operation getOperation() {
		return operation;
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
	 * Set the products of this equation.
	 */
	public void setProducts(List<String> products) {
		this.products = products;
	}

	/**
	 * Set the reactants of this equation.
	 */
	public void setReactants(List<String> reactants) {
		this.reactants = reactants;
	}

	/**
	 * Set the operation performed in this reaction.
	 */
	public void setOperation(Operation value) {
		operation = value;
	}
}
