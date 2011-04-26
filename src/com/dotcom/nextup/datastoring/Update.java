package com.dotcom.nextup.datastoring;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.CategoryHistogram;
import com.dotcom.nextup.categorymodels.CheckIn;

public class Update {
	
	private static Boolean containsUpdateTime(SharedPreferences pref, String updateTimeLocation) {
		return pref.contains(updateTimeLocation);
	}
	
	private static long getLastUpdateTime(SharedPreferences pref, String updateTimeLocation) {
		return pref.getLong(updateTimeLocation, -1);
	}
	
	public static void update(SharedPreferences pref, String updateTimeLocation, ArrayList<CheckIn> checkins, Context context) throws JSONException {
		/*
		 * If checkins is empty, return
		 */
		if (checkins == null || checkins.size() == 0)
			return;
		/*
		 * Collect New Checkins
		 */
		ArrayList<CheckIn> newCheckins = new ArrayList<CheckIn>();
		long updateTime = 0;
		long newUpdateTime;
		if (containsUpdateTime(pref, updateTimeLocation)){
			updateTime = getLastUpdateTime(pref, updateTimeLocation);
			newUpdateTime = updateTime;
			for (CheckIn c: checkins) {
				if (updateTime >= c.getCreatedAt())
					continue;
				else {
					if (newUpdateTime < c.getCreatedAt())
						newUpdateTime = c.getCreatedAt();
					newCheckins.add(c);
				}
			}
			/*
			 * Set new updatetime
			 */
			if ((updateTime < newUpdateTime)) {
				updateTime = newUpdateTime;
				Editor e = pref.edit();
				e.putLong(updateTimeLocation, updateTime);
				e.commit();
			}
			
		} else {
			updateTime = 0;
			for (CheckIn c: checkins) {
				if (c.getCreatedAt() >= updateTime) {
					updateTime = c.getCreatedAt();
				}	
				newCheckins.add(c);
			}		
			/*
			 * Set new updatetime
			 */
			Editor e = pref.edit();
			e.putLong(updateTimeLocation, updateTime);
			e.commit();
		}
		


		
		/*
		 * Update phone histogram with new checkins
		 */
		if (newCheckins.size() != 0) {
			if (CategoryHistogramManager.containsHistogram(pref, context.getString(R.string.histogramPreferenceName))) {
				CategoryHistogram storedMap = CategoryHistogramManager.getHistogramFromPhone(pref, context.getString(R.string.histogramPreferenceName));
				storedMap.addCheckInsToHistogram(newCheckins);
			} else {
				CategoryHistogram newMap = new CategoryHistogram();
				newMap.addCheckInsToHistogram(newCheckins);
				CategoryHistogramManager.storeHistogramToPhone(newMap, pref, context.getString(R.string.histogramPreferenceName));
			}
			
			/*
			 * Update cloud histogram with new checkins
			 */
			BackendManager.sendToCloud(newCheckins);
		}
	}
}
