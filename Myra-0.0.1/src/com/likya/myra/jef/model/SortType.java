/*
 * TlosFaz2
 * com.likya.tlos.core.spc.helpers : SortType.java
 * @author Serkan Ta�
 * Tarih : 12.Kas.2008 23:12:41
 */

package com.likya.myra.jef.model;

import java.io.Serializable;



public class SortType implements Comparable<SortType>,Serializable {
	
	private static final long serialVersionUID = -6374567916153023159L;
	
	private String jobKey;
	int priortiyLevel = -1;
	
	/**
	 * Bu bölüm, sıralama yapılabilmesi amacı ile eklendi
	 * Örnek : Collections.sort(arrayList);
	 * @param jobRuntimeProperties
	 * @return
	 */
	
	public int compareTo(SortType sortType) {
		if (sortType.getPriortiyLevel() > this.getPriortiyLevel()) {
			return -1;
		} else if (sortType.getPriortiyLevel() < this.getPriortiyLevel()) {
			return 1;
		} 
		return 0;
	}

	public SortType(String jobKey, int priortiyLevel) {
		super();
		this.jobKey = jobKey;
		this.priortiyLevel = priortiyLevel;
	}

	public String getJobKey() {
		return jobKey;
	}

	public int getPriortiyLevel() {
		return priortiyLevel;
	}
	
}
