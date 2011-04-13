/*package com.dotcom.nextup.yelp;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class YelpWebScrape {
	static String TAG = "YelpWebScrape";
	public static String getYelpVenueInfo(String url, String attr) {
		 given the url of the yelp page for a venue
		 * and the user-friendly attribute name, so far it recognizes
		 *    "hours", "price range"
		 * returns a string of the requested attribute info for that venue
		 * error codes:
		 *    "bad attribute name" if an unrecognized attribute is requested
		 *    "could not connect" if problems with getting source code from yelp
		 *    "attribute not found" if source code doesn't have that attribute
		
		
		ArrayList<String> keys = translateAttr(attr);
		if (keys.get(0) == "bad attribute name") { return keys.get(0); }
		
		Document doc = null;
		try{
			doc = Jsoup.connect(url).get();
		} catch (Exception e) {
			Log.v(TAG, "Exception: " + e.getMessage());
			return "could not connect";
		}
		
		String doc_string = doc.toString();
		int i = doc_string.indexOf(keys.get(0));
		String relevant_section = doc_string.substring(i, i + 1000); // assume 1000 is enough to get all attr data
		Document doc_smaller = Jsoup.parse(relevant_section);
		
		return webScrapeSpecificAttr(attr, keys, doc_smaller);
	}
	
	private static String webScrapeSpecificAttr(String attr, ArrayList<String> keys, Document doc) {
		if (attr == "hours") {
			Elements hours = doc.getElementsByClass(keys.get(1));
			ArrayList<String> hours_list = new ArrayList<String>();
			for (Element hour : hours) {
				hours_list.add(hour.text());
			}
			String hours_display = "";
			String hours_item;
			for (int i = 0; i < hours_list.size(); i++) {
				hours_item = hours_list.get(i);
				if (i > 0) {
					hours_display = hours_display + ", " + hours_item;
				}
				else {
					hours_display = hours_display + hours_item;
				}
			}
			return hours_display;
		}
		
		if (attr == "price range") {
			Elements elems = doc.getElementsByClass(keys.get(1));
			for (Element elem : elems) {
				return elem.text();  // hack way to do it, only returns the first elem's text but that's what we need
			}
		}
		
		return "bad attribute name";
	}
	
	private static ArrayList<String> translateAttr(String user_attr) {
		 given user-friendly attribute name,
		 * returns ArrayList of the strings to search for on Yelp web page
		 * in order from higher-level to lower-level
		 * error codes:
		 *    ArrayList of only "bad attribute name" if an unrecognized attribute is requested
		 
		ArrayList<String> res = new ArrayList<String>();
		if (user_attr == "hours") {
			res.add("<dt class=\"attr-BusinessHours\">");
			res.add("hours");
			return res;
		}
		
		if (user_attr == "price range") {
			res.add("<dt class=\"attr-RestaurantsPriceRange2\">");
			res.add("pricerange"); //class
			res.add("price_tip");
			return res;
		}
		
		res.add("bad attribute name");
		return res;
	}
}
*/