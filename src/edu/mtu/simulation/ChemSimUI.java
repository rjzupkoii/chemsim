package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import sim.display.Controller;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal3d.grid.SparseGridPortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;

public class ChemSimUI extends GUIState {

	private Display3D display;
	private JFrame displayFrame;
	
	private SparseGridPortrayal3D compoundPortrayal = new SparseGridPortrayal3D();
	private WireFrameBoxPortrayal3D wireFramePortrayal;
	
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
		
		// Prepare the display and the wire-frame
		display = new Display3D(600, 600, this);
		wireFramePortrayal = new WireFrameBoxPortrayal3D(-0.5, -0.5, -0.5, ChemSim.GridWidth, ChemSim.GridHeight, ChemSim.GridLength, Color.BLUE);
		
		// Attach the portrayals
		display.attach(wireFramePortrayal, "Wire Frame");
		display.attach(compoundPortrayal, "Compounds");
		
		// Make sure things are scaled correctly
		display.translate(-ChemSim.GridWidth / 2.0, -ChemSim.GridHeight / 2.0, -ChemSim.GridLength / 2.0);
		display.scale(1.0 / ChemSim.GridWidth);
		
		// Register and display the frame
		display.setBackdrop(Color.WHITE);
		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupPortrayals() {
		// Set the portrayals of the compounds
		compoundPortrayal.setField(((ChemSim)state).getCompounds());
		try {
			ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			for (ClassInfo classInfo : classPath.getTopLevelClassesRecursive("edu.mtu.compound")) {
				Class compound = Class.forName(classInfo.getName());
				Color color = (Color)compound.getMethod("getColor", (Class[])null).invoke(null, (Object[])null);
				compoundPortrayal.setPortrayalForClass(compound, new SpherePortrayal3D(color));
			}
		} catch (Exception ex) {
			// We can't recover from errors here
			ex.printStackTrace();
			System.exit(1);
		}
		
		// Make sure the display is scheduled correctly
		display.reset();
		display.createSceneGraph();
	}

	/**
	 * Main entry point for non-UI model.
	 */
	public static void main(String[] args) {
		new ChemSimUI().createController();
	}
}
