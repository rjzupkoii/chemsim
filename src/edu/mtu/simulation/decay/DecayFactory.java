package edu.mtu.simulation.decay;

import java.io.IOException;

import edu.mtu.simulation.ModelProperities;
import edu.mtu.simulation.SimulationProperties;

public class DecayFactory {
		
	/**
	 * Create the decay model that should be used, resulting model will be injected into
	 * the ModelProperties object provided.
	 */
	public static void createDecayModel(ModelProperities properties) throws IOException {
		// This is a bit of a stub from a dead-end path, the code
		// is being kept around since the factory is a useful point
		// to expand from.
		initializePhotolysisDecay(properties);
		
		int time = properties.getTimeSteps();
		int padding = SimulationProperties.getInstance().getPadding();
		padding = (int)(padding * (60 / SimulationProperties.getInstance().getTimeStepLength()));
		System.out.println("Estimated running time of " + (time - padding) + " time steps, padded to " + time);
	}
		
	/**
	 * Initialize the model so that photolyiss follows linear decay.
	 */
	private static void initializePhotolysisDecay(ModelProperities properties) throws IOException {
		Photolysis decay = new Photolysis();
		String fileName = SimulationProperties.getInstance().getChemicalsFileName();
		decay.prepare(fileName);
		properties.setDecayModel(decay);
		
		int padding = SimulationProperties.getInstance().getPadding();
		int time = (int)(decay.estimateRunningTime() + padding * (SimulationProperties.getInstance().getTimeStepLength() / 60));
		properties.setTimeSteps(time);
	}
}
