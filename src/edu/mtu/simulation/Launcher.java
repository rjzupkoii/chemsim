package edu.mtu.simulation;

import java.io.FileOutputStream;
import java.io.IOException;

import edu.mtu.system.EchoStream;
import net.sourceforge.sizeof.SizeOf;

public final class Launcher {
	/**
	 * Main entry point for the simulation.
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {

		// Echo to the console file
		FileOutputStream out = new FileOutputStream("console.txt");
		EchoStream echo = new EchoStream(out);
		System.setOut(echo);
		
		// Configure SizeOf, note that the program MUST be invoked with -javaagent:lib/SizeOf.jar
		SizeOf.skipStaticField(true);
		SizeOf.setMinSizeToLog(10);

		// Load the arguments
		ParseArguments(args);
		SimulationProperties properties = SimulationProperties.getInstance();
		if (properties.getChemicalsFileName().equals("")) {
			System.err.println("Chemicals file not provided!");
			System.exit(-1);
		}
		if (properties.getReactionsFileName().equals("")) {
			System.err.println("Reactions file not provided!");
			System.exit(-1);
		}
		
		// Initialize the simulation
		long seed = System.currentTimeMillis();
		ChemSim instance = ChemSim.getInstance();
		instance.initialize(seed);
				
		// Run the simulation and exit
		long timeSteps = ChemSim.getProperties().getTimeSteps();
		instance.start(timeSteps);
	}

	private static void ParseArguments(String[] args) {
		SimulationProperties properties = SimulationProperties.getInstance();
		String iteration = "";
		
		// Parse out the arguments
		for (int ndx = 0; ndx < args.length; ndx+=2) {
			if (args[ndx].equals("-chemicals")) {
				properties.setChemicalsFileName(args[ndx + 1]);
			} else if (args[ndx].equals("-reactions")) {
				properties.setReactionsFileName(args[ndx + 1]);
			} else if (args[ndx].equals("-run")) {
				iteration = "-" + args[ndx + 1];
			} else {
				System.err.println("Unknown argument, " + args[ndx]);
				System.exit(-1);
			}
		}
		
		// Apply the settings
		properties.setResultsFileName(String.format(properties.getResultsFileName(), iteration));
	}
}
