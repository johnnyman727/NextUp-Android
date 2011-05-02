package com.dotcom.nextup.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
	MapController mc;
	VenuesMapOverlay itemizedOverlay;
	List<Overlay> mapOverlays;
    GeoPoint p;
    ArrayList<Venue> venues;
    
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    	
	    try {
	    	Bundle b = getIntent().getExtras();
	    	venues = b.getParcelableArrayList("venues");
			setContentView(R.layout.maps);
			MapView mapView = (MapView) findViewById(R.id.mapview);
		    mapView.setBuiltInZoomControls(true);
		    mc = mapView.getController();
		    mapOverlays = mapView.getOverlays();
		    Drawable pointer = this.getResources().getDrawable(R.drawable.mapspointer);
		    itemizedOverlay = new VenuesMapOverlay(pointer, this);
		    putVenuesOnMap();

		} catch (NullPointerException e) {
			e.printStackTrace();
			setContentView(R.layout.fail);
		} catch (RuntimeException e) {
			e.printStackTrace();
			setContentView(R.layout.fail);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true; 
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Friends:    Intent toFriends = new Intent(this, Friends.class);	
    								startActivity(toFriends);
                                break;
            case R.id.Home:     Intent toHome = new Intent(this, Home.class);
            					startActivity(toHome);
                                break;
            case R.id.Preferences: Intent toPreferences = new Intent(this, Preferences.class);
            						startActivity(toPreferences);
            					break;
        }
        return true;
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
    		for (int i = 0; i < venues.size(); i++) {
    			Venue ven = venues.get(i);
    			itemizedOverlay.addOverlay(new OverlayItem(ven.getLatlong(), ven.getName(), Double.toString(ven.getRating())));
    		}
    		mapOverlays.add(itemizedOverlay);
    	}
    }
}