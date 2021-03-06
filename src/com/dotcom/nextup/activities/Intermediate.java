package com.dotcom.nextup.activities;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
	public GeoPoint currentLocation = null;
	public GeoPoint lastLocation;
	public String currentLocationName;
	public String lastLocationName;
	private String token;
	private String code;
	private static Boolean receivedLocationUpdate = false;
	@SuppressWarnings("unused")
	private static Boolean receivedLastLocationUpdate = false;
	private static Boolean checkinsUpdated = false;
	private static Boolean locationRegistered = false;
	private static Boolean codeStored = false;
	private CheckInManager checkInManager;
	private ArrayList<CheckIn> checkIns;
	private SharedPreferences pref;
	private Context context;
	private int currentSelectedVenue = -1;
	private Handler handler;	
	private Runnable fourSquare;
	private Runnable locationListening;
	private Runnable fourSquareAndLocation;
	private boolean foursquare_thread_done = false;
	private boolean location_thread_done = false;
	private ProgressDialog dialog;
	String TAG = "Intermediate";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v("Intermediate", "entering onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intermediate2);
		context = this;
		nearby_locations = new ArrayList<Venue>();
		oa = new AndroidOAuth(this);
		checkInManager = new CheckInManager();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		handler = new Handler();
		handler.postDelayed(noLocationFound, 15000);
		Button next = (Button)findViewById(R.id.Intermediate2Button);
		next.setVisibility(View.INVISIBLE);
		next.setClickable(false);
		dialog = ProgressDialog.show(this, "", "We are finding your location right now...", true);
		
		fourSquare = new Runnable() {
			@Override
			public void run() {
				connectWithFourSquare();
			}
		};
		
		locationListening = new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				findCurrentLocation();
			}
		};
		
		fourSquareAndLocation = new Runnable() {
			@Override
			public void run() {
				locationUpdateWithFoursquare();
			}
		};
		// does foursquare stuff, can work independently
		Thread foursquare_thread = new Thread(null, fourSquare, "Foursquare thread");
		foursquare_thread.start();
		Log.v(TAG, "just started foursquare_thread");
		
		// does location stuff, can work independently
		Thread location_thread = new Thread(null, locationListening, "Location thread");
		location_thread.start();
		Log.v(TAG, "just started location_thread");
		
		// can only do stuff after foursquare and location stuff finished
		Thread foursquare_and_location_thread = new Thread(null, fourSquareAndLocation, "Location + Foursquare thread");
		foursquare_and_location_thread.start();
		Log.v(TAG, "just started foursquare_and_location_thread");
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "entered onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.v(TAG, "entered onPause");
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}
	
	@Override
	public void onBackPressed() {
		Log.v(TAG, "entered onBackPressed");
		super.onDestroy();
	}
	
	/* ---------------- UI CODE BELOW -------------------*/

	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			Log.v(TAG, "running returnRes");
			TextView textview = (TextView) findViewById(R.id.Intermediate2Text);
			textview.setText("Nearby locations:");
			textview.setTextSize(16);
			updateSpinner();
		}
	};
	
	@SuppressWarnings("unchecked")
	public void updateSpinner() {
		Log.v(TAG, "entering updateSpinner");
		dialog.dismiss();
		Spinner spinner = (Spinner)findViewById(R.id.Intermediate2Spinner);
		spinner_locations = new ArrayList<CharSequence>();
		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinner_locations);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		if (nearby_locations != null && nearby_locations.size() > 0) {
			adapter.notifyDataSetChanged();
			for (int i = 0; i < nearby_locations.size(); i++)
				adapter.add(nearby_locations.get(i).getName());
			adapter.add("I'm not at any of these...");
			Button next = (Button)findViewById(R.id.Intermediate2Button);
			next.setVisibility(View.VISIBLE);
			next.setClickable(true);
		}
		if (nearby_locations == null || nearby_locations.size() == 0) {
			adapter.notifyDataSetChanged();
			adapter.add("No nearby locations found...");
			spinner.setClickable(false);
			Toast.makeText(context, getString(R.string.NotFoundInstructions), Toast.LENGTH_LONG).show();
			Button next = (Button)findViewById(R.id.Intermediate2Button);
			next.setVisibility(View.VISIBLE);
			next.setClickable(true);
		}
		adapter.notifyDataSetChanged();
	}
	
	Runnable noLocationFound = new Runnable() {
		@Override
		public void run() {
			//if (nearby_locations != null)
				//return;
			Log.v(TAG, "running noLocationFound");
			Intent i = new Intent(Intermediate.this, LocationNotFound.class);
			startActivity(i);			
		}
	};

	
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
	
	public void toYelp(View v) {
		String url = getString(R.string.yelppage);
		this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
				.parse(url)));
	}
	public void proceed(View view) throws IOException {
		Log.v(TAG, "entering proceed()");
		handler.removeCallbacks(noLocationFound);
		
		if (currentSelectedVenue == adapter.getCount() - 1) {
			Log.v(TAG, "proceed(): they chose the last option - I'm not at any of these places");
			Intent i = new Intent(this, LocationNotFound.class);
			startActivity(i);
			return;
		}
		
		Log.v(TAG, "proceed(): they chose a known location");
		Intent gotoHome = new Intent(this, Home.class);
		int numCats = 0;
		if (currentSelectedVenue != -1) {
			Venue selected;
			ArrayList<Category> cats;
			if (nearby_locations != null && nearby_locations.size() > 0) {
				selected = nearby_locations.get(currentSelectedVenue);
				
				cats = selected.getCategories();
	
				numCats = cats.size();
				
				/* Put Name */
				gotoHome.putExtra("name", selected.getName());
				 
				/* Put the number of categories */
				gotoHome.putExtra("numCats", numCats);
				
				/* Put each category as format category + index in list (for ex. category1, category2, etc.) */
				for (int j = 0; j < numCats; j++) {
					String key = "Category" + new Integer(j).toString();
					gotoHome.putExtra(key, (Parcelable)cats.get(j));
				}
				
				/*Put the location just for shits and giggles */
				gotoHome.putExtra("location", selected.getLatlong().getLatitudeE6() + "," + selected.getLatlong().getLongitudeE6());
			
				startActivity(gotoHome);
			}
		}		
	}
	
	/* -------------TOKEN/CODE STUFF BELOW------------*/
	
	private void connectWithFourSquare() {
		Log.v("Intermediate", "foursquare thread starting");
		
		if (code == null && codeStored == false)
			code = TokenManager.getCode(getIntent(), this, pref);
		
		if (code != null)
			codeStored = true;
		
		if (codeStored && this.checkIns == null) {
			if (code != null) {
				if(!code.equals("-1")) {
					try {
						initializeCheckIns();
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
			else {
				Log.v("Intermediate", "foursquare thread finds: code is null");
			}
		}
		Log.v("Intermediate", "foursquare thread finishing");
		foursquare_thread_done = true;
	}
	
	private void initializeCheckIns() throws JSONException {
		Log.v(TAG, "entering initializeCheckIns");
		this.token = TokenManager.getToken(context, codeStored, code, pref, oa);
		this.checkIns = TokenManager.getCheckIns(context, token, checkinsUpdated);
		if (this.checkIns.size() > 0) {
			Intermediate.checkinsUpdated = TokenManager.updateHistograms(context, checkinsUpdated, checkIns, pref);
			this.lastLocation = FoursquareLocationManager.getLastLocation(this.checkIns, this.checkInManager);
			this.lastLocationName = FoursquareLocationManager.getLastLocationName(this.checkIns, this.checkInManager);
		}
	}
	

	/* ----------------LOCATION CODE BELOW --------------------- */
	
	private void locationUpdateWithFoursquare() {
		Log.v(TAG, "entering locationUpdateWithFoursquare");
		while (!foursquare_thread_done || !location_thread_done) {
			try {
				Log.v("Intermediate", "final thread waiting for other two threads");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!receivedLocationUpdate) {
			try {
				updateLocationInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			receivedLocationUpdate = true;
		}
		runOnUiThread(returnRes);
	}
	
	private void findCurrentLocation() {
		//do both ways of finding location:  
		//  location listening (slower, more accurate)
		//  and last known location (faster, less accurate)
		
		Log.v(TAG, "entering findCurrentLocation");
		
		if (!locationRegistered) {
			try {
				initializeLocationListener();
				getLastKnownLocation();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		location_thread_done = true;
	}
	
	private void getLastKnownLocation() {
		Log.v(TAG, "entering getLastKnownLocation");
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		Location temp = null;
		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
			temp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
			handler.removeCallbacks(noLocationFound);
			if (currentLocationName == null)
				receivedLastLocationUpdate = true;
		}
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
			handler.removeCallbacks(noLocationFound);
			if (currentLocationName == null)
				receivedLastLocationUpdate = true;
		}
		
		locationRegistered = true;
	}
	
	private void initializeLocationListener() throws JSONException {
		Log.v(TAG, "entering initializeLocationListener");
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
				handler.removeCallbacks(noLocationFound);
				runOnUiThread(returnRes);
			}
		};
		
		locationRegistered = true;		
	}
	
	public void updateLocationInfo() throws JSONException {
		Log.v(TAG, "entering updateLocationInfo");
		try {
			JSONArray nearest = FoursquareLocationManager.getCurrentLocationDataFromFoursquare(currentLocation, token);
			nearby_locations = FoursquareLocationManager.getNearbyLocationsFromFoursquare(nearest, nearby_locations);
			nearest_location = FoursquareLocationManager.getNearestLocationFromFoursquare(nearby_locations);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
