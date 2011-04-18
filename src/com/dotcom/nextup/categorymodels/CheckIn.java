package com.dotcom.nextup.categorymodels;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

public class CheckIn {

	private Integer time;
	private ArrayList<Category> categories;
	private String name;
	private GeoPoint location;

	public CheckIn(Integer time, ArrayList<Category> categories, GeoPoint loc, String name) {
		this.time = time;
		this.categories = categories;
		this.setLocation(loc);
		this.name = name;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public Integer getTime() {
		return time;
	}

	public void setCategories(ArrayList<Category> categories) {
		this.categories = categories;
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public GeoPoint getLocation() {
		return location;
	}
}
