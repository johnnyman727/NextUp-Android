package com.dotcom.nextup.datastoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dotcom.nextup.categorymodels.CategoryHistogram;
import com.dotcom.nextup.categorymodels.Category;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryHistogramManager {
	
	public static void storeHistogramToPhone(CategoryHistogram ch, SharedPreferences pref, String prefName) {
			byte[] ret;
			if ((ret = convertCategoryHistogramTobytes(ch)) != null) {
				Editor edit = pref.edit();
				edit.putString(prefName, new String(ret));
				edit.commit();
			}
	}
	
	public static byte[] convertCategoryHistogramTobytes(CategoryHistogram map) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(baos);
			oo.writeObject(map.getMap());
			return baos.toByteArray();
		} catch (Exception e) {
			Log.e("Home.java", "Error storing Histogram");
		}
		return null;
	}
	
	public static CategoryHistogram getHistogramFromPhone(SharedPreferences pref, String prefName) {
		String map = pref.getString(prefName, null);
		CategoryHistogram ch = convertBytesToCategoryHistogram(map);
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	private static CategoryHistogram convertBytesToCategoryHistogram(String map) {
		try {
			byte[] mapBytes = map.getBytes();
			ByteArrayInputStream bais = new ByteArrayInputStream(mapBytes);
			ObjectInput oi = new ObjectInputStream(bais);
			CategoryHistogram ret = new CategoryHistogram();
			ret.setMap((HashMap<Category, ArrayList<Category>>) oi.readObject());
			return ret;
		}catch (Exception e) {
			return null;
		}
	}

	public static Boolean containsHistogram(SharedPreferences pref, String prefLocation) {
		return pref.contains(prefLocation);
	}
}
