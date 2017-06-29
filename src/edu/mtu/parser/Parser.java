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
