package edu.mtu.simulation;

public class CompoundBehavior {
	private long hydrogenPeroxideDecayQuantity = 0;
	private double hydrogenPeroxideDecay = 0;

	public double getHydrogenPeroxideDecay() {
		return hydrogenPeroxideDecay;
	}
	
	public long getHydrogenPeroxideDecayQuantity() {
		return hydrogenPeroxideDecayQuantity;
	}
	
	public void setHydrogenPeroxideDecay(double value) {
		hydrogenPeroxideDecay = value;
	}
	
	public void setHydrogenPeroxideDecayQuantity(long value) {
		hydrogenPeroxideDecayQuantity = value;
	}
}
