package edu.mtu.primitives;

import java.util.Arrays;

public class Int3D {

	public final int x;
	public final int y;
	public final int z;
	
	private final int hash;
	
	public Int3D(int x, int y, int z) {
		// Note the values
		this.x = x;
		this.y = y;
		this.z = z;
		
		// TODO Evaluate this for performance
		// Calculate the hash once and retain it
		this.hash = Arrays.hashCode(new int[] { x, y, z});
	}

	@Override
	public int hashCode() {
        return hash;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof Int3D) {
			Int3D other = (Int3D)obj;
			return (other.x == x && other.y == y && other.z == z);
		}
		
		return false;
	}
}
