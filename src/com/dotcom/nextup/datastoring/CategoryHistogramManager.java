package com.dotcom.nextup.datastoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONException;

import android.content.Context;

import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.CategoryHistogram;

public class CategoryHistogramManager {
	
	public static void storeHistogramToPhone(CategoryHistogram ch, Context context) throws JSONException {
		
		try {
			FileOutputStream fos = context.openFileOutput(context.getString(R.string.histogramFileName), Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ch);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static CategoryHistogram getHistogramFromPhone(Context context) {
		try {
			FileInputStream fis = context.openFileInput(context.getString(R.string.histogramFileName));
			ObjectInputStream ois  = new ObjectInputStream(fis);
			CategoryHistogram ch = (CategoryHistogram)ois.readObject();
			return ch;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Boolean containsHistogram(Context context) {
		 String[] filenames = context.fileList();
	        for (String name : filenames) {
	          if (name.equals(context.getString(R.string.histogramFileName))) {
	            return true;
	          }
	        }

	        return false;
	}
}
