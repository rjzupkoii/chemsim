package edu.mtu.simulation.schedule;

import org.junit.Test;

import junit.framework.Assert;

// TODO Make this a proper set of unit tests
public class ScheduleTests implements Simulation {

	private class TestSteppable extends Steppable {

		private int number;
		
		public TestSteppable(int number) {
			this.number = number;
		}
		
		@Override
		public void doAction() {
			System.out.println(number);
			if (number == 2) {
				schedule.remove(this);
			}
		}
	}
	
	private final static int starting = 5;
	private final static int timeSteps = 10;
	
	private Schedule schedule;
	
	/**
	 * Not a unit test per se, but good enough to verify that the schedule is working correctly.
	 */
	@Test
	public void ScheduleTest() {
		// Prepare by adding a couple items to the schedule
		schedule = new Schedule();
		for (int ndx = 0; ndx < starting; ndx++) {
			schedule.insert(new TestSteppable(ndx));
		}
		
		// Run the schedule
		schedule.start(this, timeSteps);
	}

	@Override
	public void initialize(long seed) { }

	@Override
	public void start(int timeSteps) { }
	
	@Override
	public void step() { }
	
	@Override
	public void finish(boolean terminated) {
		System.out.println("Completed: " + schedule.getTimeStep());
		Assert.assertEquals(timeSteps, schedule.getTimeStep());
	}	
}
