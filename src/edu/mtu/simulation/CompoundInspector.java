package edu.mtu.simulation;

import edu.mtu.compound.AceticAcid;
import edu.mtu.compound.Acetone;
import edu.mtu.compound.Formaldehyde;
import edu.mtu.compound.FormicAcid;
import edu.mtu.compound.GlyoxyalicAcid;
import edu.mtu.compound.HydrogenPeroxide;
import edu.mtu.compound.Hydroxyacetone;
import edu.mtu.compound.Hydroxyl;
import edu.mtu.compound.OxalicAcid;
import edu.mtu.compound.PyruvicAcid;
import edu.mtu.compound.PyruvicAldehyde;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
	
	public int getAceticAcidCount() {
		return countClass(AceticAcid.class);
	}
	
	public int getAcetoneCount() {
		return countClass(Acetone.class);
	}
	
	public int getFormaldehydeCount() {
		return countClass(Formaldehyde.class);
	}
	
	public int getFormicAcidCount() {
		return countClass(FormicAcid.class);
	}
	
	public int getGlyoxyalicAcidCount() {
		return countClass(GlyoxyalicAcid.class);
	}

	public int getHydrogenPeroxideCount() {
		return countClass(HydrogenPeroxide.class);
	}

	public int getHydroxyacetoneCount() {
		return countClass(Hydroxyacetone.class);
	}

	public int getHydroxylRadicalCount() {
		return countClass(Hydroxyl.class);
	}

	public int getOxalicAcidCount() {
		return countClass(OxalicAcid.class);
	}
	
	public int getPyruvicAcidCount() {
		return countClass(PyruvicAcid.class);
	}
	
	public int getPyruvicAldehydeCount() {
		return countClass(PyruvicAldehyde.class);
	}
	
	@SuppressWarnings("rawtypes")
	private int countClass(Class className) {
		int count = 0;
		
		try {
			for (Object object : ChemSim.getInstance().getCompounds().getAllObjects()) {
				count += (object.getClass() == className) ? 1 : 0;
			}
		} catch (NullPointerException ex) {
			// Ignore these errors since they likely mean the model is not running
		}
			
		return count;
	}
}
