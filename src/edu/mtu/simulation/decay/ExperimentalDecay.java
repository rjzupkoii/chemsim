package edu.mtu.simulation.decay;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import edu.mtu.simulation.ChemSim;

/**
 * This class provides a model for photolysis based upon experimentally 
 * observed data.  
 */
public class ExperimentalDecay implements DecayModel {

	private static final int TimeIndex = 0;
	
	private double volume;
	private int maxTimeStep;
	private Map<String, Long> calculated;
	private Map<Integer, Map<String, DecayDto>> data;
	
	/**
	 * Estimate how long the model needs to run for.
	 * 
	 * @return The estimated number of time steps.
	 */
	public int estimateRunningTime() {
		return maxTimeStep;
	}
	
	/**
	 * Get the experimental data stored by the decay model.
	 */
	public Map<Integer, Map<String, DecayDto>> getData() {
		return data;
	}
	
	/**
	 * Get the volume loaded by the decay model.
	 */
	public double getVolume() {
		return volume;
	}
	
	/**
	 * Get the decay quantity for the current time step based upon experimental results.
	 * 
	 * @param timeStep The current time step of the model.
	 * @param compound to get the decay quantity for.
	 * @param moleclues The current number of molecules.
	 * @return The number of the molecules that should decay.
	 */
	public long getDecayQuantity(int timeStep, String compound, long moleclues) {
		// Return zero if there are no molecules
		if (moleclues == 0) {
			return 0;
		}		
		
		// Check to see if we need to update the calculation
		if (data.containsKey(timeStep)) {
			if (!data.get(timeStep).containsKey(compound)) {
				String message = String.format("The compound '%s' does not have decay data for time step %d", compound, timeStep);
				throw new IllegalArgumentException(message);
			}
			DecayDto dto = data.get(timeStep).get(compound);
						
			// Calculate out the number of molecules, p_q  = (molecules * slope * volume) / mols
			long count = (int)Math.ceil(Math.abs((moleclues * dto.slope * volume) / dto.mols));
			calculated.put(compound, count);
		}
		
		return calculated.get(compound);
	}
	
	/**
	 * Iterates though the compounds loaded and performs a calculation at time point zero.
	 */
	public void initialize() {
		calculated = new HashMap<String, Long>();
		for (String compound : data.get(0).keySet()) {
			getDecayQuantity(0, compound, ChemSim.getTracker().getCount(compound));
		}
	}
		
	/**
	 * Prepare the decay model by loading the experimental results, 
	 * and then calculating out the slope and decay rate in molecules.
	 * 
	 * @param fileName to load the data from.
	 */
	public void prepare(String fileName) throws IOException {
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(new FileReader(fileName));
			
			// First row should be the volume of the reactor
			String[] entries = reader.readNext();
			if (!entries[0].toUpperCase().equals("VOLUME")) {
				System.err.println("File provided does not contain the volume on line one.");
				throw new IOException("Invalid ChemSim experimental data file.");
			}
			volume = Double.parseDouble(entries[1]);
			
			// Second row should be the header, check that the columns look OK
			// This also ensures that we can assume minutes for the time points
			String[] header = reader.readNext();
			if (!header[0].toUpperCase().equals("MIN")) {
				System.err.println("File provided does not contain the experimental data header.");
				throw new IOException("Invalid ChemSim experimental data file.");
			}
			
			// Prepare the data storage
			String[] current = reader.readNext();
			data = new HashMap<Integer, Map<String,DecayDto>>();
			
			// Load the entries
			String[] previous = current;			
			while ((current = reader.readNext()) != null) {
				// Note the time and update the max if need be
				int time = Integer.parseInt(previous[TimeIndex]);
				if (time > maxTimeStep) {
					maxTimeStep = time;
				}
				
				data.put(time, new HashMap<String, DecayDto>());
				for (int ndx = 1; ndx < current.length; ndx++) {
					DecayDto dto = new DecayDto();
					
					// Calculate the slope, m = (y2 - y1) / (x2 - x1)
					double rise = Double.parseDouble(current[ndx]) - Double.parseDouble(previous[ndx]);
					double run = Double.parseDouble(current[TimeIndex]) - Double.parseDouble(previous[TimeIndex]);
					dto.slope = rise / run;

					// Calculate the mols, mols = value * volume * 1000
					dto.mols = (Double.parseDouble(previous[ndx]) / 1000) * volume;
					
					// Store the slope and mols for later calculation
					data.get(time).put(header[ndx], dto);
				}
				previous = current;
			}
		} finally {
			if (reader != null) reader.close();
		}
	}
}
