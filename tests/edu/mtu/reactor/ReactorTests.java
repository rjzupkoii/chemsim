package edu.mtu.reactor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.mtu.parser.ChemicalDto;
import edu.mtu.reactor.Reactor;
import junit.framework.Assert;

public class ReactorTests {
	private static List<ChemicalDto> compounds = new ArrayList<ChemicalDto>();
	static {
		compounds.add(new ChemicalDto("Hydrogen Peroxide", "H2O2", 0.02 * 1e-9));
		compounds.add(new ChemicalDto("Acetone", "CH3COCH3", 0.0023 * 1e-9));
	}
		
	/**
	 * Test to ensure that size calculations are correct.
	 */
	@Test
	public void calculateSizeTest() {
		int result = Reactor.calculateSize(compounds, 100000);
		Assert.assertEquals(195276, result);
	}
}
