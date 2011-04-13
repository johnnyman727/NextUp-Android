package com.dotcom.nextup.classes;

import java.util.ArrayList;

import com.dotcom.nextup.categorymodels.Category;

public class RecommendationInput {
	ArrayList<Category> categories;
	double latitude;
	double longitude;
	
	public RecommendationInput(ArrayList<Category> categories, double lat, double lon) {
		this.categories = categories;
		this.latitude = lat;
		this.longitude = lon;
	}
	
	public ArrayList<Category> getCategories() {
		return this.categories;
	}
	public void setCategories(ArrayList<Category> cats) {
		this.categories = cats;
	}
	public double getLatitude() {
		return this.latitude;
	}
	public void setLatitude(double lat) {
		this.latitude = lat;
	}
	public double getLongitude() {
		return this.longitude;
	}
	public void setLongitude(double lon) {
		this.latitude = lon;
	}
}
