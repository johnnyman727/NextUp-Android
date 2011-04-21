package com.dotcom.nextup.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Categories;
import com.dotcom.nextup.classes.Venue;
import com.google.android.maps.GeoPoint;

public class Intermediate extends Activity {
	ArrayList<Venue> nearby_locations = null;
	Venue nearest_location = null;
	ArrayAdapter<CharSequence> adapter;
	ArrayList<CharSequence> spinner_locations = null;
	Categories categories = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intermediate2);
		
		getNearbyLocations();
		
		displayNearbyLocations();
	}
	
	public void getNearbyLocations() {
		// get all nearby locations
		nearby_locations = new ArrayList<Venue>();
		int lat = (int)(42.283 * 1E6);
		int lon = (int)(-71.23 * 1E6);
		GeoPoint gp = new GeoPoint(lat, lon);
		Venue ven1 = new Venue("Olin", gp, 10);
		nearby_locations.add(ven1);
		Venue ven2 = new Venue("Babson", gp, 50);
		nearby_locations.add(ven2);
		Venue ven3 = new Venue("Wellesley", gp, 100);
		nearby_locations.add(ven3);
		
		// decide which one is closest for our default guess at user's location
		nearest_location = nearby_locations.get(0);
	}
	
	public void displayNearbyLocations() {
		Spinner spinner = (Spinner)findViewById(R.id.Intermediate2Spinner);
		spinner_locations = new ArrayList<CharSequence>();
		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinner_locations);
		spinner.setAdapter(adapter);
		
		if (nearby_locations != null && nearby_locations.size() > 0) {
			adapter.notifyDataSetChanged();
			for (int i = 0; i < nearby_locations.size(); i++)
				adapter.add(nearby_locations.get(i).getName());
		}
		adapter.notifyDataSetChanged();
	}
	
	public void toHome(View view) {
		Intent gotoHome = new Intent(this, Home.class);
		
		// pass it the categories to search for
		ArrayList<String> keys = categories.getKeys();
		ArrayList<String> values = categories.getValues();
		for (int i = 0; i < keys.size(); i++) {
			gotoHome.putExtra(keys.get(i), values.get(i));
		}
		startActivity(gotoHome);
	}
	

}
