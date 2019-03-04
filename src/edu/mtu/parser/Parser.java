package edu.mtu.parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import edu.mtu.reaction.AcidDissociation;
import edu.mtu.reaction.BasicReaction;
import edu.mtu.reaction.ChemicalEquation;

/**
 * This class is used to parse the equation(s) that are present in an import file for their reaction.
 */
public class Parser {
	public static final double INVALID_ENTRY_VALUE = -999.999;
	
	/**
	 * Read the chemicals from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return A list of reactions.
	 */
	public static List<ChemicalDto> parseChemicals(String fileName) throws IOException {
		CSVReader reader = null;
		
		try {
			// Open the file, discard volume and rate
			reader = new CSVReader(new FileReader(fileName));
			
			// Scan until we reach the chemicals header
			String[] entries;
			while ((entries = reader.readNext()) != null) {
				if (entries[0].toUpperCase().equals("NAME")) {
					break;
				}
			}
			if (entries == null) {
				System.err.println("File provided does not contain the chemicals header.");
				throw new IOException("Invalid ChemSim chemicals file.");
			}
			
			// Load the entries
			List<ChemicalDto> results = new ArrayList<ChemicalDto>();
			while ((entries = reader.readNext()) != null) {
				if (entries[0].startsWith("#") || entries[0].isEmpty()) { continue; }
				results.add(new ChemicalDto(entries[0], entries[1], Double.parseDouble(entries[2])));
			}
			
			// Return the results
			return results;
		} finally {
			if (reader != null) reader.close();	
		}		
	}
		
	/**
	 * Parse the reaction rate from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return The reaction rate (i.e., slope) from the file, assumed to be in mols/L
	 */
	public static double parseRate(String fileName) throws IOException {
		CSVReader reader = null;
		
		try {
			// Open the file, discard volume
			reader = new CSVReader(new FileReader(fileName));
			reader.readNext();
			
			// Second entry should be the rate
			String[] entries = reader.readNext();
			if (!entries[0].toUpperCase().equals("RATE")) {
				System.err.println("File provided does not contain the rate on line two.");
				throw new IOException("Invalid ChemSim chemicals file.");
			}
			
			// Return the result
			return Double.parseDouble(entries[1]);
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static double parseIntercept(String fileName) throws IOException {
		CSVReader reader = null;
		
		try {
			// Open the file, discard volume
			reader = new CSVReader(new FileReader(fileName));
			reader.readNext();
			
			// Second entry should be the rate and intercept
			String[] entries = reader.readNext();
			if (!entries[0].toUpperCase().equals("RATE")) {
				System.err.println("File provided does not contain the rate on line two.");
				throw new IOException("Invalid ChemSim chemicals file.");
			}
			
			// Check for the intercept, return null if one isn't present
			if (entries.length != 3){
				return INVALID_ENTRY_VALUE;
			}
			
			// Return the result
			return Double.parseDouble(entries[2]);
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	/**
	 * Check to see if the file contains an override for the hydroxyl retention rate.
	 * 
	 * @param fileName The full path to the file.
	 * @return True if an override is present, false otherwise.
	 */
	public static boolean checkForHydroxylPercentage(String fileName) throws IOException {
		CSVReader reader = null;
		
		try {
			// Open the file
			reader = new CSVReader(new FileReader(fileName));
			
			// Read until we find the value, or the "Name" marker for the chemicals listing
			String[] entries;
			while ((entries = reader.readNext()) != null) {
				// Match found
				if (entries[0].toUpperCase().equals("PERCENTAGE")) {
					return true;
				}
				
				// Start of chemicals found
				if (entries[0].toUpperCase().equals("NAME")) {
					return false;
				}
			}
			return false;
			
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	
	/**
	 * Parse the percentage of hydroxyl radicals that should be retained.
	 * 
	 * @param fileName The full name and path of the file.
	 * @return The value as a decimal percentage (ex., 0.50).
	 * 
	 * @exception IllegalArgumentException Thrown if the file does not contain a "Percentage" field.
	 */
	public static double parseHydroxylPercentage(String fileName) throws IOException, IllegalArgumentException {
		CSVReader reader = null;
		
		try {
			// Open the file
			reader = new CSVReader(new FileReader(fileName));
			
			// Read until we find the value, or the "Name" marker for the chemicals listing
			String[] entries;
			while ((entries = reader.readNext()) != null) {
				// Match found
				if (entries[0].toUpperCase().equals("PERCENTAGE")) {
					return Double.parseDouble(entries[1]);
				}
				
				// Start of chemicals found
				if (entries[0].toUpperCase().equals("NAME")) {
					throw new IllegalArgumentException("File does not appear to contain 'percentage' entry.");
				}
			}
			
			throw new IOException("Invalid ChemSim chemicals file.");
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	/**
	 * Parse the volume of the experiment from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return The value in the units from the file, assumed to be liters.
	 */
	public static double parseVolume(String fileName) throws IOException {
		CSVReader reader = null;

		try {
			// Open the file
			reader = new CSVReader(new FileReader(fileName));

			// First entry should be the volume
			String[] entries = reader.readNext();
			if (!entries[0].toUpperCase().equals("VOLUME")) {
				System.err.println("File provided does not contain the volume on line one.");
				throw new IOException("Invalid ChemSim chemicals file.");
			}
			
			// Return the result
			return Double.parseDouble(entries[1]);
		} finally {
			if (reader != null)	reader.close();
		}
	}
	
	/**
	 * Read the reactions from the file indicated.
	 * 
	 * @param fileName The full path to the file.
	 * @return A list of reactions.
	 */
	public static List<ChemicalEquation> parseReactions(String fileName) throws IOException {
		CSVReader reader = null;

		try {
			// Read the header to note the number of items
			reader = new CSVReader(new FileReader(fileName));
			String[] entries = reader.readNext();
			int reactants = 0;
			while (entries[reactants].toUpperCase().equals("REACTANT")) {
				reactants++;
			}
			int products = 0;
			while (entries[reactants + products].toUpperCase().equals("PRODUCT")) {
				products++;
			}

			// Check to make sure the k column is present
			int k = reactants + products;
			if (!entries[k].toUpperCase().equals("K")) {
				System.err.println("File provided does not contain 'k' header at index " + (k + 1));
				throw new IOException("Invalid ChemSim reactions file.");
			}

			// Next column may be either the ratios, pKa, or nothing. However, the order
			// of k, ratios, pKa is enforced for the sake of consistency.
			int pKa = -1, ratio = -1;
			String value = entries[k + 1].toUpperCase();
			if (value.equals("RATIO")) {
				ratio = k + 1;
			} else if (value.equals("PKA")) {
				pKa = k + 1;
			}
			if (entries.length > k + 2) {
				value = entries[k + 2].toUpperCase();
				if (value.equals("PKA")) {
					pKa = k + 2;
				} else if (!value.isEmpty()) {
					System.err.println("Invalid header at column index " + (k + 2) + " value, '" + value + "'");
					throw new IOException("Invalid ChemSim reactions file.");
				}
			}

			// We know the headers, now parse out the actual reactions
			List<ChemicalEquation> results = new ArrayList<ChemicalEquation>();
			while ((entries = reader.readNext()) != null) {
				// Should we skip this line? 
				if (entries[0].startsWith("#") || entries[0].isEmpty()) { continue; }

				// Process the reactants
				List<String> reactant = new ArrayList<String>();
				for (int ndx = 0; ndx < reactants; ndx++) {
					if (!entries[ndx].isEmpty()) {
						reactant.add(entries[ndx].trim());
					}
				}

				// Process the products
				List<String> product = new ArrayList<String>();
				for (int ndx = 0; ndx < products; ndx++) {
					product.addAll(parseProduct(entries[reactants + ndx].trim()));
				}

				// Check to see if pKa is set, if so this is a acid dissociation
				if (pKa != -1 && !entries[pKa].isEmpty()) {
					results.add(new AcidDissociation(reactant, product, Double.parseDouble(entries[pKa])));
					continue;
				}

				// Basic reaction, finish parsing it out
				String kString = entries[k].isEmpty() ? "0" : entries[k]; 
				if (ratio != -1) {
					String ratioString = entries[ratio].isEmpty() ? "1" : entries[ratio];
					results.add(new BasicReaction(reactant, product, Double.parseDouble(kString), Double.parseDouble(ratioString)));
				} else {
					results.add(new BasicReaction(reactant, product, Double.parseDouble(kString)));
				}
			}

			return results;
		} finally {
			if (reader != null) { reader.close(); }
		}
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
