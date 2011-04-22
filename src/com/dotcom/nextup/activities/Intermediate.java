package com.dotcom.nextup.activities;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;
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
	private Boolean receivedLocationUpdate = false;
	private Boolean checkinsUpdated = false;
	private Boolean locationRegistered = false;
	private Boolean codeStored = false;
	private String token;
	private String code;
	private CheckInManager checkInManager;
	private ArrayList<CheckIn> checkIns;
	private SharedPreferences pref;
	private Context context;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intermediate2);
		context = this;
		nearby_locations = new ArrayList<Venue>();
		oa = new AndroidOAuth(this);
		checkInManager = new CheckInManager();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (code == null)
			code = TokenManager.getCode(getIntent(), this);
			if (code != null)
				codeStored = true;
		
		if (this.checkIns != null && !code.equals("-1"))
			initializeCheckIns();
		
		if (!locationRegistered)
			initializeLocationListener();
	}

	public void onResume() {
		 super.onResume();
	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	     if (currentLocation != null && currentLocationName == null)
	     		getCurrentLocationDataFromFoursquare(currentLocation);
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
		
		if (nearby_locations != null && nearby_locations.size() > 0) {
			adapter.notifyDataSetChanged();
			for (int i = 0; i < nearby_locations.size(); i++)
				adapter.add(nearby_locations.get(i).getName());
		}
		adapter.notifyDataSetChanged();
	}
	
	/* -------------TOKEN/CODE STUFF BELOW------------*/
	
	private void initializeCheckIns() {
			this.token = TokenManager.getToken(context, codeStored, code, pref, oa);
			this.checkIns = TokenManager.getCheckIns(context, token, checkinsUpdated);
			this.checkinsUpdated = TokenManager.updateHistograms(context, checkinsUpdated, checkIns, pref);
			getLastLocation();
			getLastLocationName();		
	}
	

	/* ----------------LOCATION CODE BELOW --------------------- */
	private void initializeLocationListener() {
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
					getCurrentLocationDataFromFoursquare(currentLocation);
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
				getCurrentLocationDataFromFoursquare(currentLocation);
				updateSpinner();
			}
		}
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
			if (currentLocationName == null)
				getCurrentLocationDataFromFoursquare(currentLocation);
				updateSpinner();
		}
		
		locationRegistered = true;
		
	}

    public void getLastLocation() {
    	if (this.checkIns != null) {
    		CheckIn lastCheckIn = this.checkInManager.getLastCheckIn(this.checkIns);
    		this.lastLocation = lastCheckIn.getLocation();
    	}
    }
    
    private void getLastLocationName() {
    	if (this.checkIns != null) {
    		CheckIn lastCheckIn = this.checkInManager.getLastCheckIn(this.checkIns);
    		this.lastLocationName = lastCheckIn.getName();
    	}
    }
	private void getCurrentLocationDataFromFoursquare(GeoPoint location) {
		if (this.token == null) {
			return;
		}
		String url = "https://api.foursquare.com/v2/venues/search?ll=" + String.valueOf((location.getLatitudeE6()/1E6)) + "," + String.valueOf((location.getLongitudeE6()/1E6));
		String authUrl = url + "&oauth_token=" + this.token;
		HttpClient hc = new DefaultHttpClient();
	
		try {
			HttpGet request = new HttpGet(authUrl);
			HttpResponse resp = hc.execute(request);
			HttpEntity entity = resp.getEntity();
			String contentString = CheckInManager.convertStreamToString(entity.getContent());
			JSONObject responseObj = new JSONObject(contentString);
			int responseCode = resp.getStatusLine().getStatusCode();

			if (responseCode >= 200 && responseCode < 300) {
				JSONObject res = responseObj.getJSONObject("response");
				JSONArray groups = res.getJSONArray("groups");
				JSONObject nearby = groups.getJSONObject(1);
				JSONArray close = nearby.getJSONArray("items");
				JSONObject closest = close.getJSONObject(0);
				this.currentLocationName = closest.getString("name");
				getNearbyLocationsFromFoursquare(close);
			} 
		}catch (IllegalStateException e) {
			//TODO: Deal with this error
			e.printStackTrace();
		} catch (IOException e) {
			//TODO: Deal with this error
			e.printStackTrace();
		} catch (JSONException e) {
			//TODO: Deal with this error
			e.printStackTrace();
		}
	}
	
	private void getNearbyLocationsFromFoursquare(JSONArray close) throws JSONException {
		this.nearby_locations.clear();
		for (int i = 0; i < close.length(); i++) {
			JSONObject nearbyPlace = close.getJSONObject(i);
			JSONObject location = nearbyPlace.getJSONObject("location");
			int lat = (int) (Double.parseDouble(location.getString("lat")) * 1E6);
			int lon = (int) (Double.parseDouble(location.getString("lng")) * 1E6);
			GeoPoint locationGeoPoint = new GeoPoint(lat, lon);
			Venue newVenue;
			try {
				newVenue = new Venue(nearbyPlace.getString("name"), "url", "image url", locationGeoPoint, Integer.parseInt(location.getString("distance")));
				this.nearby_locations.add(newVenue);
				if (i == 0) {
					this.nearest_location = newVenue;
				}
			} catch (NumberFormatException e) {
				//TODO: Deal with this error
				e.printStackTrace();
			} catch (JSONException e) {
				//TODO: Deal with this error
				e.printStackTrace();
			}
		}	
	}
	
	public boolean sameLocation(GeoPoint l1, GeoPoint l2) {
		if ((l1.getLongitudeE6() == l2.getLongitudeE6()) && (l1.getLatitudeE6() == l2.getLatitudeE6()))
			return true;
		return false;
	}
	
	public void toHome(View view) {
		Intent gotoHome = new Intent(this, Home.class);
		
		// pass it the categories to search for
		for (int i = 0; i < categories.size(); i++) {
			String ii = Integer.toString(i);
			gotoHome.putExtra("cat"+ii, categories.get(i));
		}
		gotoHome.putExtra("num_categories", categories.size());
		
		// pass it current location
		gotoHome.putExtra("latitude", currentLocation.getLatitudeE6());
		gotoHome.putExtra("longitude", currentLocation.getLongitudeE6());
		
		startActivity(gotoHome);
	}

}
