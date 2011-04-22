package com.dotcom.nextup.categorymodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Category implements Comparator<Category>, Serializable {

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
	public Category(String nam, Integer freq, Integer time) {
		name = nam;
		frequency = freq;
		averageTime = time;
	}
	
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
	public void setFrequency(Integer frequency) {this.frequency = frequency;}
	public Integer getFrequency() {return this.frequency;}
	public void setAverageTime(Integer averageTime) {this.averageTime = averageTime;}
	public Integer getAverageTime() {return averageTime;}
	
	@Override
	public int compare(Category c1, Category c2) {
		return c1.getFrequency() - c2.getFrequency();
	}
	public ArrayList<String> getAttributeValues() {
		ArrayList<String> attrs = new ArrayList<String>();
		attrs.add(name);
		attrs.add(frequency.toString());
		attrs.add(averageTime.toString());
		return attrs;
	}
	public ArrayList<String> getAttributes() {
		ArrayList<String> attrs = new ArrayList<String>();
		attrs.add("name");
		attrs.add("frequency");
		attrs.add("averageTime");
		return attrs;
	}
	
}
