package edu.mtu.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.mtu.reaction.BasicReaction;
import edu.mtu.reaction.ChemicalEquation;
import edu.mtu.tests.TestConstants;

/**
 * Test to ensure that the parser works correctly.
 */
public class ParserTests {
	
	private static final double epsilon = 1e-15;
	private final static String chemicalsFileName = "tests/chemicals.csv";
	private final static String reactionsFileName = "tests/reactions.csv";
	
	private static List<ChemicalDto> chemicals = new ArrayList<ChemicalDto>();
	static {
		chemicals.add(new ChemicalDto("Hydrogen Peroxide", "H2O2", 150.0));
		chemicals.add(new ChemicalDto("Acetone", "CH3COCH3", 11.0));
		chemicals.add(new ChemicalDto("Water", "H2O", 1001.1));
		chemicals.add(new ChemicalDto("Hydrogen", "H", 0.00125));
	}
		
	/**
	 * Test to make sure we can parse chemicals files.
	 */
	@Test
	public void parseChemicalsTest() throws IOException {
		List<ChemicalDto> results = Parser.parseChemicals(chemicalsFileName);
		
		Assert.assertEquals(chemicals.size(), results.size());
		for (int ndx = 0; ndx < chemicals.size(); ndx++) {
			Assert.assertEquals(chemicals.get(ndx).name, results.get(ndx).name);
			Assert.assertEquals(chemicals.get(ndx).formula, results.get(ndx).formula);
			Assert.assertEquals(chemicals.get(ndx).mols, results.get(ndx).mols, epsilon);
		}
	}
	
	/**
	 * Test to make sure the rate is loaded correctly.
	 */
	@Test
	public void parseRateTest() throws IOException {
		double result = Parser.parseRate(chemicalsFileName);
		Assert.assertEquals(-2.987E-7, result, epsilon);
	}
	
	/**
	 * Test to make sure the volume is loaded correctly.
	 */
	@Test
	public void parseVolumeTest() throws IOException {
		double result = Parser.parseVolume(chemicalsFileName);
		Assert.assertEquals(1.8d, result, epsilon);
	}
	
	/**
	 * Test to make sure we can parse reaction files.
	 */
	@Test
	public void parseReactionsTest() throws IOException {
		List<ChemicalEquation> results = Parser.parseReactions(reactionsFileName);
		
		Assert.assertEquals(TestConstants.BasicReactions.size(), results.size());		
		for (int ndx = 0; ndx < TestConstants.BasicReactions.size(); ndx++) {
			BasicReaction result = (BasicReaction)results.get(ndx);
			BasicReaction reference = TestConstants.BasicReactions.get(ndx);
			
			assertThat(result.getReactants(), is(reference.getReactants()));
			assertThat(result.getProducts(), is(reference.getProducts()));
			Assert.assertEquals(reference.getReactionRate(), result.getReactionRate(), epsilon);			
		}
	}
}
