package edu.mtu.compound;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class represents a chemical entity.
 */
@SuppressWarnings("serial")
public class Entity implements Steppable {

	private String formula;

	/**
	 * Get the formula of this entity.
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * Set the formula of this entity.
	 */
	public void setFormula(String value) {
		formula = value;
	}
	
	@Override
	public void step(SimState state) {
		// TODO Move 
		
		// TODO Are other compounds here?
		
			// TODO React with them if need be
		
		// TODO Decay if unstable
		
		// TODO React to UV if exposed
		
	}
}
