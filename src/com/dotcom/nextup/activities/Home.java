package com.dotcom.nextup.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CategoryHistogram;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;
import com.dotcom.nextup.classes.RecommendationInput;
import com.dotcom.nextup.classes.Venue;
import com.dotcom.nextup.datastoring.CategoryHistogramManager;
import com.dotcom.nextup.oauth.AndroidOAuth;

public class Home extends ListActivity {
	private static final Integer SECONDS_IN_THREE_HOURS = 60 * 60 * 3;
	/** Called when the activity is first created. */
	private Boolean codeStored;
	private String token;
	private String code;
	private ArrayList<CheckIn> checkIns;
	private CategoryHistogram ch;
	private ArrayList<Venue> my_venues = null;
	private VenueAdapter m_adapter;
	private Runnable viewVenues;
	private String currentLocation;
	private SharedPreferences pref;
	private AndroidOAuth oa;
	@SuppressWarnings("unused")
	private CheckInManager checkInManager;
	private LocationManager locationManager;
	private LocationListener locationListener;
	public Location myLocation;
	@SuppressWarnings("unused")
	private String currentLocationName;
	@SuppressWarnings("unused")
	private String lastLocationName;
	private String lastLocationCoord;
	ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_checkedin);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		oa = new AndroidOAuth(this);
		ch = new CategoryHistogram();
		checkInManager = new CheckInManager();
		codeStored = getCode(getIntent());
		dialog = ProgressDialog.show(this, "Loading",
				"Creating Personal Recommendations...");
		dealWithCode(codeStored);
		dialog.dismiss();
		my_venues = new ArrayList<Venue>();
		this.m_adapter = new VenueAdapter(this, R.layout.row, my_venues);
		setListAdapter(this.m_adapter);
		viewVenues = new Runnable() {
			@Override
			public void run() {
				getVenues();
			}
		};

		Thread thread = new Thread(null, viewVenues, "MagentoBackground");
		thread.start();
	}

	public void onResume() {
		super.onResume();
		if (this.checkIns != null)
			this.checkInManager.getLastCheckInLocation(this.checkIns);
		getCurrentLocation();
	}
	
	public RecommendationInput getCategoryInput() {
		Category prefix = null;
		if (this.checkIns != null) {
			CheckIn last = this.checkInManager.getLastCheckIn(this.checkIns);
			Date date = new Date();
			if ((last.getTime() + SECONDS_IN_THREE_HOURS >= (date.getTime() * 1000 ))) {
				ArrayList<Category> cats = last.getCategories(); 
				if (cats != null) {
					prefix = cats.get(cats.size() - 1);
				}
				ArrayList<Category> topThree = this.ch.getTopThreeSuffixes(prefix);
				if (topThree != null) {
					String lat = last.getLocation().substring(0, last.getLocation().indexOf(","));
					String lon = last.getLocation().substring(last.getLocation().indexOf(",") + 1);
					double latitude = Double.parseDouble(lat);
					double longitude = Double.parseDouble(lon);
					RecommendationInput ri = new RecommendationInput(topThree,latitude, longitude);
					return ri;
				}	
			}
		} else {
			prefix = new Category();
			prefix.setCategoryFromCurrentLocation(this.myLocation);
			ArrayList<Category> topThree = this.ch.getTopThreeSuffixes(prefix);
			if (topThree != null) {
				RecommendationInput ri = new RecommendationInput(topThree, 
						this.myLocation.getLatitude(), 
						this.myLocation.getLongitude());
				return ri;
			}	
		}
		return null;		
	}

	/*----------------------- ACCESS TOKEN CODE BELOW --------------------*/
	
	private void dealWithCode(Boolean codeStored) {
		if (codeStored) {
			try {
				if (!getTokenFromPreferences()) {
					retrieveToken();
				}
				if (token != null) {
					if ((this.checkIns =CheckInManager.getCheckins(this.token, this.checkIns)) != null) {
						ch.createInitialHistogram(this.checkIns);
						if (!CategoryHistogramManager.containsHistogram(pref, getString(R.string.histogramPreferenceName)));
							CategoryHistogramManager.storeHistogram(this.ch, this.pref, getString(R.string.histogramPreferenceName));
					}
				}
			} catch (MalformedURLException e) {

			}
		}
	}

	private boolean getTokenFromPreferences() {
		if (this.pref.contains(getString(R.string.accessTokenPreferenceName))) {
			this.token = this.pref.getString(
					getString(R.string.accessTokenPreferenceName), "Unknown");
			return true;
		} else {
			return false;
		}
	}

	private void retrieveToken() {
		HttpGet request = new HttpGet(oa.getAccessTokenUrl(this.code));

		DefaultHttpClient client = new DefaultHttpClient();
		Editor edit = this.pref.edit();
		try {

			HttpResponse resp = client.execute(request);
			int responseCode = resp.getStatusLine().getStatusCode();

			if (responseCode >= 200 && responseCode < 300) {

				String response = responseToString(resp);
				JSONObject jsonObj = new JSONObject(response);
				this.token = jsonObj.getString("access_token");
				edit.putString(getString(R.string.accessTokenPreferenceName),
						this.token);
				edit.putString(getString(R.string.accessCodePreferenceName),
						this.code);
				edit.commit();
			}
		} catch (ClientProtocolException e) {
			Toast.makeText(Home.this,
					"There was an error connecting to Foursquare",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(Home.this,
					"There was an error connecting to Foursquare",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (JSONException e) {
			// This means that they probably revoked the token
			// We are going to clear the preferences and go back to
			// The login prompt
			e.printStackTrace();
			edit.remove(getString(R.string.accessCodePreferenceName));
			edit.remove(getString(R.string.accessTokenPreferenceName));
			edit.commit();
			Intent i = new Intent(Home.this, FourSquareLoginPrompt.class);
			startActivity(i);
		}
	}

	private Boolean getCode(Intent i) {
		if (i.getData() != null) {
			this.code = i.getData().getQueryParameter("code");
			return true;
		} else {
			if (i.getStringExtra(getString(R.string.accessCodePreferenceName)) != "None") {
				this.code = i
						.getStringExtra(getString(R.string.accessCodePreferenceName));
				return true;
			}
		}
		return false;
	}

	public static String responseToString(HttpResponse resp)
			throws IllegalStateException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(resp
				.getEntity().getContent()));

		StringBuffer sb = new StringBuffer();

		String line = "";

		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		in.close();

		return sb.toString();
	}


	/* ----------------LOCATION CODE BELOW --------------------- */

	private void getCurrentLocation() {
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				myLocation = location;
				dealWithLocation(Home.this.lastLocationCoord,
						Home.this.currentLocation);
			}
		};

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

		// get last locations
		if (locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
			myLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			myLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		dealWithLocation(this.lastLocationCoord, this.currentLocation);

	}

	private void getCurrentLocationName(String location) {
		String url = "https://api.foursquare.com/v2/venues/search?ll=" + location;		
		HttpClient hc = new DefaultHttpClient();
	
			try {
				HttpGet request = new HttpGet(url);
				HttpResponse resp = hc.execute(request);
				int responseCode = resp.getStatusLine().getStatusCode();

				if (responseCode >= 200 && responseCode < 300) {

					String response = responseToString(resp);
					JSONObject jsonObj = new JSONObject(response);
					JSONObject jsonObj2 = jsonObj.getJSONObject("groups");
					
					
				} 
			}catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/*---------------- UI CODE BELOW *----------------------*/

	private void dealWithLocation(String prev_loc, String cur_loc) {
		if (prev_loc == cur_loc && prev_loc != "null") {
			// they checked in at a place and are still there
			setContentView(R.layout.main_checkedin);
		}
		if (prev_loc != "null" && cur_loc == "null") {
			// they checked in at a place, we don't know where they are now
			// but we'll assume they're still there
			setContentView(R.layout.main_checkedin);
		}
		if (prev_loc == "null" && cur_loc != "null") {
			// we don't know their last check in, but we know where they are now
			// prompt them to check in
			setContentView(R.layout.main_notcheckedin);
		}
		if (prev_loc == cur_loc && prev_loc == "null") {
			// we dont know their last check in or where they are now
			// this.getLocation();
			setContentView(R.layout.main_notcheckedin); // need to change this
			// to main_wedontknow
			// later
		}

	}

	public void toFriends(View view) {
		Intent toFriends = new Intent(this, Friends.class);
		startActivity(toFriends);
	}

	public void toMap(View view) {
		Intent toMap = new Intent(this, Map.class);
		startActivity(toMap);
	}

	public void toPreferences(View view) {
		Intent toPreferences = new Intent(this, Preferences.class);
		startActivity(toPreferences);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(this, my_venues.get(position).getVenueName(),
				Toast.LENGTH_SHORT).show();

	}

	private class VenueAdapter extends ArrayAdapter<Venue> {

		private ArrayList<Venue> items;

		public VenueAdapter(Context context, int textViewResourceId,
				ArrayList<Venue> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			Venue o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(o.getVenueName());
				}
				if (bt != null) {
					bt.setText(o.getVenueLocation());
				}
			}
			return v;
		}
	}

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (my_venues != null && my_venues.size() > 0) {
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < my_venues.size(); i++)
					m_adapter.add(my_venues.get(i));
			}
			m_adapter.notifyDataSetChanged();
		}
	};

	private void getVenues() {
		try {
			my_venues = new ArrayList<Venue>();
			my_venues.add(new Venue("Craigie on Main", "123 Main, Cambridge"));
			my_venues.add(new Venue("Stephanie's on Newbury",
					"755 Newbury Street, Boston"));
			my_venues.add(new Venue("Falafel Palace",
					"75 Green Street, Cambridge"));
			Thread.sleep(5000);
			Log.i("ARRAY", "" + my_venues.size());
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		runOnUiThread(returnRes);
	}

}
