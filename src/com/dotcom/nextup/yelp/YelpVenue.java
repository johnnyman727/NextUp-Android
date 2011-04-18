package com.dotcom.nextup.yelp;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.dotcom.nextup.R;

import org.json.JSONObject;
import android.graphics.drawable.Drawable;
import android.util.Log;

/* search by category (and your lat long) - 3 best things as YelpVenue.
 * attributes of YelpVenue:  name, address, lat/long, five star rating, price range, categories
 * 
 * location: distance from user, then they click on it for more info
 * 
 */

public class YelpVenue {
	// attributes mimic http://www.yelp.com/developers/documentation/v2/business
	
	ArrayList<ArrayList<String>> categories = new ArrayList<ArrayList<String>>();
	/* ex. [["Local Flavor", "localflavor"],
	 *      ["Active Life", "active"],
	 *      ["Mass Media", "massmedia"]]
	 * first in each sublist is human readable,
	 * second in each sublist if for Yelp category search
	 */ 
	
	// id, name, url, phone, rating_img_url_small, image_url, address can be (null or empty string) on error
	String id;          // Yelp ID for this business
	String name;
	double rating;
	int review_count;
	String mobile_url;
	String url;
	String phone; 
	String rating_img_url_small;
	String image_url;
	String address;     // human readable address
	double distance;    // distance from search location
	double latitude;
	double longitude;
	boolean is_closed;  // whether the business is permanently closed
	
	// web scraped attributes
	String price_range; // '$', '$$', ... ,'$$$$$'
	String hours;       // human readable description of hours
	
	public YelpVenue() {
		
	}
	
	public YelpVenue(JSONObject jbus) {
		// makes a YelpVenue object out of a Yelp search result business JSON object
		Log.v("Yelp", jbus.toString());
		this.name = jbus.optString("name");
		this.id = jbus.optString("id");
		this.categories = this.stringToCategories(jbus.optString("categories"));
		this.image_url = jbus.optString("image_url");
		this.mobile_url = jbus.optString("mobile_url");
		this.url = jbus.optString("url");
		this.phone=jbus.optString("phone"); 
		this.rating_img_url_small = jbus.optString("rating_img_url_small");
		this.initializeRating();
		this.initializeAddressLocation(jbus.optJSONObject("location"));
		this.review_count = Integer.parseInt(jbus.optString("review_count"));
		this.distance = jbus.optDouble("distance");    // distance from search location if available
		
		/* web scraping makes it really slow and sometimes run out of memory
		this.hours = YelpWebScrape.getYelpVenueInfo(this.url, "hours");
		this.price_range = YelpWebScrape.getYelpVenueInfo(this.url, "price range");
		*/	
	}
	
	private void initializeRating() {
		/* rating_img_url_small is expected to be something like
		 * http://media3.px.yelpcdn.com/static/muchmessiness/i/ico/stars/stars_small_4.png
		 * or http://media3.px.yelpcdn.com/static/a;sldkjfasd;fkas;dfk/i/ico/stars/stars_small_4_half.png
		 */
		String[] pieces = this.rating_img_url_small.split("[\\Q[]\\E, \"]+");
		String piece = pieces[pieces.length - 1]; // stars_small_x.png or stars_small_x_half.png
		String rating_description = piece.substring(12); // x.png or x_half.png
		String rating_num = rating_description.substring(0); // x
		String maybe_half = rating_description.substring(2); // p or h
		this.rating = 0;
		if (maybe_half == "h") this.rating += .5;
		if (rating_num == "0") return;
		if (rating_num == "1") this.rating += 1;
		if (rating_num == "2") this.rating += 2;
		if (rating_num == "3") this.rating += 3;
		if (rating_num == "4") this.rating += 4;
		if (rating_num == "5") this.rating += 5;
	}
	
	public String toString() {
		return "Yelp Venue " + this.name + "\n" + this.address;
	}
	
	private void initializeAddressLocation(JSONObject loc) {
		this.address = this.stringToAddress(loc.optString("display_address"));
		JSONObject coord = loc.optJSONObject("coordinate");
		if (coord != null) {
			latitude = coord.optDouble("latitude");
			longitude = coord.optDouble("longitude");
		}	
	}
	
	private String stringToAddress(String loc) {
		ArrayList<String> temp = stringToList(loc);
		String res = "";
		for (int i = 0; i < temp.size(); i++) {
			res = res + temp.get(i) + " ";
		}
		return res;
	}

	private ArrayList<String> stringToList(String str) {
		String[] pieces = str.split("[\\Q[]\\E, \"]+");
		ArrayList<String> better = new ArrayList<String>();
		String piece;
		for (int i = 0; i < pieces.length; i++) {
			piece = pieces[i];
			if (!piece.isEmpty()) {
				better.add(piece);
			}
		}
		return better;
	}
	
	private ArrayList<ArrayList<String>> stringToCategories(String cats) {
		/* ex. [["Local Flavor", "localflavor"],
		 *      ["Active Life", "active"],
		 *      ["Mass Media", "massmedia"]]
		 * first in each sublist is human readable,
		 * second in each sublist if for Yelp category search
		 */ 
		ArrayList<String> many_cats = this.stringToList(cats);
		ArrayList<ArrayList<String>> clowder = new ArrayList<ArrayList<String>>(); // "a clowder of cats"
		ArrayList<String> pair_of_cats = new ArrayList<String>();
		int i = 1;
		while (i < many_cats.size() - 1) {
			pair_of_cats.clear();
			pair_of_cats.add(many_cats.get(i));
			pair_of_cats.add(many_cats.get(i+1));
			if (!clowder.contains(pair_of_cats)) {
				clowder.add(pair_of_cats);
			}
			i = i + 2;
		}
		return clowder;
	}
	
	public String getName() { return name; }
	public String getId() { return id; }
	public double getLatitude() { return latitude; }
	public double getLongitude() { return longitude; }
	public String getImageURL() { return image_url; }
	public String getURL() { return url; }
	public double getRating() { return rating; }
	public int getReviewCount() { return review_count; }
	public double getDistance() { return distance; }
	public void setDistance(double d) { distance = d; }

}