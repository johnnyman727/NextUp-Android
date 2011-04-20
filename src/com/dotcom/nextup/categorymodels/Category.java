package com.dotcom.nextup.categorymodels;

import java.io.Serializable;
import java.util.Comparator;

public class Category implements Comparator<Category>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3131950815409215218L;
	private String name;
	private Integer frequency;
	private Integer averageTime;
	
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
}
