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
// TODO Fix decay to adjust based upon the time step duration
public class ExperimentalDecay implements DecayModel {

	private static final int TimeIndex = 0;
	
	private double volume;
	private int maxTimeStep;
	private Map<String, Long> calculated;
	private Map<Integer, Map<String, Double>> data;
	
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
	public Map<Integer, Map<String, Double>> getData() {
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
			double decay = data.get(timeStep).get(compound);
			long count = (long)Math.abs(Math.ceil(decay));
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
			data = new HashMap<Integer, Map<String, Double>>();
			
			// Note the scaling
			double scaling = (ChemSim.getProperties().getMoleculeToMol() * volume * 0.001);
			
			// Load the entries
			String[] previous = current;			
			while ((current = reader.readNext()) != null) {
				// Note the time, create the entry
				int time = Integer.parseInt(previous[TimeIndex]);				
				data.put(time, new HashMap<String, Double>());
				
				// Calculate the slope as a unit of molecules in the reactor - m = ((scaling * volume * 0.001) * (y2 - y1)) / (x2 - x1)
				for (int ndx = 1; ndx < current.length; ndx++) {
					double rise = scaling * (Double.parseDouble(current[ndx]) - Double.parseDouble(previous[ndx]));
					double run = Double.parseDouble(current[TimeIndex]) - Double.parseDouble(previous[TimeIndex]);
					double slope = rise / run;
					data.get(time).put(header[ndx], slope);
				}
				previous = current;
			}
			
			// Attempt to find the last time point
			estimateMax(previous, header);
			
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	/**
	 * Estimate the final decay point in the model based upon when the last experimental result should decay.
	 */
	private void estimateMax(String[] enteries, String[] header) {
		// Note the last calculated time entry
		int lastTime = 0;
		for (int value : data.keySet()) {
			if (value > lastTime) {
				lastTime = value;
			}
		}

		// Note the time of the entry
		int entryTime = Integer.parseInt(enteries[TimeIndex]);
		maxTimeStep = entryTime;

		// Note the scaling
		double scaling = (ChemSim.getProperties().getMoleculeToMol() * volume * 0.001);
		
		// Attempt to find the last time step
		for (int ndx = 1; ndx < enteries.length; ndx++) {
			double molecules = Double.parseDouble(enteries[ndx]) * scaling;
			double slope = data.get(lastTime).get(header[ndx]);
			int steps = (int)Math.ceil(Math.abs(molecules / slope));
			if (entryTime + steps > maxTimeStep) {
				maxTimeStep = entryTime + steps;
			}
		}
	}
}
