package edu.mtu.parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import edu.mtu.catalog.ReactionDescription;

/**
 * This class is used to parse the equation(s) that are present in an import file for their reaction.
 */
public class Parser {
	// TODO Determine if these are still needed
	private final static String compoundPattern = "(\\*?[CHO\\d\\(\\)]*\\*?)";
	private final static String quantityPattern = "(\\d*x)";
	private final static String seperator = "→";
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static List<ReactionDescription> parse(String fileName) throws IOException {
		// Open the file and discard the header
		CSVReader reader = new CSVReader(new FileReader(fileName));
		reader.readNext();
		
		String[] enteries;
		List<ReactionDescription> results = new ArrayList<ReactionDescription>();
		while ((enteries = reader.readNext()) != null) {
			// Process the reactants
			List<String> reactants = new ArrayList<String>();
			reactants.add(enteries[0]);
			if (!enteries[1].isEmpty()) { reactants.add(enteries[1]); }
			
			// Process the products
			List<String> products = new ArrayList<String>();
			products.add(enteries[2]);
			if (!enteries[3].isEmpty()) { products.add(enteries[3]); }			
			
			// Note the reaction rate
			double reactionRate = Double.parseDouble(enteries[4]);
			
			// Append to the running list
			results.add(new ReactionDescription(reactants, products, reactionRate));
		}
		
		// Close and return
		reader.close();
		return results;
	}
}
