package edu.mtu.simulation.tracking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BufferedCsvWriter {

	private BufferedWriter writer;
	private String fileName;
	
	/**
	 * Open the file for writing.
	 * 
	 * @param fileName The file name and path.
	 */
	public BufferedCsvWriter(String fileName) throws IOException {
		open(fileName, false);
	}
	
	/**
	 * Open the file for writing.
	 * 
	 * @param fileName The file name and path.
	 * @param overwrite True if the file should be overwritten, false otherwise.
	 */
	public BufferedCsvWriter(String fileName, boolean overwrite) throws IOException {
		open(fileName, overwrite);
	}
	
	/**
	 * Finalize the line, flush any buffered contents, and close the file.
	 */
	public void close() throws IOException {
		writer.write(System.lineSeparator());
		writer.flush();
		writer.close();
	}
	
	/**
	 * Flushes the buffer to the file.
	 */
	public void flush() throws IOException {
		writer.flush();
	}
	
	/**
	 * Get the filename of the file being written.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Move to a new line in the file.
	 */
	public void newline() throws IOException {
		writer.write(System.getProperty("line.separator"));
	}
	
	/**
	 * Open the file for writing.
	 * 
	 * @param fileName The file name and path.
	 * @param overwrite True if the file should be overwritten, false otherwise.
	 */
	public void open(String fileName, boolean overwrite) throws IOException {
		this.fileName = fileName;
		FileWriter file = (overwrite) ? new FileWriter(fileName) : new FileWriter(fileName, overwrite);
		writer = new BufferedWriter(file);
	}

	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(int value) throws IOException {
		writer.write(value + ",");
	}
	
	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(long value) throws IOException {
		writer.write(value + ",");
	}
	
	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(double value) throws IOException {
		writer.write(value + ",");
	}	
	
	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(String value) throws IOException {
		writer.write(value + ",");
	}
	
	/**
	 * Write the list of values to the file, finish with a new line.
	 */
	public void write(List<String> values) throws IOException {
		for (String value : values) {
			writer.write(value + ",");
		}
		writer.write(System.getProperty("line.separator"));
	}

	/**
	 * Write the array of values to the file, finish with a new line.
	 */
	public void write(String[] values) throws IOException {
		for (String value : values) {
			if (value.isEmpty()) {
				continue;
			}
			writer.write(value + ",");
		}
		writer.write(System.getProperty("line.separator"));
	}
}
