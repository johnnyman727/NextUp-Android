package com.dotcom.nextup.classes;

public class Venue {
	private String venueName;
    private String venueLocation;
    
    public Venue (String name, String coordinates) {
    	venueName=name;
    	venueLocation=coordinates;
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
}
