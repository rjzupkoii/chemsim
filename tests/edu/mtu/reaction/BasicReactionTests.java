package edu.mtu.reaction;

import org.junit.Test;

import edu.mtu.simulation.SimulationProperties;
import edu.mtu.tests.TestConstants;
import junit.framework.Assert;

public class BasicReactionTests {

	// Expected interaction radius, mirrors TestConstants.BasicReactions for dt = 1/sec
	public static int[] ExpectedRates = new int[] { 308, 3, 309, 308, 185, 705, 0, 340, 293, 304, 0 };
		
	/**
	 * Test to ensure that the interaction radius is correct.
	 */
	@Test
	public void interactionRadiusTests() {
	
		// Make sure we think the time step is correct
		//double timeStep = SimulationProperties.getInstance().getTimeStepLength();
		double timeStep = SimulationProperties.getInstance().getDeltaT();
		Assert.assertEquals(1.0, timeStep);
		
		// Check the rates
		Assert.assertEquals(ExpectedRates.length, TestConstants.BasicReactions.size());
		for (int ndx = 0; ndx < TestConstants.BasicReactions.size(); ndx++) {
			Assert.assertEquals(ExpectedRates[ndx], TestConstants.BasicReactions.get(ndx).getInteractionRadius());
		}
	}
}
