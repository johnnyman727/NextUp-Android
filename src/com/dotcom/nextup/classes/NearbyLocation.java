package com.dotcom.nextup.classes;

import java.util.ArrayList;

import com.dotcom.nextup.categorymodels.Category;
import com.google.android.maps.GeoPoint;

public class NearbyLocation {

		private GeoPoint location;
		private ArrayList<Category> categories;
		private String name;
		
		public NearbyLocation(GeoPoint location, ArrayList<Category> categories, String name) {
			this.location = location;
			this.categories = categories;
			this.name = name;
		}
		
		public void setLocation(GeoPoint location) {
			this.location = location;
		}
		public GeoPoint getLocation() {
			return location;
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
}
