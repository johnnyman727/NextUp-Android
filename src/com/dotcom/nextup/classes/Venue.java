package com.dotcom.nextup.classes;

import java.util.ArrayList;
import com.dotcom.nextup.categorymodels.Category;
import com.google.android.maps.GeoPoint;

public class Venue implements Comparable<Venue>{
	private String name;
    private GeoPoint latlong;
    private String image_url;
    private String url;
    double distance; // distance in meters from current location
    private boolean mSelectable = true;
    double rating; // some venues have a five star rating, some don't
    private ArrayList<Category> categories;
    
    public Venue (String name, String url, String imageURL, GeoPoint latlong, double d, ArrayList<Category> cats) {
    	this.name = name;
    	this.url = url;
    	this.image_url = imageURL;
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = cats;
    }
    
    public Venue (String name, String url, GeoPoint latlong, double d) {
    	this.name = name;
    	this.url = url;
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = new ArrayList<Category>();
    }
    
    public Venue (String name, GeoPoint latlong, double d) {
    	this.name = name; 
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = new ArrayList<Category>();
    }
     
    public void setRating(double rating) { this.rating = rating; }
    public double getRating() { return rating; }
    
    public String toString() {
    	return "Venue " + this.name;
    }
    
    public int compareTo(Venue other) {
        if(this.name != null)
            return this.name.compareTo(other.getName());
        else
            throw new IllegalArgumentException();
    }
    
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public GeoPoint getLatlong() { return latlong; }
    public void setGeoPoint(GeoPoint gp) {latlong = gp; }
    public String getURL() { return url; }
    public void setURL(String URL) { url = URL; }
    public String getImageURL() { return image_url; }
    public void setImageURL(String URL) { image_url = URL; }
    public double getDistance() { return distance; }
    public void setDistance(double d) { distance = d; }

	public void setmSelectable(boolean mSelectable) {
		this.mSelectable = mSelectable;
	}

	public boolean ismSelectable() {
		return mSelectable;
	}

	public void setCategories(ArrayList<Category> categories) {
		this.categories = categories;
	}
	
	public void addCategory (Category cat) {
		this.categories.add(cat);
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}
    
}
