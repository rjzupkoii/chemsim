package edu.mtu.simulation.tracking;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Read the molecular counts written by ChemSim and write then as molar values.
 */
public class Converter {
	/**
	 * Read the molecular count and write the molar concentration to the destination file.
	 * 
	 * @param source csv file to read molecular counts from.
	 * @param destintation file to write molar counts to.
	 * @param scaling factor to be applied.
	 */
	public static void Convert(String source, String destintation, double scaling) {
		CSVReader reader = null;
		BufferedCsvWriter writer = null;
		
		try {
			// Open the files
			reader = new CSVReader(new FileReader(source));
			writer = new BufferedCsvWriter(destintation);
			
			// Read and echo the first two lines
			writer.write(reader.readNext());
			writer.write(reader.readNext());
			
			// Read, convert, and write the enteries
			String[] entries;
			while ((entries = reader.readNext()) != null) {
				for (String entry : entries) {
					if (entry.isEmpty()) {
						continue;
					}
					double value = Double.parseDouble(entry) / scaling;
					writer.write(value);					
				}
				writer.newline();
				writer.flush();
			}

			// Clean-up
			writer.close();
			reader.close();
			
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(-1);
		} 
	}
}
