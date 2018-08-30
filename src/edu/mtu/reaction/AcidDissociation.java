package edu.mtu.reaction;

import java.util.List;

public class AcidDissociation extends ChemicalEquation implements Cloneable {
	private double pKa = 0.0;

	/**
	 * Private constructor.
	 */
	private AcidDissociation() { }

	/**
	 * Constructor.
	 */
	public AcidDissociation(List<String> reactants, List<String> products, double pKa) {
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

	@Override
	public String toString() {
		return super.toString() + ", pKa = " + pKa;
	}
}
