package edu.mtu.reaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class ReactionRegistryTests {

	private final static String reactionsFileName = "tests/reactions.csv";
	
	private final static String[] expectedEntities = new String[] { "HO*", "*CH(OH)2", "*CH2COCH3",
			"*CH2COCHO", "*CH2COOH", "*CH2OH", "*COOH",	"*COCOOH", "*CH(OH)COOH", "CH2CO", "H2O", 
			"H2O2", "CH3COCH3", "CH3COCHO", "CH3COOH", "CH3OH", "HCHO", "CH2(OH)2", "HCOOH", "HOCCOOH", 	
			"HOCH2COOH" };
	
	private final static String[] expectedProducts = new String[] { "*COOH", "*CH2OH", "*COCOOH", 
			"*CH(OH)COOH", "*CH2COCHO", "*CH2COCH3", "*CH(OH)2", "*CH2COOH" };
	
	@Before
	public void setUp() throws IOException {
		ReactionRegistry instance = ReactionRegistry.getInstance();
		instance.clear();
		instance.load(reactionsFileName);
	}
	
	@Test
	public void getEntityListTest() {
		List<String> entities = new ArrayList<String>(ReactionRegistry.getInstance().getEntityList());
		for (String expected : expectedEntities) {
			Assert.assertTrue(expected, entities.contains(expected));
			entities.remove(expected);
		}
		Assert.assertEquals(0, entities.size());
	}
}
