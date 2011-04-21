package com.dotcom.nextup.categorymodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Category implements Comparator<Category>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3131950815409215218L;
	private String name;
	private Integer frequency;
	private Integer averageTime; //hour of day, 0 - 23
	
	public Category() {
		super();
		this.name = "Unknown Name";
	}
	public Category(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	public Integer getFrequency() {
		return this.frequency;
	}
	
	@Override
	public int compare(Category c1, Category c2) {
		return c1.getFrequency() - c2.getFrequency();
	}

	public void setAverageTime(Integer averageTime) {
		this.averageTime = averageTime;
	}
	
	public Integer getAverageTime() {
		return averageTime;
	}
	
	//getAttributes() and getAttributeValues() have to correspond
	public ArrayList<String> getAttributes() {
		ArrayList<String> attrs = new ArrayList<String>();
		attrs.add("name");
		attrs.add("frequency");
		attrs.add("averageTime");
		return attrs;
	}
	public ArrayList<String> getAttributeValues() {
		ArrayList<String> vals = new ArrayList<String>();
		if (name == null) { vals.add(""); }
		else { vals.add(name); }
		if (frequency == null) { vals.add("none"); }
		else { vals.add(Integer.toString(frequency)); }
		if (averageTime == null) { vals.add("none"); }
		else { vals.add(Integer.toString(averageTime)); }
		return vals;
	}
}
