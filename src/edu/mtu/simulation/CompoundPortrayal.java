package edu.mtu.simulation;

import java.awt.Color;

import javax.media.j3d.TransformGroup;

import edu.mtu.compound.Species;
import sim.portrayal3d.simple.SpherePortrayal3D;

public class CompoundPortrayal extends SpherePortrayal3D {
	
	private Color color;

	/**
	 * Constructor.
	 * 
	 * @param species The species to portray.
	 */
	public CompoundPortrayal(Species species) {
		color = species.getColor();
	}
	
	/**
	 * Apply lighting to the compound.
	 */
	@Override
	public TransformGroup getModel(Object object, TransformGroup j3dModel) {
		setAppearance(j3dModel, appearanceForColors(color, null, color, null, 1.0f, 1.0f));
		return super.getModel(object, j3dModel);
	}
}
