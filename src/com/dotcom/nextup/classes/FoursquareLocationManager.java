package com.dotcom.nextup.classes;

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

import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;
import com.google.android.maps.GeoPoint;

public class FoursquareLocationManager {
	
	public static GeoPoint getLastLocation(ArrayList<CheckIn> checkIns, CheckInManager checkInManager) {
    	if (checkIns != null) {
    		CheckIn lastCheckIn = checkInManager.getLastCheckIn(checkIns);
    		return lastCheckIn.getLocation();
    	}
    	return null;
    }
    
    public static String getLastLocationName(ArrayList<CheckIn> checkIns, CheckInManager checkInManager) {
    	if (checkIns != null) {
    		CheckIn lastCheckIn = checkInManager.getLastCheckIn(checkIns);
    		return lastCheckIn.getName();
    	}
    	return null;
    }
    
    public static JSONArray getCurrentLocationDataFromFoursquare(GeoPoint location, String token) {
		if (token == null) {
			return null;
		}
		String url = "https://api.foursquare.com/v2/venues/search?ll=" + String.valueOf((location.getLatitudeE6()/1E6)) + "," + String.valueOf((location.getLongitudeE6()/1E6));
		String authUrl = url + "&oauth_token=" + token;
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
				JSONObject nearby = groups.getJSONObject(0);
				JSONArray close = nearby.getJSONArray("items");
				return close;
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
		return null;
	}
    
    public static Venue getNearestLocationFromFoursquare(ArrayList<Venue> venues) {
    	if (venues == null) 
    		return null;
    	return venues.get(0);
    }
    
	public static ArrayList<Venue> getNearbyLocationsFromFoursquare(JSONArray close, ArrayList<Venue> nearby_locations) throws JSONException {
		if (close == null)
			return null;
		if (nearby_locations != null)
			nearby_locations.clear();
		else
			nearby_locations = new ArrayList<Venue>();
		ArrayList<Category> cats = new ArrayList<Category>();
		for (int i = 0; i < close.length(); i++) {
			JSONObject nearbyPlace = close.getJSONObject(i);
			JSONObject location = nearbyPlace.getJSONObject("location");
			JSONArray categories = nearbyPlace.getJSONArray("categories");
			double distance = Double.parseDouble(location.getString("distance"));
			String name = nearbyPlace.getString("name");
			for (int j = 0; j < categories.length(); j++) {
				Category newCat = new Category(categories.getJSONObject(j).getString("name"), 1, 12);
				cats.add(newCat);
			}
			int lat = (int) (Double.parseDouble(location.getString("lat")) * 1E6);
			int lon = (int) (Double.parseDouble(location.getString("lng")) * 1E6);
			GeoPoint locationGeoPoint = new GeoPoint(lat, lon);
			Venue nearbyLocation;
			try {
				nearbyLocation = new Venue(name, "url", "imageurl", locationGeoPoint, distance, cats);
				nearby_locations.add(nearbyLocation);
			} catch (NumberFormatException e) {
				//TODO: Deal with this error
				e.printStackTrace();
			}
		}
		return nearby_locations;
	}
    
	public boolean sameLocation(GeoPoint l1, GeoPoint l2) {
		if ((l1.getLongitudeE6() == l2.getLongitudeE6()) && (l1.getLatitudeE6() == l2.getLatitudeE6()))
			return true;
		return false;
	}
}
