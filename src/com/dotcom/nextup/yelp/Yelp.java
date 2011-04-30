package com.dotcom.nextup.yelp;
/*
Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
For a more complete example (how to integrate with GSON, etc) see the blog post above.
*/
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.util.Log;

import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.classes.RecommendationInput;

/**
* Example for accessing the Yelp API.
*/
public class Yelp {
	String TAG = "Yelp";
	OAuthService service;
	Token accessToken;
	private static int MAX_CATS = 3;
	private static int MAX_CUSTOM_CATS = 2;
	private ArrayList<ArrayList<YelpVenue>> venues_by_category; // the venues we'll recommend
	private RecommendationInput input;
	static class theLock extends Object { } // mutex class
	static public theLock lockObject = new theLock(); // instance of mutex
	private int threads_finished = 0;

   /**
	* Setup the Yelp API OAuth credentials.
	*
	* OAuth credentials are available from the developer site, under Manage API access (version 2 API).
	*
	* @param consumerKey Consumer key
	* @param consumerSecret Consumer secret
	* @param token Token
	* @param tokenSecret Token secret
	*/
	public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
		//Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
		this.venues_by_category = new ArrayList<ArrayList<YelpVenue>>();
	}
	
	public ArrayList<YelpVenue> getRecommendation2(RecommendationInput input) {
		/* THE RECOMMENDATION ENGINE */
		RecommendationInput input2 = narrowDownCategories(input);
		ArrayList<YelpVenue> all_venues = getManyPossibleVenues(input2);
		ArrayList<YelpVenue> rec = chooseBest(input2, all_venues);
		return rec;
	}
	
	public ArrayList<YelpVenue> getRecommendation(RecommendationInput input) {
		/* THE RECOMMENDATION ENGINE - THREADED */
		this.input = narrowDownCategories(input);

		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		int n = input.getCategories().size();
		for (int i = 0; i < n; i++) {
			Runnable run = new SearchOneCat(input.getCategories().get(i), i);
			Thread thread = new Thread(null, run, "getting best venues for a category");
			threads.add(thread);
			thread.start();
		}
		
		while ( threads_finished < n ) { // wait until all threads are done
			try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		return shuffle(venues_by_category);
	}
	
	private RecommendationInput narrowDownCategories(RecommendationInput in) {
		for (Category cat: in.getCustomCategories()) {
			Log.v("Yelp", cat.getFrequency().toString());
		}
		Collections.sort(in.getCustomCategories(), Collections.reverseOrder());
		Collections.sort(in.getCloudCategories(), Collections.reverseOrder());
		int now = getCurrentHours();
		for (Category cat: in.getCustomCategories()) {
			Log.v("Yelp", cat.getFrequency().toString());
		}
		
		for (int i = 0; i < in.getCustomCategories().size(); i++) {
			Category current = in.getCustomCategories().get(i);
			current.setFrequency(i + rankCategory(current, now));
		}
		
		for (int i = 0; i < in.getCloudCategories().size(); i++) {
			Category current = in.getCloudCategories().get(i);
			current.setFrequency(i + rankCategory(current, now));
		}
		
		Collections.sort(in.getCloudCategories());
		Collections.sort(in.getCustomCategories());

		ArrayList<Category> custom = pullFrom(in.getCustomCategories(), MAX_CUSTOM_CATS);
		
		return new RecommendationInput(custom, pullFrom(in.getCloudCategories(), MAX_CATS - custom.size()), 
				in.getLatitude(), in.getLongitude(), in.getMaxDistance(), in.getNumResultsDesired());
	}
	
	private ArrayList<Category> pullFrom(ArrayList<Category> categories, int num) {
		ArrayList<Category> ret = new ArrayList<Category>();
		int count = 0;
		while (count < num && count < categories.size()) {
			ret.add(categories.get(count));
			count++;
		}
		return ret;
	}
	
	private int rankCategory(Category cat, int now) {
		return abs(cat.getAverageTime()-now);
	}
	
	public static Integer getCurrentHours() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		return Integer.parseInt(sdf.format(cal.getTime()));
	}
	
	public void getBestVenueForOneCat(Category cat, int i) {
		Log.v("Yelp", "cat: " + cat.getName());
		ArrayList<YelpVenue> many_cats = venuesSearch(cat.getName(), input.getLatitude(), input.getLongitude(), input.getMaxDistance());
		synchronized (lockObject) {
			venues_by_category.add(many_cats);
			threads_finished++;
		}
	}
	
	public ArrayList<YelpVenue> shuffle(ArrayList<ArrayList<YelpVenue>> vens_by_cat) {
		int ncats = vens_by_cat.size();
		
		ArrayList<YelpVenue> res = new ArrayList<YelpVenue>();
		while ( !vens_by_cat.isEmpty() ) {
			for (int i = 0; i < ncats; i++) {
				ArrayList<YelpVenue> vens = vens_by_cat.get(i);
				if (!vens.isEmpty()) {
					YelpVenue ven = vens.get(0);
					res.add(ven);
					vens.remove(ven);
				}
			}
		}
		
		return res;
	}
	
	public ArrayList<YelpVenue> getManyPossibleVenues(RecommendationInput input) {
		/* part of the recommendation engine:
		 * does a yelp search() for each given category
		 * returns all results
		 * (note that right now the yelp search() limits itself to returning 3 venues to keep it from being too slow)
		 */
		Log.v("Yelp", "get many possible venues");
		double lat = input.getLatitude();
		double lon = input.getLongitude();
		double maxd = input.getMaxDistance();
		ArrayList<YelpVenue> all_venues = new ArrayList<YelpVenue>();
		ArrayList<YelpVenue> one_search = new ArrayList<YelpVenue>();
		for (Category cat : input.getCategories()) {
			if (cat.getName() == null)
				continue;
			Log.v("Yelp", "cat: " + cat.getName());
			one_search = venuesSearch(cat.getName(), lat, lon, maxd);
			if (one_search == null)
				continue;
			all_venues.addAll(one_search);
		}
		return all_venues;
	}
	
	public ArrayList<YelpVenue> venuesSearch(String term, double latitude, double longitude, double max_distance) {
		Log.v("Yelp", "entering venuesSearch");
		String response = this.search(term, latitude, longitude, max_distance);
		if (response == null)
			return null;
		return this.searchResponseToYelpVenues(response);
	}
	
    /**
     * Search with term and location.
     *
     * @param term Search term
     * @param latitude Latitude
     * @param longitude Longitude
     * @return JSON string response
     */
	public String search(String term, double latitude, double longitude, double max_distance) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
		request.addQuerystringParameter("limit", "10");
		String term2 = convertTermToYelpForm(term);
		request.addQuerystringParameter("term", term2);
	    request.addQuerystringParameter("ll", latitude + "," + longitude);
	    request.addQuerystringParameter("radius_filter", Integer.toString((int)max_distance));
	    this.service.signRequest(this.accessToken, request);
	    Response response = request.send();
	    if (response == null) Log.v("Yelp", "response is null");
	    if (response.getCode() == -1)
	    	return null;
	    Log.v("Yelp", "about to return response body from Yelp search");
	    return response.getBody();
	}
	
	private String convertTermToYelpForm(String term) { // "Middle Eastern"
		String lower = term.toLowerCase(); // "middle eastern"
		String[] pieces = lower.split(" "); // ["middle", "eastern"]
		String res = "";
		if (pieces != null) {
			if ( pieces.length > 0) {
				res = res + pieces[0];
				for (int i = 1; i < pieces.length; i++) {
					res = res + "+" + pieces[i];
				}
			}
		}
		return res; // "middle+eastern"
	}
	
	private ArrayList<YelpVenue> searchResponseToYelpVenues(String response) {
		Log.v(TAG, response);
		ArrayList<YelpVenue> venues = new ArrayList<YelpVenue>();
		try {
			JSONObject jresponse = new JSONObject(response);
			JSONArray jbusinesses = new JSONArray(jresponse.getString("businesses"));
			JSONObject jbus = new JSONObject();
			for (int i = 0; i < jbusinesses.length(); i++) {
				jbus = jbusinesses.getJSONObject(i);
				YelpVenue venue = new YelpVenue(jbus);
				venues.add(venue);
			}
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
		return venues;
	}
	
	public ArrayList<YelpVenue> chooseBest(RecommendationInput input, ArrayList<YelpVenue> all_venues) {
		Collections.sort(all_venues, Collections.reverseOrder());
		ArrayList<YelpVenue> best = new ArrayList<YelpVenue>();
		int i = 0;
		while ( best.size() < input.getNumResultsDesired() && i < all_venues.size()) {
			YelpVenue ven = all_venues.get(i);
			if ( ! isIn(ven, best) ) { 
				best.add(ven);
				Log.v("Yelp", "added " + ven.toString() + " to best");
			}
			i++;
		}
		return best;
	}
	
	public ArrayList<YelpVenue> chooseBest2(RecommendationInput input, ArrayList<YelpVenue> all_venues) {
		/* part of the recommendation engine
		 * given a lot of yelp venues to consider, returns the best
		 */
		ArrayList<YelpVenue> best = new ArrayList<YelpVenue>();
		YelpVenue venue;
		/* a list of (rank, venue) pairs is for the decorate, sort, undecorate sort method
		 * decorate: put each venue with its rank
		 * sort: list of (rank, venue) pairs by their rank
		 * undecorate: from list of venues sorted by rank, choose the highest-ranked venue
		 */
		ArrayList<NumObjectPair> options = new ArrayList<NumObjectPair>(); // will assign rank to each venue, then select venues with highest ranks
		for ( int i = 0; i < all_venues.size(); i++ ) {
			venue = all_venues.get(i);
			options.add(new NumObjectPair(rankYelpVenue(venue), venue));
		}
		Collections.sort(options); // I think the ones at the end have higher ranks
		/*for ( int i = options.size() - 1; i >= options.size() - 3; i--) {
			best.add((YelpVenue)options.get(i).getVenue());
		}*/
		
		int i = options.size() -1;
		while ( best.size() < input.getNumResultsDesired() && i >= 0) {
			YelpVenue ven = (YelpVenue) options.get(i).getObject();
			if ( ! isIn(ven, best) ) { 
				best.add(ven);
				Log.v("Yelp", "added " + ven.toString() + " to best");
			}
			i--;
		}
		return best;
	}

	private boolean isIn(YelpVenue venue, ArrayList<YelpVenue> venues) {
		for (YelpVenue ven : venues) {
			if ( venue.hasSameNameAs(ven)) return true;
		}
		return false;
	}
	
	private boolean isIn(Category kitty, ArrayList<Category> cats) {
		for (Category cat : cats) {
			if ( kitty.getName().equals(cat.getName())) return true;
		}
		return false;
	}
	
	private boolean isIn(int num, ArrayList<Integer> nums) {
		for (int n : nums) {
			if ( n == num ) return true;
		}
		return false;
	}
	
	private double rankYelpVenue(YelpVenue venue) {
		/* if a venue has too few reviews, its rating doesn't mean much */
		if ( venue.getReviewCount() > 3 ) { return venue.getRating(); }
		else { return venue.getRating()/2; }
	}
	
	public class SearchOneCat implements Runnable {
		Category category;
		int index;
		
		public SearchOneCat(Category cat, int i) {
			this.category = cat;
			this.index = i;
		}
		
		@Override
		public void run() {
			getBestVenueForOneCat(category, index);
		}
		
	}
	
	public class NumObjectPair implements Comparable<NumObjectPair> {
		/* for when you want to sort a list of objects based on 
		 * a number assigned to each object
		 * 
		 * convert the list of objects into a list of NumObjectPair
		 * and use Collections.sort, then extract the objects back out
		 */
		private double n;
		private Object o;
		
		public NumObjectPair(double n, Object o) {
			this.n = n;
			this.o = o;
		}
		
		public double getNum() { return n; }
		public Object getObject() { return o; }
		
		public int equals(NumObjectPair another) {
			// I think this method is actually super sketch and hopefully never gets called
			// but I'm gonna leave it be
			return Double.compare((Double) this.getNum(), (Double) another.getNum());
		}
		
		@Override
		public int compareTo(NumObjectPair another) {
			return Double.compare((Double) this.getNum(), (Double) another.getNum());
		}
		
		public String toString() {
			return "NumObjectPair num " + Double.toString(n) + " venue " + o.toString();
		}
	}
	
	public int min (int a, int b) {
		if (a < b) { return a; }
		else { return b; }
	}
	
	public int abs (int a) {
		if (a < 0) { return -1*a; }
		else { return a; }
	}
}