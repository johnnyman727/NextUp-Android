package com.dotcom.nextup.classes;

import android.graphics.drawable.Drawable;
import android.location.Location;

public class Venue implements Comparable<Venue>{
	private String name;
    double latitude;
    double longitude;
    private Drawable picture;
    private boolean mSelectable = true;
    
    public Venue (String nam, double lat, double lon) {
    	this.name = nam;
    	this.latitude = lat;
    	this.longitude = lon;
    }
    
    public Venue (String nam, double lat, double lon, Drawable draw) {
    	this.name=nam;
    	this.latitude=lat;
    	this.longitude = lon;
    	this.picture = draw;
    }
    
    public int compareTo(Venue other) {
        if(this.name != null)
            return this.name.compareTo(other.getName());
        else
            throw new IllegalArgumentException();
    }
    
    public String getName() { return this.name; }
    public void setName(String venueName) { this.name = venueName;}
    public Drawable getDrawable() { return this.picture; }
    public void setDrawable(Drawable draw) {this.picture = draw;}
    public boolean isSelectable() { return this.mSelectable; }
    public void setSelectable(boolean s) { this.mSelectable = s; }
}
