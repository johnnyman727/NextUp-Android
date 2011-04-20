package com.dotcom.nextup.yelp;
/*
Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
For a more complete example (how to integrate with GSON, etc) see the blog post above.
*/
import java.util.ArrayList;
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
		Log.v("Yelp", "entering Yelp constructor");
		//Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}
	
	public ArrayList<YelpVenue> getRecommendation(RecommendationInput input) {
		/* THE RECOMMENDATION ENGINE */
		ArrayList<YelpVenue> all_venues = getManyPossibleVenues(input);
		ArrayList<YelpVenue> rec = chooseBest(input, all_venues);
		return rec;
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
			Log.v("Yelp", "cat: " + cat.getName());
			one_search = venuesSearch(cat.getName(), lat, lon, maxd);
			for (YelpVenue venue : one_search) {
				all_venues.add(venue);
			}
		}
		return all_venues;
	}
	
	public ArrayList<YelpVenue> venuesSearch(String term, double latitude, double longitude, double max_distance) {
		String response = this.search(term, latitude, longitude, max_distance);
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
		request.addQuerystringParameter("limit", "3");
		request.addQuerystringParameter("term", term);
	    request.addQuerystringParameter("ll", latitude + "," + longitude);
	    request.addQuerystringParameter("radius_filter", Double.toString(max_distance));
	    this.service.signRequest(this.accessToken, request);
	    Response response = request.send();
	    return response.getBody();
	}
	
	private ArrayList<YelpVenue> searchResponseToYelpVenues(String response) {
		ArrayList<YelpVenue> venues = new ArrayList<YelpVenue>();
		try {
			JSONObject jresponse = new JSONObject(response);
			Log.v(TAG, jresponse.toString());
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
		/* part of the recommendation engine
		 * given a lot of yelp venues to consider, returns the best 3
		 */
		ArrayList<YelpVenue> best = new ArrayList<YelpVenue>();
		YelpVenue venue;
		/* a list of (rank, venue) pairs is for the decorate, sort, undecorate sort method
		 * decorate: put each venue with its rank
		 * sort: list of (rank, venue) pairs by their rank
		 * undecorate: from list of venues sorted by rank, choose the highest-ranked venue
		 */
		ArrayList<RankVenuePair> options = new ArrayList<RankVenuePair>(); // will assign rank to each venue, then select venues with highest ranks
		for ( int i = 0; i < all_venues.size(); i++ ) {
			venue = all_venues.get(i);
			options.add(new RankVenuePair(rank(venue), venue));
		}
		Collections.sort(options); // I think the ones at the end have higher ranks
		for ( int i = options.size() - 1; i >= options.size() - 3; i--) {
			best.add((YelpVenue)options.get(i).getVenue());
		}
		return best;
	}

	private double rank(YelpVenue venue) {
		/* if a venue has too few reviews, its rating doesn't mean much */
		if ( venue.getReviewCount() > 3 ) { return venue.getRating(); }
		else { return venue.getRating()/2; }
	}
	
	public class RankVenuePair implements Comparable<RankVenuePair> {
		/* for comparing venues by their rank */
		private double rank;
		private YelpVenue venue;
		
		public RankVenuePair(double rank, YelpVenue venue) {
			this.rank = rank;
			this.venue = venue;
		}

		public double getRank() { return rank; }
		public YelpVenue getVenue() { return venue; }
		
		public int equals(RankVenuePair another) {
			return Double.compare((Double) this.getRank(), (Double) another.getRank());
		}

		@Override
		public int compareTo(RankVenuePair another) {
			return Double.compare((Double) this.getRank(), (Double) another.getRank());
		}
		
		public String toString() {
			return "RankVenuePair rank " + Double.toString(rank) + " venue " + venue.toString();
		}
	}
	
	public int min (int a, int b) {
		if (a < b) { return a; }
		else { return b; }
	}
}