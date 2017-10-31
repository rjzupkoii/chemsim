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
	 * @param append True if the file should be appended to, false otherwise.
	 */
	public BufferedCsvWriter(String fileName, boolean append) throws IOException {
		open(fileName, append);
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
	 * @param append True if the file should be appended to, false otherwise.
	 */
	public void open(String fileName, boolean append) throws IOException {
		this.fileName = fileName;
		FileWriter file = new FileWriter(fileName, append);
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
	 * Write the list of values to the file, finish with a new line.
	 */
	public void write(List<String> values) throws IOException {
		for (String value : values) {
			writer.write(value + ",");
		}
		writer.write(System.getProperty("line.separator"));
	}
}
