package edu.mtu.simulation.decay;

import java.io.IOException;

import edu.mtu.simulation.ModelProperities;
import edu.mtu.simulation.SimulationProperties;

public class DecayFactory {
	
	// Padding to add to the time steps to act as a buffer
	private static final int PADDING = 250;
	
	/**
	 * Create the decay model that should be used, resulting model will be injected into
	 * the ModelProperties object provided.
	 */
	public static void createDecayModel(ModelProperities properties) throws IOException {
		if (SimulationProperties.getInstance().getExperimentalDecay()) {
			initializeExperimentalDecay(properties);
		} else {
			initializePhotolysisDecay(properties);
		}
		
		System.out.println("Estimated running time of " + (properties.getTimeSteps() - PADDING) +  
				" time steps, padded to " + properties.getTimeSteps());
	}
	
	/**
	 * Initialize the model so that photolysis follows experimental results.
	 */
	private static void initializeExperimentalDecay(ModelProperities properties) throws IOException {
		// Prepare experimentally based decay, note that it must be initialized
		ExperimentalDecay decay = new ExperimentalDecay();
		String fileName = SimulationProperties.getInstance().getExperimentalDataFileName();
		decay.prepare(fileName);
		decay.initialize();
		properties.setDecayModel(decay);
		properties.setTimeSteps(decay.estimateRunningTime() + PADDING);
		
		System.out.println("Using experimental decay data.");
	}
	
	/**
	 * Initialize the model so that photolyiss follows linear decay.
	 */
	private static void initializePhotolysisDecay(ModelProperities properties) throws IOException {
		Photolysis decay = new Photolysis();
		String fileName = SimulationProperties.getInstance().getChemicalsFileName();
		decay.prepare(fileName);
		properties.setDecayModel(decay);
		properties.setTimeSteps(decay.estimateRunningTime() + PADDING);
	}
}
