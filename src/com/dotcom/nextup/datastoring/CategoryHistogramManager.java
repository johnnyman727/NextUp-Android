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
