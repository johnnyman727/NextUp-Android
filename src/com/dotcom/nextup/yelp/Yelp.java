package com.dotcom.nextup.yelp;
/*
Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
For a more complete example (how to integrate with GSON, etc) see the blog post above.
*/


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
		Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

  /**
* Search with term and location.
*
* @param term Search term
* @param latitude Latitude
* @param longitude Longitude
* @return JSON string response
*/
	public String search(String term, double latitude, double longitude) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
		request.addQuerystringParameter("limit", "3"); // seems to have no effect
		request.addQuerystringParameter("term", term);
	    request.addQuerystringParameter("ll", latitude + "," + longitude);
	    this.service.signRequest(this.accessToken, request);
	    Response response = request.send();
	    return response.getBody();
	}
  
	public ArrayList<YelpVenue> venuesSearch(String term, double latitude, double longitude) {
		String response = this.search(term, latitude, longitude);
		return this.searchResponseToYelpVenues(response);
	}
	
	public ArrayList<YelpVenue> searchResponseToYelpVenues(String response) {
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

}