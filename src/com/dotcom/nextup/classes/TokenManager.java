package com.dotcom.nextup.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.dotcom.nextup.R;
import com.dotcom.nextup.activities.FourSquareLoginPrompt;
import com.dotcom.nextup.categorymodels.CheckIn;
import com.dotcom.nextup.categorymodels.CheckInManager;
import com.dotcom.nextup.datastoring.Update;
import com.dotcom.nextup.oauth.AndroidOAuth;

public class TokenManager {

	public static String getToken(Context context, Boolean codeStored,
			String code, SharedPreferences pref, AndroidOAuth oa) {
		String token = null;
		if (codeStored) {
			if ((token = getTokenFromPreferences(context, pref)) == null) {
				retrieveToken(context, oa, code, pref);
				return token;
			}
			return token;
		}
		return token;
	}

	public static ArrayList<CheckIn> getCheckIns(Context context, String token,
			Boolean checkinsUpdated) {
		ArrayList<CheckIn> checkIns = new ArrayList<CheckIn>();
		if (token != null) {
			try {
				checkIns = CheckInManager.getCheckins(token, checkIns);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return checkIns;
	}

	public static Boolean updateHistograms(Context context,
			Boolean checkinsUpdated, ArrayList<CheckIn> checkIns,
			SharedPreferences pref) throws JSONException {
		if (!checkinsUpdated) {
			Update.update(pref, context
					.getString(R.string.updateTimePreferenceName), checkIns,
					context);
			checkinsUpdated = true;
		}
		return checkinsUpdated;
	}

	private static String getTokenFromPreferences(Context context,
			SharedPreferences pref) {
		String token = null;
		if (pref
				.contains(context.getString(R.string.accessTokenPreferenceName))) {
			token = pref.getString(context
					.getString(R.string.accessTokenPreferenceName), "Unknown");
			return token;
		} else {
			return token;
		}
	}

	private static String retrieveToken(Context context, AndroidOAuth oa,
			String code, SharedPreferences pref) {
		HttpGet request = new HttpGet(oa.getAccessTokenUrl(code));
		String token;
		DefaultHttpClient client = new DefaultHttpClient();
		Editor edit = pref.edit();
		try {

			HttpResponse resp = client.execute(request);
			int responseCode = resp.getStatusLine().getStatusCode();

			if (responseCode >= 200 && responseCode < 300) {

				String response = responseToString(resp);
				JSONObject jsonObj = new JSONObject(response);
				token = jsonObj.getString("access_token");
				edit.putString(context
						.getString(R.string.accessTokenPreferenceName), token);
				edit.commit();
				return token;
			}
		} catch (ClientProtocolException e) {
			Toast.makeText(context,
					"There was an error connecting to Foursquare",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			// TODO: Deal with this error
		} catch (IOException e) {
			Toast.makeText(context,
					"There was an error connecting to Foursquare",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			// TODO: Deal with this error
		} catch (JSONException e) {
			// This means that they probably revoked the token
			// We are going to clear the preferences and go back to
			// The login prompt
			e.printStackTrace();
			edit.remove(context.getString(R.string.accessCodePreferenceName));
			edit.remove(context.getString(R.string.accessTokenPreferenceName));
			edit.commit();
			Intent i = new Intent(context, FourSquareLoginPrompt.class);
			context.startActivity(i);
		}
		return null;
	}

	public static String getCode(Intent i, Context context, SharedPreferences pref) {
		String code = null;
		if (i.getData() != null) {
			code = i.getData().getQueryParameter("code");
			Editor e = pref.edit();
			e.putString(context.getString(R.string.accessCodePreferenceName), code);
			e.commit();
			return code;
		} else {
			if ((code=i.getStringExtra(context
					.getString(R.string.accessCodePreferenceName))) != "None") {
				code = i.getStringExtra(context
						.getString(R.string.accessCodePreferenceName));
				return code;
			}
		}
		return null;
	}

	public static String responseToString(HttpResponse resp)
			throws IllegalStateException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(resp
				.getEntity().getContent()));

		StringBuffer sb = new StringBuffer();

		String line = "";

		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		in.close();

		return sb.toString();
	}

}
