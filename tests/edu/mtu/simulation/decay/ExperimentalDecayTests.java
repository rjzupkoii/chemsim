package edu.mtu.simulation.decay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;

public class ExperimentalDecayTests {
	private final static double Epsilon = 1E-5;
	

	// TODO Update all of these time points
	private static Map<Integer, Map<String, Double>> experimental = new HashMap<Integer, Map<String, Double>>();
	static {
		experimental.put(0, new HashMap<String, Double>());
		experimental.get(0).put("CH3COCH3", 0.0);
		experimental.get(0).put("H2O2", 0.0);
		
		experimental.put(30,  new HashMap<String, Double>());
		experimental.get(30).put("CH3COCH3", 0.0);
		experimental.get(30).put("H2O2", 0.0);
		
		experimental.put(90,  new HashMap<String, Double>());
		experimental.get(90).put("CH3COCH3", 0.0);
		experimental.get(90).put("H2O2", 0.0);
		
		experimental.put(150,  new HashMap<String, Double>());
		experimental.get(150).put("CH3COCH3", 0.0);
		experimental.get(150).put("H2O2", 0.0);
				
		experimental.put(180,  new HashMap<String, Double>());
		experimental.get(180).put("CH3COCH3", 0.0);
		experimental.get(180).put("H2O2", 0.0);
		
		experimental.put(210,  new HashMap<String, Double>());
		experimental.get(210).put("CH3COCH3", 0.0);
		experimental.get(210).put("H2O2", 0.0);
		
		experimental.put(300,  new HashMap<String, Double>());
		experimental.get(300).put("CH3COCH3", 0.0);
		experimental.get(300).put("H2O2", 0.0);
	}
	
	@Test
	public void dumpResults() throws IOException {
		ExperimentalDecay model = new ExperimentalDecay();
		model.prepare("experiment/experiment.csv");
		Map<Integer, Map<String, Double>> data = model.getData();
		ArrayList<Integer> times = new ArrayList<Integer>(data.keySet());
		Collections.sort(times);
		for (int time : times) {
			for (String compound : data.get(time).keySet()) {
				System.out.printf("%d: %s - Slope %f\n", time, compound, data.get(time).get(compound));
			}
		}
	}
	
	/**
	 * Test that experimental data is loaded correctly.
	 */
	@Test
	public void prepareTest() throws IOException {
		ExperimentalDecay model = new ExperimentalDecay();
		model.prepare("tests/experiment.csv");
		
		Assert.assertEquals(1.8, model.getVolume());
		
		Map<Integer, Map<String, Double>> data = model.getData();	
		Assert.assertEquals(experimental.size(), data.size());
		for (int time : experimental.keySet()) {
			Assert.assertTrue("Expected " + time, data.containsKey(time));
			for (String compound : experimental.get(time).keySet()) {
				Assert.assertTrue("Expected " + compound, data.get(time).containsKey(compound));
				double expected = experimental.get(time).get(compound);
				double result = data.get(time).get(compound);
				Assert.assertEquals(expected, result, Epsilon);
			}
		}
	}
}
