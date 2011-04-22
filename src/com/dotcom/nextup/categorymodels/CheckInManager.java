package com.dotcom.nextup.categorymodels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class CheckInManager {
	public static ArrayList<CheckIn> getCheckins(String oauth_token,
			ArrayList<CheckIn> checkIns) throws MalformedURLException {
		String checkinsUrl = "https://api.foursquare.com/v2/users/self/checkins";
		String authUrl = checkinsUrl + "?oauth_token=" + oauth_token;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(authUrl);
		HttpResponse response;
		HttpEntity entity;
		//This is a hacky way to get around the unknown host exception
	    try {
	        @SuppressWarnings("unused")
			InetAddress i = InetAddress.getByName(authUrl);
	      } catch (UnknownHostException e1) {
	        e1.printStackTrace();
	      }
		try {
			response = httpClient.execute(getMethod);
			entity = response.getEntity();
			String contentString = convertStreamToString(entity.getContent());
			JSONObject responseObj = new JSONObject(contentString);
			checkIns = extractCheckInsFromJson(responseObj);
			return checkIns;
		} catch (Throwable t) {
			Log.e("Networking", "Exception in getStatus()", t);
			// Could be a problem with the access_token
			return checkIns;
		}
	}

	private static ArrayList<CheckIn> extractCheckInsFromJson(JSONObject json)
			throws JSONException {
		ArrayList<CheckIn> Checkins = new ArrayList<CheckIn>();
		JSONArray checkins = json.getJSONObject("response").getJSONObject(
				"checkins").getJSONArray("items");

		for (int i = 0; i < checkins.length(); i++) {
			ArrayList<Category> internalCategories = new ArrayList<Category>();
			String name = new String();
			JSONObject checkin = checkins.getJSONObject(i);
			Integer milliSinceEpoch = Integer.parseInt(checkin.getString("createdAt"))/1000;
			Date date = new Date(milliSinceEpoch);
			int time = date.getHours();
			JSONObject venue = checkin.getJSONObject("venue");
			int lat = (int) (Double.parseDouble(venue.getJSONObject("location").getString("lat")) * 1E6);
			int lon = (int) (Double.parseDouble(venue.getJSONObject("location").getString("lng")) * 1E6);
			GeoPoint checkInPoint = new GeoPoint(lat, lon);
			name = venue.getString("name");
			JSONArray cats = venue.getJSONArray(
					"categories");
			for (int j = 0; j < cats.length(); j++) {
				internalCategories.add(new Category(cats.getJSONObject(j).getString("name")));
			}
			CheckIn newCheckin = new CheckIn(time, internalCategories, checkInPoint, name, milliSinceEpoch);
			Checkins.add(newCheckin);
		}
		return Checkins;
	}

	public static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public CheckIn getLastCheckIn(ArrayList<CheckIn> checkins) {
		return checkins.get(checkins.size() - 1);
	}
	
	public GeoPoint getLastCheckInLocation(ArrayList<CheckIn> checkins) {
		return getLastCheckIn(checkins).getLocation();
	}
}
