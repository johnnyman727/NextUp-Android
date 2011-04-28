package com.dotcom.nextup.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.classes.FoursquareLocationManager;
import com.dotcom.nextup.classes.Venue;
import com.google.android.maps.GeoPoint;

public class LocationNotFound extends Activity {
	private Context context;
	private Geocoder geoCoder;
	private String token;
	private SharedPreferences pref;
	private TextView currentAddress;
	private GeoPoint currentLocation;
	private ArrayList<Venue> nearby_locations;
	Venue nearest_location;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intermediatelocationnotfound);
		context = this;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		currentAddress = (TextView)findViewById(R.id.notFoundText);
		geoCoder = new Geocoder(context, Locale.getDefault());
		getToken();
		
	}

	private void getToken() {
		if (pref
				.contains(context.getString(R.string.accessTokenPreferenceName)))
			token = pref.getString(context
					.getString(R.string.accessTokenPreferenceName), "Unknown");
	}
	
	OnClickListener getAddress = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String location = "";
			if ((location = currentAddress.getText().toString()).equals("")) {
				Toast.makeText(context, "You must type an address", Toast.LENGTH_LONG).show();
				return;
			}
			try {
				Address address = geoCoder.getFromLocationName(location, 1).get(0);
				currentLocation = new GeoPoint((int)(address.getLatitude() * 1E6), (int)(address.getLongitude() * 1E6));
				JSONArray nearest = FoursquareLocationManager.getCurrentLocationDataFromFoursquare(currentLocation, token);
				nearby_locations = FoursquareLocationManager.getNearbyLocationsFromFoursquare(nearest, nearby_locations);
				nearest_location = FoursquareLocationManager.getNearestLocationFromFoursquare(nearby_locations);
				toHome(v);
			} catch (IOException e) {
				thereWasAnError();
				e.printStackTrace();
			} catch (JSONException e) {
				thereWasAnError();
				e.printStackTrace();
			}
			
		}
	};
	
	public void thereWasAnError() {
		Toast.makeText(context, "Sorry, we still can't find you.", Toast.LENGTH_LONG).show();
		LocationNotFound.this.finish();
	}
	public void toHome(View view) throws IOException {
		Intent gotoHome = new Intent(this, Home.class);
		int numCats = 0;

		Venue selected = nearest_location;
		ArrayList<Category> cats;
		if (nearest_location == null)
			return;
		cats = selected.getCategories();

		numCats = cats.size();

		/* Put Name */
		gotoHome.putExtra("name", selected.getName());

		/* Put the number of categories */
		gotoHome.putExtra("numCats", numCats);

		/*
		 * Put each category as format category + index in list (for ex.
		 * category1, category2, etc.)
		 */
		for (int j = 0; j < numCats; j++) {
			String key = "Category" + new Integer(j).toString();
			gotoHome.putExtra(key, (Parcelable) cats.get(j));
		}

		/* Put the location just for shits and giggles */
		gotoHome.putExtra("location", selected.getLatlong().getLatitudeE6()
				+ "," + selected.getLatlong().getLongitudeE6());

		startActivity(gotoHome);
	}
	
}
