package edu.mtu.compound;

/**
 * This class represents a "virtual" molecule in the context of the model since
 * dissolved molecules are assumed to be present throughout.
 */
public class DissolvedMolecule extends Molecule {

	public DissolvedMolecule(String formula) {
		super(formula, false);
	}

	@Override
	public void dispose() { }
	
	@Override
	public void doAction(int step) {
		throw new IllegalAccessError("doAction called on DissolvedMolecule");
	}
}
