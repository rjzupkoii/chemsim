package edu.mtu.reaction;

import java.util.Arrays;
import java.util.List;

import edu.mtu.compound.Molecule;
import edu.mtu.util.FnvHash;

/**
 * Base class for all chemical equations, contains most of the relevant
 * information.
 */
public abstract class ChemicalEquation {
	protected String[] products;
	protected String[] reactants;
	protected int[] reactantHashes;
	
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
	
	@Override
	public boolean equals(Object obj) {
		ChemicalEquation ce = (ChemicalEquation)obj;
		if (!Arrays.equals(this.products, ce.products)) {
			return false;
		}
		if (!Arrays.equals(this.reactants, ce.reactants)) {
			return false;
		}
		return true;
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
	 * Set the reactants for this reaction.
	 */
	protected void setReactants(List<String> value) {
		if (value.size() > 2) {
			throw new IllegalArgumentException("The number of reactants should not exceed two.");
		}
				
		// Note the reactants and their hash
		reactants = new String[value.size()];
		reactants = value.toArray(reactants);
		reactantHashes = new int[reactants.length];
		for (int ndx = 0; ndx < reactants.length; ndx++) {
			reactantHashes[ndx] = FnvHash.fnv1a32(reactants[ndx]);
		}
	}
	
	/**
	 * Set the products for this reaction.
	 */
	protected void setProducts(List<String> value) {
		products = new String[value.size()];
		products = value.toArray(products);
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
