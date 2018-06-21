package edu.mtu.parser;

/**
 * This class acts as a DTO for reading chemical entries out of a CSV for an experiment.
 */
public class ChemicalDto {
	/**
	 * Name of the compound.
	 */
	public String name;
	
	/**
	 * Formula of the compound.
	 */
	public String formula;
	
	/**
	 * Molar concentration of the compound.
	 */
	public double mols;
	
	/**
	 * Count of moleclues of the compound in the model.
	 */
	public long count;
	
	public ChemicalDto(String name, String formula, double mols) {
		this.name = name;
		this.formula = formula;
		this.mols = mols;
	}	
}
