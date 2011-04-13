package com.dotcom.nextup.activities;


import com.dotcom.nextup.R;
import com.google.android.maps.MapActivity;
import android.os.Bundle;



public class Map extends MapActivity{
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.maps);	    
	}
}; 