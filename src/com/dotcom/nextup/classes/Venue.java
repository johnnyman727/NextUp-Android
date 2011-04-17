package com.dotcom.nextup.classes;

import android.graphics.drawable.Drawable;
import android.location.Location;

public class Venue implements Comparable<Venue>{
	private String name;
    double latitude;
    double longitude;
    private String image_url;
    private String url;
    private boolean mSelectable = true;
    
    public Venue (String nam, double lat, double lon) {
    	this.name = nam;
    	this.latitude = lat;
    	this.longitude = lon;
    }
    
    public Venue (String nam, double lat, double lon, String url, String image_url) {
    	this.name=nam;
    	this.latitude=lat;
    	this.longitude = lon;
    	this.url = url;
    	this.image_url = image_url;
    }
    
    public int compareTo(Venue other) {
        if(this.name != null)
            return this.name.compareTo(other.getName());
        else
            throw new IllegalArgumentException();
    }
    
    public String getName() { return this.name; }
    public void setName(String venueName) { this.name = venueName;}
    public boolean isSelectable() { return this.mSelectable; }
    public void setSelectable(boolean s) { this.mSelectable = s; }
    public String getImageURL() { return image_url; }
    public String getURL() { return url; }
}
