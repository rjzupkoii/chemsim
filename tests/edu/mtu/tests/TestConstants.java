package edu.mtu.tests;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.reaction.BasicReaction;

public class TestConstants {
	public static List<BasicReaction> BasicReactions = new ArrayList<BasicReaction>();
	static {
		BasicReactions.add(new BasicReaction(new String[]{"CH2(OH)2", "HO*"}, new String[]{"*CH(OH)2"}, 7.43E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"CH2CO", "H2O"}, new String[]{"CH3COOH"}, 44, 1));
		BasicReactions.add(new BasicReaction(new String[]{"CH3COCH3", "HO*"}, new String[]{"*CH2COCH3"}, 7.49E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"CH3COCHO", "HO*"}, new String[]{"*CH2COCHO"}, 7.40E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"CH3COOH", "HO*"}, new String[]{"*CH2COOH", "H2O"}, 1.60E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"CH3OH", "HO*"}, new String[]{"*CH2OH", "H2O"}, 9.63E+08, 1));
		BasicReactions.add(new BasicReaction(new String[]{"HCHO", "H2O"}, new String[]{"CH2(OH)2"}, 0.01, 1));
		BasicReactions.add(new BasicReaction(new String[]{"HCOOH", "HO*"}, new String[]{"*COOH", "H2O"}, 1.00E+08, 1));
		BasicReactions.add(new BasicReaction(new String[]{"HOCCOOH", "HO*"}, new String[]{"*COCOOH", "H2O", "H2O"}, 6.38E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"HOCH2COOH", "HO*"}, new String[]{"*CH(OH)COOH"}, 7.12E+07, 1));
		BasicReactions.add(new BasicReaction(new String[]{"H2O2", "UV"}, new String[]{"HO*"}, 0, 1));
	}
}
