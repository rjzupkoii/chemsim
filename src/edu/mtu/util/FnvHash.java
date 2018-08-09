package edu.mtu.util;

/**
 * Basic implementation of the FNV-1a http://www.isthe.com/chongo/tech/comp/fnv/index.html) hash.
 */
public class FnvHash {

	private static final int FNV1_32_INIT = 0x811c9dc5;
	private static final int FNV1_PRIME_32 = 16777619;
	private static final long FNV1_64_INIT = 0xcbf29ce484222325L;
	private static final long FNV1_PRIME_64 = 1099511628211L;
	
	/**
	 * FNV-1a 32-bit hash for strings.
	 */
	public static int fnv1a32(String data) {
		int hash = FNV1_32_INIT;
		for (byte value : data.getBytes()) {
			hash ^= value;
			hash *= FNV1_PRIME_32;
		}
		return hash;
	}
	
	/**
	 * FNV-1a 64-bit hash for strings.
	 */
	public static long fnv1a64(String data) {
		long hash = FNV1_64_INIT;
		for (byte value : data.getBytes()) {
			hash ^= value;
			hash *= FNV1_PRIME_64;
		}
		return hash;
	}
}
