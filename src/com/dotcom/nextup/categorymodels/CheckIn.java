package com.dotcom.nextup.categorymodels;

import java.util.ArrayList;

public class CheckIn {

	private Integer time;
	private ArrayList<Category> categories;
	private String name;
	private String location;

	public CheckIn(Integer time, ArrayList<Category> categories, String lat,
			String lon, String name) {
		this.time = time;
		this.categories = categories;
		this.location = lat + "," + lon;
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

	public void setLocation(String lat, String lon) {
		this.location = lat + "," + lon;
	}

	public String getLocation() {
		return location;
	}
}
