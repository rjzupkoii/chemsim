package edu.mtu.compound;

import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps how chemical entities named. Each chemical entity can be mapped back to to 
 * a non-unique name that is used as needed. 
 */
public class Nomenclature {

	private String commonName;
	
	private List<String> chemicalEntities = new ArrayList<String>();
	
	/**
	 * Return true if the entity is aliased to this name, false otherwise.
	 */
	public boolean contains(String entity) {
		return chemicalEntities.contains(entity);
	}
	
	/**
	 * Get the common name string for this chemical entity.
	 */
	public String getCommonName(String entity) {
		if (commonName.isEmpty()) {
			return entity;
		}
		return String.format("{0}, {1}", commonName, entity);
	}
}
