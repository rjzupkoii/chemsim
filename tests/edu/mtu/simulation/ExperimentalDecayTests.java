package edu.mtu.simulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;

public class ExperimentalDecayTests {

	private static Map<Integer, Map<String, DecayDto>> experimental = new HashMap<Integer, Map<String, DecayDto>>();
	static {
		experimental.put(0, new HashMap<String, DecayDto>());
		experimental.get(0).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(0).put("H2O2", new DecayDto(0, 0));
		
		experimental.put(30,  new HashMap<String, DecayDto>());
		experimental.get(30).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(30).put("H2O2", new DecayDto(0, 0));
		
		experimental.put(90,  new HashMap<String, DecayDto>());
		experimental.get(90).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(90).put("H2O2", new DecayDto(0, 0));
		
		experimental.put(150,  new HashMap<String, DecayDto>());
		experimental.get(150).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(150).put("H2O2", new DecayDto(0, 0));
				
		experimental.put(180,  new HashMap<String, DecayDto>());
		experimental.get(180).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(180).put("H2O2", new DecayDto(0, 0));
		
		experimental.put(210,  new HashMap<String, DecayDto>());
		experimental.get(210).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(210).put("H2O2", new DecayDto(0, 0));
		
		experimental.put(300,  new HashMap<String, DecayDto>());
		experimental.get(300).put("CH3COCH3", new DecayDto(0, 0));
		experimental.get(300).put("H2O2", new DecayDto(0, 0));
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
				DecayDto dto = data.get(time).get(compound);
				System.out.println(String.format("%d %f %f", time, dto.slope, dto.mols));
			}
		}
	}
}
