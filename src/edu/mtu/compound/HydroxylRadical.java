package edu.mtu.compound;

public class HydroxylRadical extends Molecule {

	// TODO update this per literature / program
	private final static int LifeSpan = 1;
	
	private int actions;
	
	public HydroxylRadical() {
		super("HO*");
	} 
	
	@Override
	public void doAction(int step) {
		// Constrain the life span of HO* since it is short lived
		actions++;
		if (actions == LifeSpan) {
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
