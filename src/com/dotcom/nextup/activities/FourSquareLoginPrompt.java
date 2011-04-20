package com.dotcom.nextup.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dotcom.nextup.R;
import com.dotcom.nextup.oauth.AndroidOAuth;

public class FourSquareLoginPrompt extends Activity {
	/** Called when the activity is first created. */
	private Button login;
	private Button nothanks;
	@SuppressWarnings("unused")
	private String code;
	@SuppressWarnings("unused")
	private static String LOG_TAG;
	private AndroidOAuth oauth;
	private SharedPreferences prefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foursquareloginprompt);
		login = (Button)findViewById(R.id.loginbutton);
		login.setOnClickListener(loginClick);
		nothanks = (Button)findViewById(R.id.nothanksbutton);
		nothanks.setOnClickListener(nothanksClick);
		LOG_TAG = "FourSquareLoginPrompt";
		oauth= new AndroidOAuth(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (checkIfCodeAlreadyExists()) {
			sendCode(retrieveCodeFromPreferences(), Home.class);
		}
	}
	
	public void onResume() {
		super.onResume();
		if (checkIfCodeAlreadyExists()) {
			sendCode(retrieveCodeFromPreferences(), Home.class);
		}
	}
	
	private void loginToFourSquare() {
			// Call the webbrowser with the Foursquare OAuth login URL
			String authUrl = oauth.getAuthenticationURL();
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse(authUrl)));
	}

	OnClickListener loginClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			loginToFourSquare();			
		}
	};
	
	OnClickListener nothanksClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			setCodePreference("-1");
		}
	};
	
	public Boolean checkIfCodeAlreadyExists() {
		return this.prefs.contains(getString(R.string.accessCodePreferenceName));
	}
	
	private String retrieveCodeFromPreferences() {
		return this.prefs.getString(getString(R.string.accessCodePreferenceName), "None");
	}
	
	private void setCodePreference(String newPref) {
		String nullCode = "-1";
		Editor edit = this.prefs.edit();
		edit.putString(getString(R.string.accessCodePreferenceName), nullCode);
		edit.commit();
		sendCode(nullCode, Home.class);
	}
	
	@SuppressWarnings("unchecked")
	private void sendCode(String code, Class c) {
		Intent i = new Intent(this, c);
		i.putExtra(getString(R.string.accessCodePreferenceName), code);
		startActivity(i);
	}
}
