package edu.mtu.parser;

/**
 * This class acts as a DTO for reading chemical entries out of a CSV for an experiment.
 */
public class ChemicalDto {
	public String name;
	public String formula;
	public double mols;
	
	public ChemicalDto(String name, String formula, double mols) {
		this.name = name;
		this.formula = formula;
		this.mols = mols;
	}	
}
