package com.dotcom.nextup.yelp;
/*
 * Yelp(), search()
 * Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
 * For a more complete example (how to integrate with GSON, etc) see the blog post above.
 * 
 * all other methods:
 * expand on the basic yelp searching capability, making it into a recommendation engine getRecommendation()
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

public class Yelp {
	String TAG = "Yelp";
	OAuthService service;
	Token accessToken;
	private static int MAX_CATS = 3;
	private static int MAX_CUSTOM_CATS = 2;
	private static int ONE_SEARCH_MAX_RESULTS = 10;
	private YelpVenue[][] venues_listed_by_category; // format [[a yelp venue, a yelp venue, ...], [a yelp venue, a yelp venue, ...], ...]
	private RecommendationInput input;
	private Integer[] threads_finished;
	
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
		Log.v(TAG, "entering Yelp()");
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
		this.venues_listed_by_category = new YelpVenue[MAX_CATS][ONE_SEARCH_MAX_RESULTS];
		this.threads_finished = new Integer[MAX_CATS];
		for (int i = 0; i < threads_finished.length; i++) {
			threads_finished[i] = 0;
		}
	}
	
	public ArrayList<YelpVenue> getRecommendation(RecommendationInput input) {
		/* THE RECOMMENDATION ENGINE - THREADED */
		Log.v(TAG, "entering getRecommendation");
		
		// narrow down the categories to search for
		this.input = narrowDownCategories(input);

		// create a thread to do a yelp search on each category
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int n = input.getCategories().size();
		for (int i = 0; i < n; i++) {
			Runnable run = new SearchOneCat(input.getCategories().get(i), i);
			Thread thread = new Thread(null, run, "getting best venues for a category");
			threads.add(thread);
			Log.v(TAG, "about to launch thread " + Integer.toString(i));
			thread.start();
		}
		
		// wait until all threads are done
		while ( isIn(0, threads_finished) ) {
			try { 
				Log.v(TAG, "getRecommendation waiting for all threads to finish");
				Thread.sleep(1000);
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// return all threads' results shuffled together
		return shuffle(venues_listed_by_category);
	}
	
	private RecommendationInput narrowDownCategories(RecommendationInput in) {
		/* narrow down the categories of places to search for to MAX_CATS
		 * try to choose MAX_CUSTOM_CATS from in.getCustomCategories()
		 * and MAX_CATS - MAX_CUSTOM_CATS from in.getCloudCategories()
		 * but if we can't do that, just make sure to return MAX_CATS categories
		 * 
		 * categories are better if they are visited more frequently, and if their
		 * average time of being visited is closer to the current time 
		 */
		Log.v(TAG, "entering narrowDownCategories");
		String freqs = "";
		for (Category cat: in.getCloudCategories()) {
			freqs = freqs + cat.getFrequency().toString() + " ";
		}
		Log.v(TAG, "unsorted frequencies: " + freqs);
		
		// sorting categories by frequency
		Collections.sort(in.getCustomCategories(), Collections.reverseOrder());
		Collections.sort(in.getCloudCategories(), Collections.reverseOrder());
		int now = getCurrentHours();
		
		freqs = "";
		for (Category cat: in.getCloudCategories()) {
			freqs = freqs + cat.getFrequency().toString() + " ";
		}
		Log.v(TAG, "sorted frequencies: " + freqs);
		
		// sorting categories by how close their average time is to now
		// (note that we do this by assigning the 'rating of how good the average time is'
		//  to the 'frequency' attribute of the category, since Category has a built-in 
		//  sort-by-frequency method)
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
		
		// choosing the right number of custom and cloud categories to return, and returning them
		ArrayList<Category> custom = pullFrom(in.getCustomCategories(), MAX_CUSTOM_CATS);
		return new RecommendationInput(custom, pullFrom(in.getCloudCategories(), MAX_CATS - custom.size()), 
				in.getLatitude(), in.getLongitude(), in.getMaxDistance(), in.getNumResultsDesired());
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
			getBestVenuesForOneCat(category, index);
		}		
	}
	
	public void getBestVenuesForOneCat(Category cat, int index) {
		/* each thread calls this method. 
		 * it does a yelp search for a single category and chooses the best venues from the results.
		 * then it saves those venues to a global variable, and updates a global variable to say it's finished.
		 */
		
		// get many venues as result of Yelp search
		Log.v(TAG, "thread " + Integer.toString(index) + " entering getBestVenueForOneCat(" + cat.getName() + ")");
		ArrayList<YelpVenue> many_venues = venuesSearch(cat.getName(), input.getLatitude(), input.getLongitude(), input.getMaxDistance());
		if (many_venues == null) {
			Log.v(TAG, "thread " + Integer.toString(index) + " has venuesSearch return null");
		} else {
			String results = "";
			for (YelpVenue venue : many_venues) {
				results = results + venue.getName() + " ";
			}
			Log.v(TAG, "thread " + Integer.toString(index) + " has venuesSearch return " + results);
		}
		
		Log.v(TAG, "thread " + Integer.toString(index) + " about to update venlist_by_cat");
		
		// narrow down to best venues
		ArrayList<YelpVenue> best_venues = chooseBest(many_venues);
		
		// add best venues to shared venues_listed_by_category[][]
		// ( each thread only interacts with its own venlist_by_cat[index][] )
		int i = 0;
		Log.v(TAG, "thread " + Integer.toString(index) + " i = " + Integer.toString(i));
		Log.v(TAG, "thread " + Integer.toString(index) + " venues_listed_by_category[index].length = " + Integer.toString(venues_listed_by_category[index].length));
		Log.v(TAG, "thread " + Integer.toString(index) + " many_venues.size() = " + Integer.toString(many_venues.size()));
		while (i < venues_listed_by_category[index].length && i < best_venues.size()) {
			Log.v(TAG, "thread " + Integer.toString(index) + " about to add " + best_venues.get(i).getName() + " to venues_listed_by_category");
			venues_listed_by_category[index][i] = best_venues.get(i);
			i++;
		}
		
		// record that this thread is finishing
		threads_finished[index] = 1;
	}
	
	public ArrayList<YelpVenue> chooseBest(ArrayList<YelpVenue> all_venues) {
		/* chooses the best venues from a list of venues
		 * venues are better if they have a higher rating and enough reviews
		 */
		Log.v(TAG, "entering chooseBest");
		Collections.sort(all_venues, Collections.reverseOrder());
		ArrayList<YelpVenue> best = new ArrayList<YelpVenue>();
		int i = 0;
		while ( best.size() <= (int)(input.getNumResultsDesired()/MAX_CATS) && i < all_venues.size()) {
			YelpVenue ven = all_venues.get(i);
			if ( ! isIn(ven, best) ) { 
				best.add(ven);
				Log.v("Yelp", "added " + ven.toString() + " to best");
			}
			i++;
		}
		return best;
	}
	
	private ArrayList<YelpVenue> shuffle(YelpVenue[][] vens_by_cat) {
		/* vens_by_cat is a list of lists of YelpVenues
		 * ex. [[a yelp venue, a yelp venue], [a yelp venue, a yelp venue]]
		 * each sublist is the result of a yelp search for one category
		 */
		Log.v(TAG, "entering shuffle for YelpVenue[][]");
		int ncats = vens_by_cat.length;       // the number of categories = the number of sublists
		Log.v(TAG, "shuffle(): ncats = " + Integer.toString(ncats));
		int max_nvens = 0;                    // length of longest sublist
		
		for (int i = 0; i < ncats; i++) {
			int n = vens_by_cat[i].length;
			if (n > max_nvens) max_nvens = n;
		}
		
		ArrayList<YelpVenue> res = new ArrayList<YelpVenue>();
		
		for (int veni = 0; veni < max_nvens; veni++) {
			for (int cati = 0; cati < vens_by_cat.length; cati++) {
				YelpVenue[] vens = vens_by_cat[cati];
				if (veni < vens.length)  {
					if (vens[veni] != null) {
						YelpVenue ven = vens[veni];
						if (!isIn(ven, res)) {
							res.add(ven);
						}
					}
				}
			}
		}
		Log.v(TAG, "shuffle(): returning " + Integer.toString(res.size()) + " results");
		return res;
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
	
	public ArrayList<YelpVenue> venuesSearch(String term, double latitude, double longitude, double max_distance) {
		Log.v("Yelp", "entering venuesSearch for term " + term);
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
		request.addQuerystringParameter("limit", Integer.toString(ONE_SEARCH_MAX_RESULTS));
		String term2 = convertTermToYelpForm(term);
		request.addQuerystringParameter("term", term2);
	    request.addQuerystringParameter("ll", latitude + "," + longitude);
	    request.addQuerystringParameter("radius_filter", Integer.toString((int)max_distance));
	    this.service.signRequest(this.accessToken, request);
	    Response response = request.send();
	    if (response == null) Log.v("Yelp", "response is null");
	    if (response.getCode() == -1)
	    	return null;
	    Log.v("Yelp", "search(): about to return response body from Yelp search");
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
		Log.v(TAG, "searchResponseToYelpVenues(): " + response);
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
	
	private boolean isIn(int num, Integer[] nums) {
		for (int n : nums) {
			if (n == num) return true;
		}
		return false;
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