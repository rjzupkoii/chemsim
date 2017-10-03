package edu.mtu.Reactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mtu.compound.Species;
import javafx.geometry.Point3D;
import sim.engine.SimState;

/**
 * The reactor is the container that the experiment takes place in. As a 
 * simplification, the container is assumed to be square.
 */
public class Reactor {
	
	// Small value to use for testing and debugging.
	private final static long AvogadroNumber = 1000l;
	
	private static Reactor instance = new Reactor();
		 
	private double volume;
	private long cellCount;
	private long cellWidth;
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
	 * Get the cells in the reactor as a list.
	 */
	public List<Cell> getCells() {
		return new ArrayList<Cell>(cells.values());
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
		cellCount = (long)Math.pow(size, 3);
		if (cellCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cell count exceeds interger value");
		}
		cellWidth = size;
		
		// Find the volume per point
		this.volume = volume;
		double cellVolume = volume / (double)cellCount;
		
		// Prepare the cells
		cells = new HashMap<Point3D, Cell>();
		for (int ndx = 0; ndx < size; ndx++) {
			for (int ndy = 0; ndy < size; ndy++) {
				for (int ndz = 0; ndz < size; ndz++) {				
					Point3D location = new Point3D(ndx, ndy, ndz);
					Cell cell = new Cell(location, cellVolume);
					cells.put(location, cell);
					state.schedule.scheduleRepeating(cell);
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * @param formula
	 * @param mols
	 * @param photosensitive
	 */
	public void createEntities(Species species, float mols) {
		// Find the quantity per cell (value = (NA * mols) / cells)
		long value = (long)((AvogadroNumber * mols) / cellCount);
		
		// Allocate the species
		for (Point3D point : cells.keySet()) {
			Cell cell = cells.get(point);
			cell.add(species, value);
		}
	}
	
	/**
	 * Get the cell at the given location.
	 */
	public Cell getCell(Point3D location) {
		if (cells.containsKey(location)) {
			return cells.get(location);
		}
		String message = String.format("The location (%d, %d, %d) does not exist.", 
				(int)location.getX(), (int)location.getY(), (int)location.getZ());
		throw new IllegalArgumentException(message);
	}
	
	/**
	 * Returns the number of cells, or zero if they haven't been prepared.
	 */
	public int getCellCount() {
		return (cells != null) ? cells.size() : 0;
	}
	
	/**
	 * Get the number of cells along any dimension.
	 */
	public long getCellWidth() {
		return cellWidth;
	}
		
	/**
	 * Get the volume of the container in milliliters (ml).
	 */
	public double getVolume() {
		return volume;
	}
}
