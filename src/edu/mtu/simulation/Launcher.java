package edu.mtu.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.mtu.reaction.Verifier;
import edu.mtu.simulation.tracking.Converter;
import edu.mtu.system.EchoStream;
import net.sourceforge.sizeof.SizeOf;

public final class Launcher {
	
	/**
	 * Main entry point for the simulation.
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {

		// Echo to the console file 
		String filename = "console.txt";
		if (args.length > 0 && args[0].equals("-timestamp")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.SSSZ");
			filename = String.format("console.%s.txt", formatter.format(new Date()));
		}
		FileOutputStream out = new FileOutputStream(filename);
		EchoStream echo = new EchoStream(out);
		System.setOut(echo);
		System.setErr(echo);
		
		// Configure SizeOf, note that the program MUST be invoked with -javaagent:lib/SizeOf.jar
		SizeOf.skipStaticField(true);
		SizeOf.setMinSizeToLog(10);

		try {
			// Load the arguments
			ParseArguments(args);
		} catch (Exception ex) {
			// This might get better, but for now just echo and exit
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
		SimulationProperties properties = SimulationProperties.getInstance();
		if (properties.getChemicalsFileName().equals("")) {
			System.err.println("Chemicals file not provided!");
			System.exit(-1);
		}
		if (properties.getReactionsFileName().equals("")) {
			System.err.println("Reactions file not provided!");
			System.exit(-1);
		}
		
		// Set the shutdown hook so we can gracefully cleanup
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
		// Initialize the simulation
		long seed = System.currentTimeMillis();
		ChemSim instance = ChemSim.getInstance();
		instance.initialize(seed);
				
		try {
			// Run the simulation and exit
			int timeSteps = ChemSim.getProperties().getTimeSteps();
			instance.start(timeSteps);
		} catch (OutOfMemoryError ex) {
			// Attempt to gracefully fail when we run out of memory
			System.err.println("Ran out of memory while executing the model!");
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			
			// Still format the mols though
			System.err.println("Results and molar files saved.");
			String moleculear = properties.getResultsFileName();
			String mols = properties.getMolarFileName();
			Converter.Convert(moleculear, mols, ChemSim.getProperties().getMoleculeToMol());
		}
	}

	private static void ParseArguments(String[] args) {
		boolean chemicals = false, reactions = false;
		
		ArrayList<String> terminateOn = new ArrayList<String>();
		
		SimulationProperties properties = SimulationProperties.getInstance();
		String iteration = "";
		
		// Parse out the arguments
		for (int ndx = 0; ndx < args.length; ndx+=2) {
			switch(args[ndx]) {
			case "-c":
			case "--chemicals":
				properties.setChemicalsFileName(args[ndx + 1]);
				chemicals = true;
				break;
			case "-dt":
				double value = Double.parseDouble(args[ndx + 1]);
				properties.setDeltaT(value);
				break;
			case "-r":
			case "--reactions":
				properties.setReactionsFileName(args[ndx + 1]);
				reactions = true;
				break;
			case "-n":
			case "--run":
				iteration = "-" + args[ndx + 1];
				break;
			case "-p":
			case "--padding":
				properties.setPadding(Integer.parseInt(args[ndx + 1]));
				break;
			case "-l":
			case "--limit":
				int limit = Double.valueOf(args[ndx + 1]).intValue();
				properties.setInitialMolecules(limit);
				break;
			case "-t":
			case "--terminate":
				terminateOn.add(args[ndx + 1]);
				break;
			case "-v":
			case "--verify":
				Verifier.verify(args[ndx + 1], args[ndx + 2]);
				System.exit(0);
			case "-w":
			case "--write":
				properties.setReportInterval(Integer.parseInt(args[ndx + 1]));
				break;
			default:
				System.err.println("Unknown argument, " + args[ndx]);
				printUsage();
				System.exit(-1);				
			}
		}
		
		// Make sure we have the parameters to run
		if (!(chemicals && reactions)) {
			printUsage();
			System.exit(-1);
		}
		
		// Apply the settings
		if (terminateOn.size() > 0) {
			String[] value = new String[terminateOn.size()];
			value = terminateOn.toArray(value);
			properties.setTerminateOn(value);
		}
		properties.setMolarFileName(String.format(properties.getMolarFileName(), iteration));
		properties.setResultsFileName(String.format(properties.getResultsFileName(), iteration));
	}
	
	private static void printUsage() {
		String format = "%-25s %s\n";
		
		System.err.println("Usage: [ChemSim] [Parameters]");
		System.err.println("\nRequired:");
		System.err.printf(format, "-c, --chemicals [file]", "CSV file with compounds present at start of experiment");
		System.err.printf(format, "-r, --reactions [file]", "CSV file with reactions to be modeled");
		System.err.println("\nOptional: ");
		System.err.printf(format, "", "-dt [number]", "The delta T in seconds, default 1");
		System.err.printf(format, "-l, --limit [number]", "The maximum number of molecules to generate at initlization.");
		System.err.printf(format, "-n, --run [number]", "The run number to apply to results files");
		System.err.printf(format, "-p, --padding [number]", "The number of seconds to pad the estimated time by, default 900 seconds");
		System.err.printf(format, "-w, --write [number]", "The report interval to print / save status on, default 60 iterations");
		System.err.printf(format, "-t, --terminate [formula]", "Terminate the model when the given molecule has zero entities");
		System.err.printf(format, "-v, --verify [file] [file]", "Verify the [reactions] and [chemicals] files are properly formatted and has balanced reactions.");
		System.err.println("\nNOTE:");
		System.err.println("JAVAGENT initialization is required, -javaagent:lib/SizeOf.jar");
	}
	
	private static class ShutdownHook extends Thread {
		@Override
		public void run() {
			if (ChemSim.getSchedule().stopped()) {
				return;
			}
			System.err.println("Hard shutdown hook noted, terminating simulation!");
			ChemSim.getSchedule().halt();
		}
	}
}
