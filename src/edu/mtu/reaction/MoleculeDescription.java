package edu.mtu.reaction;

public class MoleculeDescription {
	public boolean hasBimolecular;
	public boolean hasPhotolysis;
	public boolean hasUnimolecular;
	
	public boolean isRadical;
	public boolean hasReactants;
	public boolean hasDissolvedReactants;
	
	// The hash and the interaction radius are coupled with each other
	public Integer[] reactsWithHash;
	public int[] interactionRadius;
}
