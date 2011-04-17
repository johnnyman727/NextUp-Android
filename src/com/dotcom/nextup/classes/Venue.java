package com.dotcom.nextup.classes;

import com.google.android.maps.GeoPoint;

public class Venue implements Comparable<Venue>{
	private String name;
    private String venueLocation;
    private GeoPoint latlong;
    private Integer distance;
    private String image_url;
    private boolean mSelectable = true;
    
    public Venue (String name, String coordinates) {
    	this.name=name;
    	setVenueLocation(coordinates);
    }
    
    public Venue (String name, GeoPoint latlong) {
    	this.name = name;
    	this.setLatlong(latlong);
    	
    }
    
    public Venue (String name, GeoPoint latlong, String url) {
    	this.name = name;
    	this.setLatlong(latlong);
    	this.image_url = url;
    	
    }
    public Venue (String name, GeoPoint latlong, Integer distance) {
    	this.name = name;
    	this.setLatlong(latlong);
    	this.distance = distance;
    }
    
	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDistance() {
		return distance;
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

		public void setLatlong(GeoPoint latlong) {
			this.latlong = latlong;
		}

		public GeoPoint getLatlong() {
			return latlong;
		}

		public void setVenueLocation(String venueLocation) {
			this.venueLocation = venueLocation;
		}

		public String getVenueLocation() {
			return venueLocation;
		}
}