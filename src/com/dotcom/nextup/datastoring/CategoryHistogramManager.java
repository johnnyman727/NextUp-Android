package com.dotcom.nextup.datastoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dotcom.nextup.categorymodels.CategoryHistogram;
import com.dotcom.nextup.categorymodels.TempHist;

public class CategoryHistogramManager {
	
	public static void storeHistogramToPhone(CategoryHistogram ch, SharedPreferences pref, String prefName) throws JSONException {
		/*
		if (!containsHistogram(pref, prefName)) {
			JSONObject hist = new JSONObject();
			JSONArray pres = new JSONArray();
			for (Category prefix: ch.getMap().keySet()) {
				JSONObject prefDetails = new JSONObject();
				JSONArray suffixes = new JSONArray();
				for (Category suffix: ch.getMap().get(prefix)) {
					JSONObject suffDetails = new JSONObject().put(suffix.getName(), suffix.getFrequency()+":"+suffix.getAverageTime());
					suffixes.put(suffDetails);
				}
				prefDetails.put(prefix.getName(), suffixes.toString());
				pres.put(prefDetails);
			}
			hist.put("CustomHistogram", pres.toString());
		}
		String histString = pref.getString(prefName, null);
		
	}
		/*Map<String, String> map = (Map<String, String>) pref.getAll();
		ArrayList<String> prefixes = getPrefixes(ch.getMap().keySet());
		if (!map.isEmpty()) {
			Iterator<Entry<String, String>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> pairs = (Map.Entry<String, String>)iter.next();
				for (String prefix: prefixes) {
					if (pairs.getKey().equals(keyPrefix + prefix)) {
						String[] suffixes = pairs.getValue().split(",");
						for (int i = 0; i < suffixes.length; i++) {
							String[] attrs = suffixeJSONObject hist = new JSONObject();
		JSONArray pres = new JSONArray();
		for (Category prefix: ch.getMap().keySet()) {
			JSONObject prefDetails = new JSONObject();
			JSONArray suffixes = new JSONArray();
			for (Category suffix: ch.getMap().get(prefix)) {
				JSONObject suffDetails = new JSONObject().put(suffix.getName(), suffix.getFrequency()+":"+suffix.getAverageTime());
				suffixes.put(suffDetails);
			}
			prefDetails.put(prefix.getName(), suffixes.toString());
			pres.put(prefDetails);
		}
		hist.put("CustomHistogram", pres.toString());s[i].split(":");
							if (ch.getAllSuffixes(new Category(prefix)).contains(new Category(attrs[0]))) {
								Double freq = Double.parseDouble(attrs[1]);
								freq++;
								String newSuffix = attrs[0] + ":" + freq.toString() + ":" + attrs[2];
							}
						}
				}
				if (pairs.getKey().contains(prefixes. )) {
					for (Category c : ch.getMap().keySet())
				}
			}
		}  */
			String ret = null;
			TempHist th = new TempHist(ch);
			if ((ret = convertHashMapTobytes(th)) != null) {
				Editor edit = pref.edit();
				edit.putString(prefName, ret);
				edit.commit();
			}
	}
	
	public static CategoryHistogram getHistogramFromPhone(SharedPreferences pref, String prefName) {
		String map = pref.getString(prefName, null);
		if (map == null)
			return null;
		TempHist th = convertBytesToHashMap(map.getBytes());
		CategoryHistogram hash = th.toCatHist();
		return hash;
	}
	

	private static String convertHashMapTobytes(TempHist th) {
		try {
			String ret = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(baos);
			oo.writeObject(th);
			ret = baos.toString();
			oo.close();
			return ret;
		} catch (Exception e) {
			Log.e("Home.java", "Error storing Histogram");
		}
		return null;
	}
	private static TempHist convertBytesToHashMap(byte[] map) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(map);
			ObjectInput oi = new ObjectInputStream(bais);
			TempHist th = ((TempHist) oi.readObject());
			return th;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	
	public static Boolean containsHistogram(SharedPreferences pref, String prefLocation) {
		return pref.contains(prefLocation);
	}
}
