package com.dotcom.nextup.datastoring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dotcom.nextup.categorymodels.Category;
import com.dotcom.nextup.categorymodels.CategoryHistogram;

public class CategoryHistogramManager {
	
	public static void storeHistogramToPhone(CategoryHistogram ch, SharedPreferences pref, String prefName) {
			byte[] ret;
			if ((ret = convertCategoryHistogramTobytes(ch)) != null) {
				Editor edit = pref.edit();
				edit.putString(prefName, new String(ret));
				edit.commit();
			}
	}
	public static void storeHistogramToCloud(CategoryHistogram ch, SharedPreferences pref, String prefName) {
		/*try {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(baos);
			oo.writeObject(ch);
			baos.toByteArray();
			
			String url = "https://nextup-android.appspot.com/nextupbackend/gethistogram";
			HttpPost post = new HttpPost(url);
			List<NameValuePair> form = new ArrayList<NameValuePair>();
			HttpClient hc = new DefaultHttpClient();
		
			try {
				HttpGet request = new HttpGet(authUrl);
				HttpResponse resp = hc.execute(request);
				HttpEntity entity = resp.getEntity();
				String contentString = CheckInManager.convertStreamToString(entity.getContent());
				JSONObject responseObj = new JSONObject(contentString);
				int responseCode = resp.getStatusLine().getStatusCode();

				if (responseCode >= 200 && responseCode < 300) {
					
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
		} catch (Exception e) {
			Log.e("Home.java", "Error storing Histogram");
		}		
		*/		
	}
		
	public static HashMap<Category, ArrayList<Category>> getMapFromCloud() {
		String url = "https://nextup-android.appspot.com/nextupbackend/gethistogram";
		HttpClient hc = new DefaultHttpClient();
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse resp = hc.execute(request);
			HttpEntity entity = resp.getEntity();
			return convertStreamToMap(entity.getContent());
		} catch (IOException e) {
			
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<Category, ArrayList<Category>> convertStreamToMap(InputStream is) {
		HashMap<Category, ArrayList<Category>> newMap = new HashMap<Category, ArrayList<Category>>();
		try {
			ObjectInputStream oi = new ObjectInputStream(is);
			newMap = (HashMap<Category, ArrayList<Category>>)(oi.readObject());
			oi.close();
			return newMap;
			
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static byte[] convertCategoryHistogramTobytes(CategoryHistogram map) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(baos);
			oo.writeObject(map);
			return baos.toByteArray();
		} catch (Exception e) {
			Log.e("Home.java", "Error storing Histogram");
		}
		return null;
	}
	
	public static Boolean containsHistogram(SharedPreferences pref, String prefLocation) {
		return pref.contains(prefLocation);
	}
}
