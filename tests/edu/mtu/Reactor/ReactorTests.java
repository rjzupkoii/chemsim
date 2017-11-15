package edu.mtu.Reactor;

import java.util.ArrayList;

import org.junit.Test;

import edu.mtu.parser.ChemicalDto;
import junit.framework.Assert;

public class ReactorTests {
	/**
	 * Test to ensure Avogadro's Number is calculated correctly.
	 */
	@Test
	public void calculateAvogadroNumberTest() {
		Reactor instance = Reactor.getInstance();
		
		// Make sure it starts at zero
		Assert.assertEquals(0, instance.getAvogadroNumber());
		
		ArrayList<ChemicalDto> items = new ArrayList<ChemicalDto>();
		items.add(new ChemicalDto("1", "1", 1));
		double result = instance.calculateAvogadroNumber(items);
		Assert.assertEquals(Integer.MAX_VALUE, result);
		
		items.add(new ChemicalDto("2", "2", 2));
		result = instance.calculateAvogadroNumber(items);
		Assert.assertEquals((int)(Integer.MAX_VALUE / 2.0), result);
		
		items.add(new ChemicalDto("0.5", "0.5", 0.5));
		items.add(new ChemicalDto("3.5", "3.5", 3.5));
		result = instance.calculateAvogadroNumber(items);
		Assert.assertEquals((int)(Integer.MAX_VALUE / 3.5), result);
	}
}
