package com.dotcom.nextup.datastoring;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dotcom.nextup.categorymodels.CategoryHistogram;

public class CategoryHistogramManager {
	
	public static void storeHistogram(CategoryHistogram ch, SharedPreferences pref, String prefName) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(baos);
			oo.writeObject(ch);
			baos.toByteArray();
			Editor edit = pref.edit();
			edit.putString(prefName, new String (baos.toByteArray()));
			edit.commit();
		} catch (Exception e) {
			Log.e("Home.java", "Error storing Histogram");
		}				
	}
	
	public static Boolean containsHistogram(SharedPreferences pref, String prefLocation) {
		return pref.contains(prefLocation);
	}
}
