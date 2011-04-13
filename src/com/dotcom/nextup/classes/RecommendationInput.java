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
}
