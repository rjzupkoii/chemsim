package edu.mtu.simulation.decay;

import java.io.IOException;
import java.util.List;

import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.ChemSim;
import edu.mtu.simulation.SimulationProperties;

/**
 * This class provides a model for photolysis based upon
 * a single decay rate for the whole experiment.
 */
public class Photolysis implements DecayModel {

	// TODO Move this in to the simulation code
	private final static int targetTime = 700;
	
	private int time;
	private double m;
	private long b;
	
	@Override
	public int estimateRunningTime() {
		// Is this minutes?
		if (time < targetTime) {
			return targetTime;
		}
		// Must be seconds
		return targetTime * 60;
	}

	@Override
	public double getDecayQuantity(int timeStep, String compound, long moleclues) {
		// TODO Adjust this for multiple compounds
		
		double y = m * timeStep + b;
		return moleclues - y;
	}

	/**
	 * Prepare for photolysis on the basis of the slope provided. Note that we
	 * are assuming that the slope is being given as mM/L/min and will need to 
	 * be adjusted for the reactor and the model time span.
	 */
	@Override
	public void prepare(String fileName) throws IOException {
		// TODO Adjust this for multiple compounds
		
//		ChemicalDto chemical = getCompound("H2O2", fileName);

		// Get the current count of H2O2
		long count = ChemSim.getTracker().getCount("H2O2");
		double rate = Parser.parseRate(fileName);
		double volume = Parser.parseVolume(fileName);

		// Note the scaling factor for mols to molecules
		double scaling = ChemSim.getProperties().getMoleculeToMol();
				
		// Note the current time step duration
		double timeStep = SimulationProperties.getInstance().getTimeStepLength();
		
		// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
		// this means we need to determine the odds that any individual 
		// hydrogen peroxide agent will be removed each time step based upon
		// the new population which requires us knowing the initial decay
		m = (rate * volume * 0.001 * scaling * timeStep) / 60;
		m = Math.round(m * 100.0) / 100.0;
		if (m == 0) {
			throw new IllegalStateException("Calculated decay is zero, adjust inputs.");
		}
		
		// If a b is supplied, then convert it to model units and use it
		double intercept = Parser.parseIntercept(fileName);
		if (intercept != Parser.INVALID_ENTRY_VALUE) {
			// Convert to model units
			b = (int)Math.ceil(Math.abs(intercept * volume * 0.001 * scaling));
		} else {
			b = count;
		}
			
		// Time when y = 0
		time = (int)Math.abs(-b / m);
		
		// Note the estimated decay
		System.out.println("H2O2 photolysis decay rate: " + m + " molecules/timestep");
	}
	
	// TODO Update this form more moleclues
	private ChemicalDto getCompound(String compound, String fileName) throws IOException {
		List<ChemicalDto> compounds = Parser.parseChemicals(fileName);
		for (ChemicalDto dto : compounds) {
			if (dto.formula.equals(compound)) {
				return dto;
			}
		}
		throw new IllegalArgumentException("Compound, '" + compound + "' not found.");
	}
}
