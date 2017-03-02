package edu.mtu.simulation;

import edu.mtu.compound.Acetone;
import edu.mtu.compound.Alcohol;
import edu.mtu.compound.Aldehyde;
import edu.mtu.compound.HydrogenPeroxide;
import edu.mtu.compound.radical.CarbonCentered;
import edu.mtu.compound.radical.Hydroxyl;
import edu.mtu.compound.radical.Oxyl;
import edu.mtu.compound.radical.Peroxy;

// TODO Generate the following block of properties using reflection
public class CompoundInspector {
	
	public int getAcetoneCount() {
		return countClass(Acetone.class);
	}
	
	public int getAlcoholCount() {
		return countClass(Alcohol.class);
	}
	
	public int getAldehydeCount() {
		return countClass(Aldehyde.class);
	}
	
	public int getHydrogenPeroxideCount() {
		return countClass(HydrogenPeroxide.class);
	}
	
	public int getCarbonCenteredRadicalCount() {
		return countClass(CarbonCentered.class);
	}
	
	public int getHydroxylRadicalCount() {
		return countClass(Hydroxyl.class);
	}
	
	public int getOxylRadicalCount() {
		return countClass(Oxyl.class);
	}
	
	public int getPeroxyRadicalCount() {
		return countClass(Peroxy.class);
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
