package com.dotcom.nextup.classes;

import java.util.ArrayList;

import com.dotcom.nextup.categorymodels.Category;

public class RecommendationInput {
	/* This class does not actually make the recommendation.
	 * It just represents the appropriate input to provide
	 * when asking for a recommendation.
	 */
	ArrayList<Category> categories;
	double latitude;
	double longitude;
	double max_distance; // in meters, cut off distance for how far away a venue can be, should vary by transportation type
	
	public RecommendationInput(ArrayList<Category> categories, double lat, double lon, double distance) {
		this.categories = categories;
		this.latitude = lat;
		this.longitude = lon;
		this.max_distance = distance;
	}
	
	public ArrayList<Category> getCategories() {return this.categories;}
	public void setCategories(ArrayList<Category> cats) {this.categories = cats;}
	public double getLatitude() {return this.latitude;}
	public void setLatitude(double lat) {this.latitude = lat;}
	public double getLongitude() {return this.longitude;}
	public void setLongitude(double lon) {this.latitude = lon;}
	public double getMaxDistance() { return this.max_distance; }
}
