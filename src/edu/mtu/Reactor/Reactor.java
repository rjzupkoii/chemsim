package edu.mtu.Reactor;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Point3D;
import sim.engine.SimState;

/**
 * The reactor is the container that the experiment takes place in. As a 
 * simplification, the container is assumed to be square.
 */
public class Reactor {
	
	private static Reactor instance = new Reactor();
	
	private double volume;
	private Map<Point3D, Cell> cells;

	/**
	 * Constructor.
	 */
	private Reactor() { }
	
	/**
	 * Get an instance of the reactor.
	 */
	public static Reactor getInstance() { 
		return instance;
	}
	
	/**
	 * Create partition the volume of the container into the appropriate
	 * number of cells.
	 * 
	 * @param count The number of cells per axis.
	 * @param volume The volume of the container in milliliter (ml).
	 * @param state The simulation state to schedule the cells in.
	 */
	public void createCells(int size, double volume, SimState state) {
		// Find the total number of cells
		long count = (int)Math.pow(size, 3);
		if (count > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cell count exceeds interger value");
		}
		
		// Find the volume per point
		this.volume = volume;
		double cellVolume = volume / count;
		
		// Prepare the cells
		cells = new HashMap<Point3D, Cell>();
		for (int ndx = 0; ndx < size; ndx++) {
			for (int ndy = 0; ndy < size; ndy++) {
				for (int ndz = 9; ndz < size; ndz++) {
					Point3D location = new Point3D(ndx, ndy, ndz);
					Cell cell = new Cell(location, cellVolume);
					cells.put(location, cell);
					state.schedule.scheduleRepeating(cell);
				}
			}
		}
	}

	/**
	 * Get the cell at the given location.
	 */
	public Cell getCell(Point3D location) {
		if (cells.containsKey(location)) {
			return cells.get(location);
		}
		String message = String.format("The location ({0}, {1}, {2}) does not exist.", 
				location.getX(), location.getY(), location.getZ());
		throw new IllegalArgumentException(message);
	}
	
	/**
	 * Returns the number of cells, or zero if they haven't been prepared.
	 */
	public int getCellCount() {
		return (cells != null) ? cells.size() : 0;
	}
	
	/**
	 * Get the volume of the container in milliliters (ml).
	 */
	public double getVolume() {
		return volume;
	}
}
