package com.dotcom.nextup.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CategoryManager;
import com.dotcom.nextup.classes.RecommendationInput;
import com.dotcom.nextup.classes.Venue;
import com.dotcom.nextup.yelp.Yelp;
import com.dotcom.nextup.yelp.YelpVenue;
import com.google.android.maps.GeoPoint;

public class Home extends ListActivity {
	Bundle bundle;
	ArrayList<Category> categories = new ArrayList<Category>();
	double latitude;
	double longitude;
	String name;
	double max_distance = 3000;
	
	private ArrayList<Venue> my_venues = null;
	private VenueAdapter m_adapter;
	ProgressDialog dialog = null;
	private Runnable viewVenues;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_checkedin);
		try {
			extractLocationData(getIntent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		my_venues = new ArrayList<Venue>();
		this.m_adapter = new VenueAdapter(this, R.layout.row, my_venues);
		setListAdapter(this.m_adapter);
		
		viewVenues = new Runnable() {
			@Override
			public void run() {
				getVenues();
			}
		};

		Thread thread = new Thread(null, viewVenues, "GettingVenuesThread");
		thread.start();
	}


	private void extractLocationData(Intent intent) throws IOException, ClassNotFoundException {
		//Pull location data
		String[] latlong = intent.getStringExtra("location").split(",");
		this.latitude = Double.parseDouble(latlong[0]);
		this.longitude = Double.parseDouble(latlong[1]);
		
		//Pull name
		this.name = intent.getStringExtra("name");
		
		//Pull Categories
		int iter = intent.getIntExtra("numCats", 0);
		
		for (int i = 0; i < iter; i++) {
			Category category = (Category)intent.getParcelableExtra("Category" + new Integer(i).toString());
			this.categories.add(category);
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
	    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( my_venues.get(position).getURL() ) );
	    startActivity( browse );
	}
	
	private void getCategories() {
		if (bundle == null) return;
		int n = bundle.getInt("num_categories");
		if (n == 0) return;
		for (int i = n; i < n; i++) {
			String ii = Integer.toString(i);
			categories.add((Category)bundle.get("cat"+ii));
		}
	}
	
	private void getLatLong() {
		if (bundle == null) return;
		latitude = bundle.getInt("latitude")/1E6;
		longitude = bundle.getInt("longitude")/1E6;
	}
	
	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if (my_venues != null && my_venues.size() > 0) {
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < my_venues.size(); i++)
					m_adapter.add(my_venues.get(i));
			}
			dialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};

	private void getVenues() {
		try {
			Log.v("Home", "entering getVenues()");
			/* uses up limited actual Yelp queries */
			Yelp yelp = getYelp();
			
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(new Category("cafe"));
			cats.add(new Category("dessert"));
			cats.add(new Category("coffee"));
			RecommendationInput input = new RecommendationInput(categories, latitude, longitude, max_distance);
			//RecommendationInput input = new RecommendationInput(cats, 42.283, -71.23, 5000);
			ArrayList<YelpVenue> venues = yelp.getRecommendation(input);
			my_venues = new ArrayList<Venue>();			
			for (int i = 0; i < venues.size(); i++) {
				YelpVenue yven = venues.get(i);
				int lat = (int)(yven.getLatitude() * 1E6);
				int lon = (int)(yven.getLongitude() * 1E6);
				GeoPoint gp = new GeoPoint(lat, lon);
				Venue ven = new Venue(yven.getName(), yven.getURL(), yven.getImageURL(), gp, yven.getDistance(), null);
				ven.setRating(yven.getRating());
				my_venues.add(ven);
			} 

		} catch (Exception e) {
			Log.e("getVenues()", e.toString());
		}
		runOnUiThread(returnRes);
	}
	
	/* like everything in Java, you need to make a Yelp object in order to actually do anything
	 * (actually there's a reason for this:  it authorizes you with the Yelp API) */
	 
    public Yelp getYelp() {
    	Log.v("Yelp", "entering getYelp()");
        String consumerKey = getString( R.string.oauth_consumer_key );
        String consumerSecret = getString( R.string.oauth_consumer_secret);
        String token = getString(R.string.oauth_token);
        String tokenSecret = getString(R.string.oauth_token_secret);
        
        Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
        
        return yelp;
    }
    
    public class VenueAdapter extends ArrayAdapter<Venue> {

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
    				bt.setText(Double.toString(o.getRating()));
    			}
    			if (iv != null) {
    	    		Drawable image = ImageOperations(context, items.get(position).getImageURL(), "item" + Integer.toString(position) + ".jpg");
    	    		if (image == null) {
    	    			/* supposed to display this when the image can't be gotten from the url
    	    			 * but instead, no image displays, which is ok but doesn't look so good
    	    			 * probably returning null because it's an incorrect path name */
    	    			 
    	    			image = Drawable.createFromPath("../../../../../res/drawable/default_venue_image.png");
    	    		}
    				iv.setImageDrawable(image);
    			}
    		}
    		return v;
    	}
    	
    	public ArrayList<Venue> getItems() { return items; }
        
        /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/ 
         * returns a Drawable from a URL for an image
         * an ImageView can set itself to display this Drawable as its image
         * example usage:
         * ArrayList<YelpVenue> venues; //already got them using yelp.getRecommendation(), see getVenues() method in Home
         * Drawable image = ImageOperations(this,venues.get(0).rating_img_url_small,"image.jpg");
           ImageView imgView = (ImageView)findViewById(R.id.image1);
           imgView.setImageDrawable(image); */
         
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
         * used by ImageOperations to get an image from a URL */
         
    	public Object fetch(String address) throws MalformedURLException,IOException {
    		URL url = new URL(address);
    		Object content = url.getContent();
    		return content;
    	}
    } 
}

