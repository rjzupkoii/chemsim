package edu.mtu.parser;

/**
 * This class is used to parse the equation(s) that are present in an import file for their reaction.
 */
public class Parser {
	private String compoundPattern = "(\\*?[CHO\\d\\(\\)]*\\*?)";
	private String quantityPattern = "(\\d*x)";
	private String seperator = "→";
	
}
