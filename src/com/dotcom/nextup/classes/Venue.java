package com.dotcom.nextup.classes;

import com.google.android.maps.GeoPoint;

public class Venue {
	private String venueName;
    private String venueLocation;
    private GeoPoint latlong;
    private Integer distance;
    
    public Venue (String name, String coordinates) {
    	venueName=name;
    	venueLocation=coordinates;
    }
    
    public Venue (String name, GeoPoint latlong) {
    	this.venueName = name;
    	this.latlong = latlong;
    	
    }
    public Venue (String name, GeoPoint latlong, Integer distance) {
    	this.venueName = name;
    	this.latlong = latlong;
    	this.distance = distance;
    }
    public String getVenueName() {
        return venueName;
    }
    public void setvenueName(String venueName) {
        this.venueName = venueName;
    }
    public String getVenueLocation() {
        return venueLocation;
    }
    public void setvenueLocation(String venueLocation) {
        this.venueLocation = venueLocation;
    }
	public void setLatlong(GeoPoint latlong) {
		this.latlong = latlong;
	}
	public GeoPoint getLatlong() {
		return latlong;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDistance() {
		return distance;
	}
}
