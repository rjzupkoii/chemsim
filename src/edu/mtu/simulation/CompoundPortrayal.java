package edu.mtu.simulation;

import java.awt.Color;

import javax.media.j3d.TransformGroup;

import sim.portrayal3d.simple.SpherePortrayal3D;

public class CompoundPortrayal extends SpherePortrayal3D {
	
	private Color color;
	
	/**
	 * Constructor.
	 * 
	 * @param compound The class of the compound that is being rendered.
	 * @throws Exception One of various exception types that will be 
	 * generated if the compound cannot be loaded. 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CompoundPortrayal(Class compound) throws Exception {
		color = (Color)compound.getMethod("getColor", (Class[])null).invoke(null, (Object[])null);
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
