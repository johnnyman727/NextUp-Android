package com.dotcom.nextup.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;
import com.dotcom.nextup.classes.FoursquareLocationManager;
import com.dotcom.nextup.classes.TokenManager;
import com.dotcom.nextup.classes.Venue;
import com.dotcom.nextup.oauth.AndroidOAuth;
import com.google.android.maps.GeoPoint;

public class Intermediate extends Activity {
	ArrayList<Venue> nearby_locations = null;
	Venue nearest_location = null;
	ArrayAdapter<CharSequence> adapter;
	ArrayList<CharSequence> spinner_locations = null;

	ArrayList<Category> categories = new ArrayList<Category>();

	private AndroidOAuth oa;
	private LocationManager locationManager;
	private LocationListener locationListener;
	public GeoPoint currentLocation;
	public GeoPoint lastLocation;
	public String currentLocationName;
	public String lastLocationName;
	private String token;
	private String code;
	private static Boolean receivedLocationUpdate = false;
	private static Boolean receivedLastLocationUpdate = false;
	private static Boolean checkinsUpdated = false;
	private static Boolean locationRegistered = false;
	private static Boolean codeStored = false;
	private CheckInManager checkInManager;
	private ArrayList<CheckIn> checkIns;
	private SharedPreferences pref;
	private Context context;
	private int currentSelectedVenue = -1;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intermediate2);
		context = this;
		nearby_locations = new ArrayList<Venue>();
		oa = new AndroidOAuth(this);
		checkInManager = new CheckInManager();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (code == null && codeStored == false)
			code = TokenManager.getCode(getIntent(), this);
			if (code != null)
				codeStored = true;
		
		if (this.checkIns == null && !code.equals("-1"))
			initializeCheckIns();
		
		if (!locationRegistered)
			try {
				initializeLocationListener();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void onResume() {
		 super.onResume();
	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	     if (currentLocation != null && currentLocationName == null && !receivedLastLocationUpdate)
			try {
				updateLocationInfo();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}
	
	/* ---------------- UI CODE BELOW -------------------*/
	
	@SuppressWarnings("unchecked")
	public void updateSpinner() {
		Spinner spinner = (Spinner)findViewById(R.id.Intermediate2Spinner);
		spinner_locations = new ArrayList<CharSequence>();
		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinner_locations);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		
		if (nearby_locations != null && nearby_locations.size() > 0) {
			adapter.notifyDataSetChanged();
			for (int i = 0; i < nearby_locations.size(); i++)
				adapter.add(nearby_locations.get(i).getName());
		}
		adapter.notifyDataSetChanged();
	}
	
	OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
				long id) {
			Intermediate.this.currentSelectedVenue = position;
			Button button = (Button)findViewById(R.id.Intermediate2Button);
			button.requestFocus();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	};
	
	/* -------------TOKEN/CODE STUFF BELOW------------*/
	
	private void initializeCheckIns() {
			this.token = TokenManager.getToken(context, codeStored, code, pref, oa);
			this.checkIns = TokenManager.getCheckIns(context, token, checkinsUpdated);
			Intermediate.checkinsUpdated = TokenManager.updateHistograms(context, checkinsUpdated, checkIns, pref);
			this.lastLocation = FoursquareLocationManager.getLastLocation(this.checkIns, this.checkInManager);
			this.lastLocationName = FoursquareLocationManager.getLastLocationName(this.checkIns, this.checkInManager);		
	}
	

	/* ----------------LOCATION CODE BELOW --------------------- */
	private void initializeLocationListener() throws JSONException {
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				currentLocation = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
				if (!receivedLocationUpdate) {
					try {
						updateLocationInfo();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updateSpinner();
					receivedLocationUpdate = true;
				}
			}
		};

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		Location temp = null;
		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
			temp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
			if (currentLocationName == null) {
				updateLocationInfo();
				updateSpinner();
				receivedLastLocationUpdate = true;
			}
		}
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
			if (currentLocationName == null)
				updateLocationInfo();
				updateSpinner();
				receivedLastLocationUpdate = true;
		}
		
		locationRegistered = true;
		
	}
	
	public void updateLocationInfo() throws JSONException {
		JSONArray nearest = FoursquareLocationManager.getCurrentLocationDataFromFoursquare(currentLocation, token);
		nearby_locations = FoursquareLocationManager.getNearbyLocationsFromFoursquare(nearest, nearby_locations);
		nearest_location = FoursquareLocationManager.getNearestLocationFromFoursquare(nearby_locations);
	}
	
	public void toHome(View view) {
		Intent gotoHome = new Intent(this, Intermediate.class);
		int numCats = 0;
		
		
		if (currentSelectedVenue != -1) {
			Venue selected = nearby_locations.get(currentSelectedVenue);
			ArrayList<Category> cats = selected.getCategories();
			numCats = cats.size();
			/* Put the number of categories */
			gotoHome.putExtra("numCats", numCats);
			
			/* Put each category as format category + index in list (for ex. category1, category2, etc.) */
			for (int j = 0; j < numCats; j++) {
				String key = "Category" + new Integer(j).toString();
				gotoHome.putExtra(key, cats.get(j));
			}
			
			/*Put the location just for shits and giggles */
			gotoHome.putExtra("location", selected.getLatlong().getLatitudeE6() + "," + selected.getLatlong().getLongitudeE6());
			startActivity(gotoHome);
		}		
	}
}
