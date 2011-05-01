package com.dotcom.nextup.classes;

import java.util.ArrayList;

import com.dotcom.nextup.categorymodels.Category;

public class RecommendationInput {
	/* This class does not actually make the recommendation.
	 * It just represents the appropriate input to provide
	 * when asking for a recommendation.
	 */
	ArrayList<Category> categories_custom;
	ArrayList<Category> categories_cloud;
	double latitude;
	double longitude;
	double max_distance; // in meters, cut off distance for how far away a venue can be, should vary by transportation type
	int numResultsDesired;
	
	
	public RecommendationInput(ArrayList<Category> categories_custom, ArrayList<Category> categories_cloud, 
			double lat, double lon, double distance, int n) {
		this.categories_custom = categories_custom;
		this.categories_cloud = categories_cloud;
		this.latitude = lat;
		this.longitude = lon;
		this.max_distance = distance;
		this.numResultsDesired = n;
	}
	
	public ArrayList<Category> getCategories() {
		ArrayList<Category> all_cats = new ArrayList<Category>();
		all_cats.addAll(categories_custom);
		all_cats.addAll(categories_cloud);
		return all_cats;
	}
	public void setCustomCategories(ArrayList<Category> cats) {this.categories_custom = cats;}
	public void setCloudCategories(ArrayList<Category> cats) {this.categories_cloud = cats;}
	public ArrayList<Category> getCustomCategories() { return categories_custom; }
	public ArrayList<Category> getCloudCategories() { return categories_cloud; }
	
	public double getLatitude() {return this.latitude;}
	public void setLatitude(double lat) {this.latitude = lat;}
	public double getLongitude() {return this.longitude;}
	public void setLongitude(double lon) {this.latitude = lon;}
	public double getMaxDistance() { return this.max_distance; }
	public int getNumResultsDesired() { return this.numResultsDesired; }
}
