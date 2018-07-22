package edu.mtu.compound;

public class Hydroxyl extends Molecule {
	
	public Hydroxyl() {
		super("HO*");
	}	

	@Override
	public void doAction(int step) {
		// HO* is short lived, so just dispose if yourself and note the action
		MoleculeFactory.create("HO*'", grid.getObjectLocation(this));
		dispose();		
	}
}
