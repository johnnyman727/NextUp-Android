package com.dotcom.nextup.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.dotcom.nextup.yelp.Yelp;
import com.dotcom.nextup.yelp.YelpVenue;

public class Home extends ListActivity {
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
		/*codeStored = getCode(getIntent());
		dialog = ProgressDialog.show(this, "Loading",
				"Creating Personal Recommendations...");
		dealWithCode(codeStored);
		dialog.dismiss();

		if (this.checkIns != null)
			getLastLocation();
		getCurrentLocation();
		*/
		
		my_venues = new ArrayList<Venue>();
		this.m_adapter = new VenueAdapter(this, R.layout.row, my_venues);
		setListAdapter(this.m_adapter);
		
		/* @TODO: should only do this once location has been found */
		viewVenues = new Runnable() {
			@Override
			public void run() {
				getVenues();
			}
		};

		Thread thread = new Thread(null, viewVenues, "MagentoBackground");
		Log.v("Home", "about to start thread for getVenues");
		thread.start();
		
	}

	public void onResume() {
		super.onResume();
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

	private void getLastLocation() {
		this.lastLocationName = this.checkIns.get(0).getName();
		this.lastLocationCoord = this.checkIns.get(0).getLocation();
	}

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
				//getVenues();
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( my_venues.get(position).getURL() ) );
	    startActivity( browse );
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
			Log.v("Home", "entering getVenues()");
			/* version 1: just put in our own data
			my_venues = new ArrayList<Venue>();
			my_venues.add(new Venue("Craigie on Main", "123 Main, Cambridge"));
			my_venues.add(new Venue("Stephanie's on Newbury",
					"755 Newbury Street, Boston"));
			my_venues.add(new Venue("Falafel Palace",
					"75 Green Street, Cambridge"));
			Thread.sleep(5000);
			Log.i("ARRAY", "" + my_venues.size());
			*/
			/* version 2: yelp search based on RecommendationInput and filtering for best */
			Yelp yelp = getYelp();
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(new Category("cafe"));
			cats.add(new Category("dessert"));
			cats.add(new Category("coffee"));
			// throws NullPointerException due to myLocation
			//RecommendationInput input = new RecommendationInput(cats, myLocation.getLatitude(), myLocation.getLongitude());
			RecommendationInput input = new RecommendationInput(cats, 42.283, -71.23, 5000);
			ArrayList<YelpVenue> venues = yelp.getRecommendation(input);
			my_venues = new ArrayList<Venue>();
			
			for (int i = 0; i < venues.size(); i++) {
				YelpVenue yven = venues.get(i);
				Venue ven = new Venue(yven.getName(), yven.getLatitude(), yven.getLongitude(), yven.getURL(), yven.getImageURL());
				my_venues.add(ven);
			}

		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.toString());
		}
		runOnUiThread(returnRes);
	}
	
    private class VenueAdapter extends ArrayAdapter<Venue> {

    	private Context context;
    	private ArrayList<Venue> items;

    	public VenueAdapter(Context context, int textViewResourceId, ArrayList<Venue> items) {
    		super(context, textViewResourceId, items);
    		this.items = items;
    		this.context = context;
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
    			ImageView iv = (ImageView) v.findViewById(R.id.icon);
    			if (tt != null) {
    				tt.setText(o.getName());
    			}
    			if (bt != null) {
    				bt.setText(o.getName());
    			}
    			if (iv != null) {
    	    		Drawable image = ImageOperations(context, items.get(position).getImageURL(), "item" + Integer.toString(position) + ".jpg");
    	    		if (image == null) {
    	    			/* supposed to display this when the image can't be gotten from the url
    	    			 * but instead, no image displays, which is ok but doesn't look so good
    	    			 * probably returning null because it's an incorrect path name
    	    			 */
    	    			image = Drawable.createFromPath("../../../../../res/drawable/default_venue_image.png");
    	    		}

    				iv.setImageDrawable(image);
    			}
    		}
    		return v;
    	}
        
        /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/ 
         * returns a Drawable from a URL for an image
         * an ImageView can set itself to display this Drawable as its image
         * example usage:
         * ArrayList<YelpVenue> venues; //already got them using yelp.getRecommendation(), see getVenues() method in Home
         * Drawable image = ImageOperations(this,venues.get(0).rating_img_url_small,"image.jpg");
           ImageView imgView = (ImageView)findViewById(R.id.image1);
           imgView.setImageDrawable(image);
         */
    	private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
    		try {
    			InputStream is = (InputStream) this.fetch(url);
    			Drawable d = Drawable.createFromStream(is, saveFilename);
    			return d;
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    			return null;
    		} catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		}
    	}

        /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/
         * used by ImageOperations to get an image from a URL
         */
    	public Object fetch(String address) throws MalformedURLException,IOException {
    		URL url = new URL(address);
    		Object content = url.getContent();
    		return content;
    	}
    }
	
	/* like everything in Java, you need to make a Yelp object in order to actually do anything
	 * (actually there's a reason for this:  it authorizes you with the Yelp API)
	 */
    public Yelp getYelp() {
    	Log.v("Yelp", "entering getYelp()");
        String consumerKey = getString( R.string.oauth_consumer_key );
        String consumerSecret = getString( R.string.oauth_consumer_secret);
        String token = getString(R.string.oauth_token);
        String tokenSecret = getString(R.string.oauth_token_secret);
        
        Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
        
        return yelp;
    }
}
