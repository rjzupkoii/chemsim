package edu.mtu.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.mtu.catalog.ReactionDescription;

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
	}
	
	private static List<ReactionDescription> reactions = new ArrayList<ReactionDescription>();
	static {
		reactions.add(new ReactionDescription(new String[]{"CH2(OH)2", "HO*"}, new String[]{"*CH(OH)2"}, 7.43E+07));
		reactions.add(new ReactionDescription(new String[]{"CH2CO", "H2O"}, new String[]{"CH3COOH"}, 44));
		reactions.add(new ReactionDescription(new String[]{"CH3COCH3", "HO*"}, new String[]{"*CH2COCH3"}, 7.49E+07));
		reactions.add(new ReactionDescription(new String[]{"CH3COCHO", "HO*"}, new String[]{"*CH2COCHO"}, 7.40E+07));
		reactions.add(new ReactionDescription(new String[]{"CH3COOH", "HO*"}, new String[]{"*CH2COOH", "H2O"}, 1.60E+07));
		reactions.add(new ReactionDescription(new String[]{"CH3OH", "HO*"}, new String[]{"*CH2OH", "H2O"}, 9.63E+08));
		reactions.add(new ReactionDescription(new String[]{"HCHO", "H2O"}, new String[]{"CH2(OH)2"}, 0.01));
		reactions.add(new ReactionDescription(new String[]{"HCOOH", "HO*"}, new String[]{"*COOH", "H2O"}, 1.00E+08));
		reactions.add(new ReactionDescription(new String[]{"HOCCOOH", "HO*"}, new String[]{"*COCOOH", "H2O"}, 6.38E+07));
		reactions.add(new ReactionDescription(new String[]{"HOCH2COOH", "HO*"}, new String[]{"*CH(OH)COOH"}, 7.12E+07));
		reactions.add(new ReactionDescription(new String[]{"H2O2", "UV"}, new String[]{"HO*"}, 0));
	}
	
	/**
	 * Test to make sure we can parse chemicals files.
	 */
	@Test
	public void parseChemicals() throws IOException {
		List<ChemicalDto> results = Parser.parseChemicals(chemicalsFileName);
		
		Assert.assertEquals(chemicals.size(), results.size());
		for (int ndx = 0; ndx < chemicals.size(); ndx++) {
			Assert.assertEquals(chemicals.get(ndx).name, results.get(ndx).name);
			Assert.assertEquals(chemicals.get(ndx).formula, results.get(ndx).formula);
			Assert.assertEquals(chemicals.get(ndx).mols, results.get(ndx).mols, epsilon);
		}
	}
	
	/**
	 * Test to make sure we can parse reaction files.
	 */
	@Test
	public void parseReactionsTest() throws IOException {
		List<ReactionDescription> results = Parser.parseReactions(reactionsFileName);
		
		Assert.assertEquals(reactions.size(), results.size());		
		for (int ndx = 0; ndx < reactions.size(); ndx++) {
			ReactionDescription result = results.get(ndx);
			ReactionDescription reference = reactions.get(ndx);
			
			assertThat(result.getReactants(), is(reference.getReactants()));
			assertThat(result.getProducts(), is(reference.getProducts()));
			Assert.assertEquals(reference.getReactionRate(), result.getReactionRate(), epsilon);			
		}
	}
}
