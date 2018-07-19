package edu.mtu.compound;

public class HydroxylRadical extends Molecule {

	private int actions;
	
	public HydroxylRadical() {
		super("HO*");
	} 
	
	@Override
	public void doAction(int step) {
		// Constrain the life span of HO* since it is short lived
		// TODO Update this for the time span
		actions++;
		if (actions == 60) {
			// Track our decay at this time step
			int[] location = grid.getObjectLocation(this);
			MoleculeFactory.create("HO*'", location);
			dispose();
		} else {
			// HO* just moves, everything else reacts with it
			move();
		}
	}
}
