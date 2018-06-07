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
	private long quantity;
	
	@Override
	public int estimateRunningTime() {
		return time;
	}
	
	@Override
	public long getDecayQuantity(int timeStep, String compound, long moleclues) {
		// TODO Adjust this for multiple compounds
		return quantity;
	}

	@Override
	public void prepare(String fileName) throws IOException {
		// TODO Adjust this for multiple compounds
		
		// Get the current count of H2O2
		ChemicalDto chemical = getCompound("H2O2", fileName);
		long count = ChemSim.getTracker().getCount("H2O2");
		double rate = Parser.parseRate(fileName);
		double volume = Parser.parseVolume(fileName);
		
		// Hydrogen peroxide is a linear decay, or f(x) = C - r * t 
		// this means we need to determine the odds that any individual 
		// hydrogen peroxide agent will be removed each time step based upon
		// the new population which requires us knowing the initial decay
		quantity = (int)Math.ceil(Math.abs((count * rate * volume) / chemical.mols)) * ChemSim.SCALING;
		
		// Since we know the decay rate we can calculate the running time
		time = (int)(count / quantity);
	}
	
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
