package edu.mtu.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mtu.compound.Reaction;

/**
 * This singleton contains a look up of the reactions in the simulation.
 */
public class Reactions {

	private static Reactions instance = new Reactions();
	
	private Map<String, List<Reaction>> lookUp = new HashMap<String, List<Reaction>>();
	
	/**
	 * Singleton constructor.
	 */
	private Reactions() { }
	
	/**
	 * Get the instance of the singleton.
	 */
	public static Reactions getInstance() {
		return instance;
	}
	
	/**
	 * Add the reaction given to the look-up table.
	 * 
	 * @param reaction The reaction to be added.
	 */
	public void addReaction(Reaction reaction) {
		// TODO Write this method
	}
	
	/**
	 * Get the reaction that use the given reactant.
	 * 
	 * @param reactant the formula of the chemical entity.
	 * @return A list of reactions, or null if none are found.s
	 */
	public List<Reaction> getReactions(String reactant) {
		
		// TODO Write this method
		
		return null;
	}
	
	public List<Reaction> getReactions(List<String> reactants) {
		
		// TODO Write this method
		
		return null;
	}
}
