package edu.mtu.compound;

import org.junit.Test;

import junit.framework.Assert;

public class ReactionTests {

	@Test
	public void calculateDecayQuantityTest() {
		// Basic case, should echo input clipping
		double result = Reaction.calculateDecayQuantity(1, 1, 1, 3.0 * Math.pow(10, -7));
		Assert.assertEquals(0, result);
		
		// Should echo offset
		result = Reaction.calculateDecayQuantity(1, 1, (int)Math.pow(10, 7), 3.0 * Math.pow(10, -7));
		Assert.assertEquals(3, result);
		
		// Should adjust for volume
		result = Reaction.calculateDecayQuantity(1, 1.8, (int)Math.pow(10, 7), 3.0 * Math.pow(10, -7));
		Assert.assertEquals(5, result);
		
		// More realistic value for Avagadro's Number
		result = Reaction.calculateDecayQuantity(1, 1, Integer.MAX_VALUE, 3.0 * Math.pow(10, -7));
		Assert.assertEquals("1.0L reactor", (int)(Integer.MAX_VALUE * (3.0 * Math.pow(10, -7))), result);
		
		// Typical value used in model
		result = Reaction.calculateDecayQuantity(5, 1.8, Integer.MAX_VALUE, 3.0 * Math.pow(10, -7));
		Assert.assertEquals("1.8L reactor", (int)(Integer.MAX_VALUE * (4.32 * Math.pow(10, -9))), result);
	}
}
