package edu.mtu.compound;

import java.awt.Color;

/**
 * This class represents a chemical entity.
 */
public class Species {

	private boolean photosensitive = false;
	private String formula;
	
	/**
	 * Constructor.
	 */
	public Species(String formula) {
		this.formula = formula;
	}
	
	public static boolean areEqual(Species one, Species two) {
		// Not equal if one is null
		if (one == null || two == null) {
			return false;
		}
		
		// The given formulas should be the same
		return one.getFormula().equals(two.getFormula());
	}
	
	public static boolean areEqual(String formula, Species species) {
		// Not equal if the species is null
		if (species == null) {
			return false;
		}
		
		// The given formulas should be the same
		return formula.equals(species.getFormula());
	}
	
	/**
	 * Get the color of this species.
	 * 
	 * @return The color to be used in visualizations.
	 */
	public Color getColor() {
		// TODO Figure out how we want to do this.
		return Color.BLUE;
	}
	
	/**
	 * Get the formula of this entity.
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * Get the flag to indicate if this species is photosensitive.
	 */
	public boolean getPhotosensitive() {
		return photosensitive;
	}
		
	/**
	 * Set the flag to indicate if this species is photosensitive.
	 */
	public void setPhotosensitive(boolean value) {
		photosensitive = value;
	}
}
