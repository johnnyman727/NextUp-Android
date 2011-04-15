package com.dotcom.nextup.activities;

import com.dotcom.nextup.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;



public class Preferences extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.preferences);
	   	    
	}
	public void toFriends(View view) {
		Intent toFriends = new Intent(this, Friends.class);
		startActivity(toFriends);
	}

	public void toMap(View view) {
		Intent toMap = new Intent(this, Map.class);
		startActivity(toMap);
	}

	public void toPreferences(View view) {
		Intent toPreferences = new Intent(this, Preferences.class);
		startActivity(toPreferences);
	}
}; 