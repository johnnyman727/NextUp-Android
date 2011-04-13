package com.dotcom.nextup.categorymodels;

import java.util.Comparator;

import android.location.Location;

public class Category implements Comparator<Category> {

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
	
	public void setCategoryFromCurrentLocation(Location location) {
		//TODO: Httpget to foursquare to find place for current location. convert to category and return
	}
}
