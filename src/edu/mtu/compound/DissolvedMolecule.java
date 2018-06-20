package edu.mtu.compound;

/**
 * This class represents a "virtual" molecule in the context of the model since
 * dissolved molecules are assumed to be present throughout.
 */
public class DissolvedMolecule extends Molecule {

	public DissolvedMolecule(String formula) {
		this.formula = formula;
	}

	@Override
	public void dispose() { }
	
	@Override
	public void doAction() {
		throw new IllegalAccessError("doAction called on DissolvedMolecule");
	}
}
