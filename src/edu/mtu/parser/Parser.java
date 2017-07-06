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
	
	/**
	 * Read the chemicals from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return A list of reactions.
	 */
	public static List<ChemicalDto> parseChemicals(String fileName) throws IOException {
		// Open the file and discard the header
		CSVReader reader = new CSVReader(new FileReader(fileName));
		reader.readNext();
		
		// Load the entries
		String[] entries;
		List<ChemicalDto> results = new ArrayList<ChemicalDto>();
		while ((entries = reader.readNext()) != null) {
			results.add(new ChemicalDto(entries[0], entries[1], Double.parseDouble(entries[2])));
		}
		
		// Close and return
		reader.close();
		return results;
	}
	
	/**
	 * Read the reactions from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return A list of reactions.
	 */
	public static List<ReactionDescription> parseReactions(String fileName) throws IOException {
		// Open the file
		CSVReader reader = new CSVReader(new FileReader(fileName));
		
		// Read the header to note the number of items
		String[] enteries = reader.readNext();
		int reactants = 0;
		while (enteries[reactants].toUpperCase().equals("REACTANT")) {
			reactants++;
		}
		int products = 0;
		while (enteries[reactants + products].toUpperCase().equals("PRODUCT")) {
			products++;
		}
		
		List<ReactionDescription> results = new ArrayList<ReactionDescription>();
		while ((enteries = reader.readNext()) != null) {
			// Process the reactants
			List<String> reactant = new ArrayList<String>();
			for (int ndx = 0; ndx < reactants; ndx++) {
				if (!enteries[ndx].isEmpty()) {
					reactant.add(enteries[ndx]);	
				}
			}
						
			// Process the products
			List<String> product = new ArrayList<String>();
			for (int ndx = 0; ndx < products; ndx++) {
				product.addAll(parseProduct(enteries[reactants + ndx]));
			}				
			
			// Note the reaction rate
			double reactionRate = Double.parseDouble(enteries[reactants + products]);
			
			// Append to the running list
			results.add(new ReactionDescription(reactant, product, reactionRate));
		}
		
		// Close and return
		reader.close();
		return results;
	}
	
	/**
	 * Parse the given product into multiples, if appropriate.
	 */
	private static List<String> parseProduct(String product) {
		List<String> results = new ArrayList<String>();
		
		// Do we have any work to do?
		if (product.isEmpty()) {
			return results;
		}
		
		// Is there only a single product?
		if (!Character.isDigit(product.charAt(0))) {
			results.add(product);
			return results;
		}

		// Parse the number and return the appropriate count of products
		int count = 0;
		while (Character.isDigit(product.charAt(count))) { 
			count++;
		}
		String formula = product.substring(count);
		count = Integer.parseInt(product.substring(0, count));
		for (int ndx = 0; ndx < count; ndx++) {
			results.add(formula);
		}		
		return results;
	}
}
