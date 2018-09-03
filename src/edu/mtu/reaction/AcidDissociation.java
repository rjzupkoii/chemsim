package edu.mtu.reaction;

import java.util.List;

import javax.activity.InvalidActivityException;

public class AcidDissociation extends ChemicalEquation implements Cloneable {
	private double pKa = 0.0;

	/**
	 * Private constructor.
	 */
	private AcidDissociation() { }

	/**
	 * Constructor.
	 */
	public AcidDissociation(List<String> reactants, List<String> products, double pKa) throws InvalidActivityException {
		setReactants(reactants);
		setProducts(products);
		this.pKa = pKa;
	}

	@Override
	public AcidDissociation clone() {
		AcidDissociation copy = new AcidDissociation();
		copy.products = this.products.clone();
		copy.reactants = this.reactants.clone();
		copy.reactantHashes = this.reactantHashes.clone();
		copy.pKa = this.pKa;
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

		// Check the pKa value
		if (pKa != ((AcidDissociation) obj).pKa) {
			return false;
		}

		// Defer to the base for everything else
		return super.equals(obj);
	}
	
	String acid() {
		return reactants[0];
	}
	
	String conjugateBase() {
		return products[0].endsWith("-") ? products[0] : products[1];
	}
	
	String hydrogenIon() {
		return products[0].endsWith("+") ? products[0] : products[1];
	}
	
	double pKa() {
		return pKa;
	}

	@Override
	public String toString() {
		return super.toString().replace("->", "<=>") + ", pKa = " + pKa;
	}
}
