package edu.mtu.simulation;

import javax.swing.JFrame;

import edu.mtu.simulation.chart.HydroxylRadicalChart;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;

public class ChemSimUI extends GUIState {

	private JFrame hydroxylRadicalChartFrame;
			
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
