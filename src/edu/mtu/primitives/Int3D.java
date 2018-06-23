package edu.mtu.primitives;

import java.util.Arrays;

public class Int3D {
	
	public final int x;
	public final int y;
	public final int z;
	
	private final int hash;
	
	/**
	 * Constructor, will create a hash based upon x, y, z
	 */
	public Int3D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		// Use the basic array hash to be safe
		hash = Arrays.hashCode(new int[] { x, y, z});
	}
	
	/**
	 * Constructor, expects the hash to be provided.
	 */
	public Int3D(int x, int y, int z, int hash) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.hash = hash;
	}

	/**
	 * Return the hash for this object.
	 */
	@Override
	public int hashCode() {
        return hash;
	}
	
	/**
	 * Compare this object to the one provided.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof Int3D) {
			Int3D other = (Int3D)obj;
			return (other.x == x && other.y == y && other.z == z && other.hash == hash);
		}
		
		return false;
	}
}
