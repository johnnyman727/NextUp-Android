package com.dotcom.nextup.datastoring;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CategoryHistogram;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;

public class BackendManager {
	
	public static void sendToCloud(ArrayList<Category> prefixes, ArrayList<Category> suffixes) {
		String baseUrl = "http://nextupandroid.appspot.com/backend/addition";
		String prefixMessage = "?prefixes=";
		String suffixMessage = "&suffixes=";
		for (Category prefix: prefixes) {
			if (prefixes.indexOf(prefix) > 0)
				prefixMessage += ",";
			prefixMessage+= prefix.getName();
		}
		for (Category suffix: suffixes) {
			if (suffixes.indexOf(suffix) > 0)
				suffixMessage += ",";
			suffixMessage+= suffix.getName() + ":" + suffix.getAverageTime();
		}
		
		String finalUrl = baseUrl + prefixMessage + suffixMessage;
		
		HttpClient hc = new DefaultHttpClient();
		HttpGet request = new HttpGet(finalUrl);
		try {
			hc.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public static void sendToCloud(ArrayList<CheckIn> checkins) {
		ArrayList<Category> prefixes = new ArrayList<Category>();
		ArrayList<Category> suffixes = new ArrayList<Category>();
		
		int last = checkins.size() - 1;
		int index;
		for (CheckIn checkin : checkins) {
			index = checkins.indexOf(checkin);
			if (index != last) {
				CheckIn otherCheckin = checkins.get(index + 1);
				if (CategoryHistogram.areSerial(checkin, otherCheckin)) {
					for (Category category : checkin.getCategories()) {
						for (Category otherCat : otherCheckin.getCategories()) {
								prefixes.add(category);
								suffixes.add(otherCat);
						}
					}
				}
			}
		}
		sendToCloud(prefixes, suffixes);
	}
	
	public static ArrayList<Category> getSuggestionsFromCloud(Category prefix) {
		String baseUrl = "http://nextupandroid.appspot.com/backend/suggestions";
		String prefixMessage = "?prefix=" + prefix.getName();
		ArrayList<Category> suggs = new ArrayList<Category>();
		String finalUrl = baseUrl + prefixMessage;
		
		HttpClient hc = new DefaultHttpClient();
		HttpGet request = new HttpGet(finalUrl);
		HttpResponse response;
		HttpEntity entity;
		try {
			response = hc.execute(request);
			entity = response.getEntity();
			String resp = CheckInManager.convertStreamToString(entity.getContent());
			JSONArray suggestions = new JSONArray(resp);
			for (int i = 0; i < suggestions.length(); i++) {
				String snuggie = suggestions.getString(i);
				String[] nameTime = snuggie.split(":");				
				Category suffixCat = new Category(nameTime[0]);
				suffixCat.setAverageTime(Integer.parseInt(nameTime[1]));
				suggs.add(suffixCat);
			}
		} catch (IOException e) {
			//TODO Auto-generated catch block
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return suggs;
	}
}
