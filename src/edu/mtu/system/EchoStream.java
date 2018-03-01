package edu.mtu.system;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class acts as a wrapper for System.out so that calls to it are echoed to a file.
 */
public class EchoStream extends PrintStream {

	private final PrintStream console = System.out;
	
	public EchoStream(OutputStream out) {
		super(out);
	}
	
	 @Override
     public void close() {
         super.close();
     }

     @Override
     public void flush() {
         super.flush();
         console.flush();
     }

     @Override
     public void write(byte[] buf, int off, int len) {
         super.write(buf, off, len);
         console.write(buf, off, len);
     }

     @Override
     public void write(int b) {
         super.write(b);
         console.write(b);
     }

     @Override
     public void write(byte[] b) throws IOException {
         super.write(b);
         console.write(b);
     }
}
