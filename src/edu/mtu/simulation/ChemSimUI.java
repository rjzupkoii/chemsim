package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import edu.mtu.compound.DisproportionatingSpecies;
import edu.mtu.compound.Species;
import edu.mtu.simulation.chart.HydroxylRadicalChart;
import sim.display.Controller;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal3d.grid.SparseGridPortrayal3D;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;

public class ChemSimUI extends GUIState {

	private Display3D display;
	private JFrame displayFrame;
	private JFrame hydroxylRadicalChartFrame;
	
	private SparseGridPortrayal3D compoundPortrayal = new SparseGridPortrayal3D();
	private WireFrameBoxPortrayal3D wireFramePortrayal;
		
	private HydroxylRadicalChart hydroxylRadicalChart = new HydroxylRadicalChart();
	
	/**
	 * Constructor.
	 */
	public ChemSimUI() {
		super(new ChemSim(System.currentTimeMillis()));
	}
	
	/**
	 * Constructor.
	 */
	public ChemSimUI(SimState state) {
		super(state);
	}
	
	/**
	 * Prepare a model inspector for the UI. 
	 */
	@Override
	public Inspector getInspector() {
		Inspector inspector = super.getInspector();
		inspector.setVolatile(true);
		return inspector;
	}
	
	/**
	 * Get a state object for the UI.
	 */
	@Override
	public Object getSimulationInspectedObject() { 
		return ChemSim.getProperties();
	}
		
	/**
	 * Initialize the display for rendering.
	 */
	@Override
	public void init(Controller controller) {
		super.init(controller);
		
		// TODO Come up with another way of displaying the status
//		// Prepare the display and the wire-frame
//		display = new Display3D(600, 600, this);
//		display.setBackdrop(Color.WHITE);
//		wireFramePortrayal = new WireFrameBoxPortrayal3D(-0.5, -0.5, -0.5, ChemSim.GridWidth, ChemSim.GridHeight, ChemSim.GridLength, Color.BLUE);
//		
//		// Attach the portrayals
//		display.attach(wireFramePortrayal, "Wire Frame");
//		display.attach(compoundPortrayal, "Compounds");
//		
//		// Make sure things are scaled correctly
//		display.translate(-ChemSim.GridWidth / 2.0, -ChemSim.GridHeight / 2.0, -ChemSim.GridLength / 2.0);
//		display.scale(1.0 / ChemSim.GridWidth);
//		
//		// Register and display the frame
//		displayFrame = display.createFrame();
//		controller.registerFrame(displayFrame);
//		displayFrame.setVisible(true);
		
		// Register and display the hydroxyl radical chart
		hydroxylRadicalChartFrame = hydroxylRadicalChart.createFrame();
		controller.registerFrame(hydroxylRadicalChartFrame);
		hydroxylRadicalChartFrame.setVisible(true); 
	}

	/**
	 * Load the simulation.
	 */
	@Override
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}
	
	/**
	 * Ensure that the simulation cleans up correctly.
	 */
	@Override
	public void quit() {
		super.quit();
		if (hydroxylRadicalChartFrame != null) { hydroxylRadicalChartFrame.dispose(); }
		if (displayFrame != null) { displayFrame.dispose(); }
		displayFrame = null;
		display = null;
	}
	
	/**
	 * Start the simulation.
	 */
	@Override
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	/**
	 * Setup the presentation of the simulation.
	 */
	private void setupPortrayals() {
		try {
//			// Set the portrayals of the compounds
//			Species reference = new Species("H2O2");
//			compoundPortrayal.setField(((ChemSim)state).getMolecules());
//			compoundPortrayal.setPortrayalForClass(Species.class, new CompoundPortrayal(reference));
//			compoundPortrayal.setPortrayalForClass(DisproportionatingSpecies.class, new CompoundPortrayal(DisproportionatingSpecies.create(reference, null)));
//			
//			// Make sure the display is scheduled correctly
//			display.createSceneGraph();
//			display.reset();
			
			// Add the hydroxyl radical chart
			this.scheduleRepeatingImmediatelyAfter(hydroxylRadicalChart);
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		new ChemSimUI().createController();
	}
}
