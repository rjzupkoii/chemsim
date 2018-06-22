package edu.mtu.simulation.decay;

import java.io.IOException;
import java.util.List;

import edu.mtu.parser.ChemicalDto;
import edu.mtu.parser.Parser;
import edu.mtu.simulation.ChemSim;

/**
 * This class provides a model for photolysis based upon
 * a single decay rate for the whole experiment.
 */
public class Photolysis implements DecayModel {

	private int time;
	private long m;
	private long b;
	
	@Override
	public int estimateRunningTime() {
		return time;
	}

	@Override
	public long getDecayQuantity(int timeStep, String compound, long moleclues) {
		// TODO Adjust this for multiple compounds
		
		long y = m * timeStep + b;
		long quantity = moleclues - y;
		return quantity;
	}

	/**
	 * Prepare for photolysis on the basis of the slope provided. Note that we
	 * are assuming that the slope is being given as mM/L and will need to be
	 * adjusted for the reactor.
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
		
		// Prepare the adjustment for mM/L to mols/reactor
		double adjustment = volume * 0.001;
		
		// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
		// this means we need to determine the odds that any individual 
		// hydrogen peroxide agent will be removed each time step based upon
		// the new population which requires us knowing the initial decay
		m = (int)Math.ceil(rate * adjustment * scaling);
		
		// If a b is supplied, then convert it to model units and use it
		double intercept = Parser.parseIntercept(fileName);
		if (intercept != Parser.INVALID_ENTRY_VALUE) {
			// Convert to model units
			b = (int)Math.ceil(Math.abs(intercept * adjustment * scaling));
		} else {
			b = count;
		}
			
		// Time when y = 0
		time = (int)Math.abs(-b / m);
		
		// Note the estimated decay
		System.out.println("H2O2 photolysis decay rate: " + m + " moleclules/timestep");
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
