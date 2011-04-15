package com.dotcom.nextup.activities;

import java.util.List;
import com.dotcom.nextup.R;
import com.dotcom.nextup.classes.VenuesMapOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.os.Bundle;



public class Map extends MapActivity{
	MapController mc;
    GeoPoint p;
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.maps);	  
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.mapspointer);
	    VenuesMapOverlay itemizedOverlay = new VenuesMapOverlay(drawable, this);
	    mc = mapView.getController();
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
	}
	
}; 