package com.dotcom.nextup.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.dotcom.nextup.R;
import com.dotcom.nextup.classes.Venue;
import com.dotcom.nextup.classes.VenuesMapOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Map extends MapActivity{
	MapView mapView;
	MapController mc;
	VenuesMapOverlay itemizedOverlay;
	VenuesMapOverlay currentLocationOverlay;
	List<Overlay> mapOverlays;
	
    GeoPoint p;
    ArrayList<Venue> venues;
    
    // info about current location
    double latitude;
    double longitude;
    double distance;
    String name;
    
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    	
	    try {
	    	getLocationAndVenues(getIntent());
			setContentView(R.layout.maps);
			mapView = (MapView) findViewById(R.id.mapview);
		    mapView.setBuiltInZoomControls(true);
		    mc = mapView.getController();
		    mapOverlays = mapView.getOverlays();
		    Drawable pointer = this.getResources().getDrawable(R.drawable.mapspointer);
		    itemizedOverlay = new VenuesMapOverlay(pointer, this);
		    Drawable pointergreen = this.getResources().getDrawable(R.drawable.mapspointergreen);
		    currentLocationOverlay = new VenuesMapOverlay(pointergreen, this);
		    putVenuesOnMap();

		} catch (NullPointerException e) {
			e.printStackTrace();
			setContentView(R.layout.fail);
		} catch (RuntimeException e) {
			e.printStackTrace();
			setContentView(R.layout.fail);
		}
	}
    
	public void getLocationAndVenues(Intent intent) {
    	Bundle b = intent.getExtras();
    	venues = b.getParcelableArrayList("venues");
    	latitude = b.getDouble("latitude");
    	longitude = b.getDouble("longitude");
    	distance = b.getDouble("max distance");
    	name = b.getString("name");
	}
	
    public void putVenuesOnMap() {
    	if (venues != null && venues.size() > 0) {
    		/*
	        String coordinates[] = {"42.283333", "-71.233333"};
	        double lat = Double.parseDouble(coordinates[0]);
	        double lng = Double.parseDouble(coordinates[1]);
	        
	        p = new GeoPoint(
	                (int) (lat * 1E6), 
	                (int) (lng * 1E6));
	        mc.animateTo(p);
	        mc.setZoom(15);
		    OverlayItem overlayitem = new OverlayItem(p, "Olin", "our map sorta works");
		    itemizedOverlay.addOverlay(overlayitem);
		    mapOverlays.add(itemizedOverlay);
		    */
    		
    		if ( !Double.isNaN(latitude) && !Double.isNaN(longitude) && name != null) {
	    		p = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
	    		currentLocationOverlay.addOverlay(new OverlayItem(p, name, "You are here."));
	    		mapOverlays.add(currentLocationOverlay);
	    		mc.animateTo(p);
    		}
    		
    		if (venues != null && venues.size() > 0) {
	    		for (int i = 0; i < venues.size(); i++) {
	    			Venue ven = venues.get(i);
	    			itemizedOverlay.addOverlay(new OverlayItem(ven.getLatlong(), ven.getName(), Double.toString(ven.getRating())));
	    		}
	    		mapOverlays.add(itemizedOverlay);
	    		mc.animateTo(venues.get(0).getLatlong());
    		}

	        mc.setZoom(13);
	        mapView.invalidate();
    	}
    }
}