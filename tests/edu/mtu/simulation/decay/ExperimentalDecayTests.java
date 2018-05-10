package edu.mtu.simulation.decay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.mtu.simulation.decay.DecayDto;
import junit.framework.Assert;

public class ExperimentalDecayTests {
	private final static double Epsilon = 1E-5;
	

	private static Map<Integer, Map<String, DecayDto>> experimental = new HashMap<Integer, Map<String, DecayDto>>();
	static {
		experimental.put(0, new HashMap<String, DecayDto>());
		experimental.get(0).put("CH3COCH3", new DecayDto(-0.01, 0.0013));
		experimental.get(0).put("H2O2", new DecayDto(-0.033333, 0.011));
		
		experimental.put(30,  new HashMap<String, DecayDto>());
		experimental.get(30).put("CH3COCH3", new DecayDto(-0.0033333333333333327, 0.001));
		experimental.get(30).put("H2O2", new DecayDto(-0.06666666666666667, 0.01));
		
		experimental.put(90,  new HashMap<String, DecayDto>());
		experimental.get(90).put("CH3COCH3", new DecayDto(-0.00667, 0.0008));
		experimental.get(90).put("H2O2", new DecayDto(-0.03333, 0.006));
		
		experimental.put(150,  new HashMap<String, DecayDto>());
		experimental.get(150).put("CH3COCH3", new DecayDto(-0.00333, 0.0004));
		experimental.get(150).put("H2O2", new DecayDto(-0.06666666666666667, 0.004));
				
		experimental.put(180,  new HashMap<String, DecayDto>());
		experimental.get(180).put("CH3COCH3", new DecayDto(-0.00333, 0.0003));
		experimental.get(180).put("H2O2", new DecayDto(-0.03333, 0.002));
		
		experimental.put(210,  new HashMap<String, DecayDto>());
		experimental.get(210).put("CH3COCH3", new DecayDto(-0.00111, 0.0002));
		experimental.get(210).put("H2O2", new DecayDto(-0.00556, 0.001));
		
		experimental.put(300,  new HashMap<String, DecayDto>());
		experimental.get(300).put("CH3COCH3", new DecayDto(-0.00129, 0.0001));
		experimental.get(300).put("H2O2", new DecayDto(-0.00357, 0.0005));
	}
	
	/**
	 * Test that experimental data is loaded correctly.
	 */
	@Test
	public void prepareTest() throws IOException {
		ExperimentalDecay model = new ExperimentalDecay();
		model.prepare("tests/experiment.csv");
		
		Assert.assertEquals(1.8, model.getVolume());
		
		Map<Integer, Map<String, DecayDto>> data = model.getData();	
		Assert.assertEquals(experimental.size(), data.size());
		for (int time : experimental.keySet()) {
			Assert.assertTrue("Expected " + time, data.containsKey(time));
			for (String compound : experimental.get(time).keySet()) {
				Assert.assertTrue("Expected " + compound, data.get(time).containsKey(compound));
				
				DecayDto expected = experimental.get(time).get(compound);
				DecayDto dto = data.get(time).get(compound);
				
				Assert.assertEquals(expected.slope, dto.slope, Epsilon);
				Assert.assertEquals(expected.mols, dto.mols, Epsilon);
			}
		}
	}
}
