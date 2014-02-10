package com.integreight.onesheeld.shields.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import twitter4j.TwitterException;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.gsm.GsmCellLocation;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.shields.controller.utils.Foursquare;
import com.integreight.onesheeld.shields.controller.utils.Foursquare.DialogListener;
import com.integreight.onesheeld.shields.controller.utils.FoursquareDialogError;
import com.integreight.onesheeld.shields.controller.utils.FoursquareError;
import com.integreight.onesheeld.utils.ControllerParent;

public class FoursquareShield extends ControllerParent<FoursquareShield> {

	private FoursquareEventHandler eventHandler;
	private static final byte FOURSQUARE_COMMAND = (byte) 0x1B;
	private static final byte CHECKIN_METHOD_ID = (byte) 0x01;
	Foursquare foursquare;
	String clintID = "Z5G4T1LGIAM4TLW3JVQH2DDKOTLN5XM1UG3W4V4LJ1VK2SX4";
	String clintSecret = "V2NLGZL3KRGZ42FUDTHY5D3EZG0TQXH0MD5M1ZHJVE25F2SD";
	String redirectUrl = "https://www.foursquare.com";
	String placeID = "";
	String message = "";
	public String userName="";
	public String lastcheckin="";

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	public FoursquareShield() {
		super();
	}

	@Override
	public ControllerParent<FoursquareShield> setTag(String tag) {
		getApplication().getAppFirmata().initUart();
		// getShareprefrences
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		
		if (!isFoursquareLoggedInAlready()) {
			Log.d("Foursquare::User not LoggedIn", "");
			foursquare = new Foursquare(clintID, clintSecret, redirectUrl);

			foursquare.authorize(getActivity(),
					new FoursquareAuthenDialogListener());
		}
		else 
		{
			Log.d("Foursquare::User LoggedIn", "");
			String userName = mSharedPreferences.getString(
					"PREF_FourSquare_UserName", "");
			String lastCheckin = mSharedPreferences.getString(
					"PREF_FourSquare_LastPlace", "");

			setUserName(userName);
			setLastcheckin(lastCheckin);
			
		}
		

		return super.setTag(tag);
	}

	public FoursquareShield(Activity activity, String tag) {
		super(activity, tag);
		// getShareprefrences
	}

	public boolean isFoursquareLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences
				.getBoolean("PREF_KEY_FOURSQUARE_LOGIN", false);
	}

	public void setFoursquareEventHandler(FoursquareEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		getApplication().getAppFirmata().initUart();
		CommitInstanceTotable();
	}

	public static interface FoursquareEventHandler {
		void onPlaceCheckin(String placeName);

		void onForsquareLoggedIn(String userName);

		void onForsquareLogout();

		void onForsquareError();

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == FOURSQUARE_COMMAND) {
			if (isFoursquareLoggedInAlready())
				if (frame.getFunctionId() == CHECKIN_METHOD_ID) {
					placeID = frame.getArgumentAsString(0);
					message = frame.getArgumentAsString(1);
					ConnectFour connectFour = new ConnectFour();
					connectFour.execute("");
				}
		}
	}

	private class ConnectFour extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				String foursquare_token = mSharedPreferences.getString(
						"PREF_FourSquare_OAUTH_TOKEN", null);
				String placeId = placeID;
				String messageId = URLEncoder.encode(message, "UTF-8");
				String checkinURLRequest = "https://api.foursquare.com/v2/checkins/add?venueId="
						+ placeId + "&" + "shout=" + messageId
						+ "&broadcast=public&oauth_token="
						+ foursquare_token + "&v=20140201";
				Log.d("checkinURLRequest", checkinURLRequest);
				HttpPost post = new HttpPost(checkinURLRequest);

				HttpClient hc = new DefaultHttpClient();
				HttpResponse rp = hc.execute(post);
				// Log.v(TAG,"response from server "+EntityUtils.toString(rp.getEntity()));
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// String response =
					// EntityUtils.toString(rp.getEntity().toString());
					Log.d("Response From Server ::", rp.toString());
					
				}
			} catch (Exception e) {
				Log.d("HTTP ERROR ::", e.toString());
			}
			return "";
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//parse checkin response !
		//	new ParseUserFoursquareData().execute("");
		}

	}
	
	 private class ParseUserFoursquareData extends AsyncTask<String, Void, String> {

	        @Override
	        protected String doInBackground(String... params) {
				String aa = null;

	        	try {

					aa = foursquare.request("users/self");
					// show and save prefrences user name , last place
					// checkin
					//jsonParser(aa);
					Log.d("Foursquare-Main", aa);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            return aa;
	        }

	        @Override
	        protected void onPostExecute(String result) {
	        	
	        	try {
	        		JSONObject json = new JSONObject(result);
				JSONObject response = json.getJSONObject("response");
				JSONObject user = response.getJSONObject("user");
				String userName = user.getString("firstName");
				// Set user name UI
				eventHandler.onForsquareLoggedIn(userName);
				JSONObject venue = user.getJSONObject("checkins")
						.getJSONArray("items").getJSONObject(0)
						.getJSONObject("venue");
				String placeName = venue.getString("name");
				eventHandler.onPlaceCheckin(placeName);

				// save in share prefrences
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				editor.putString("PREF_FourSquare_UserName", userName);
				editor.putString("PREF_FourSquare_LastPlace", placeName);

				// Commit the edits!
				editor.commit();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	            
	        }

	    }
	private class FoursquareAuthenDialogListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			new ParseUserFoursquareData().execute("");
		}

		@Override
		public void onFoursquareError(FoursquareError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(FoursquareDialogError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

	}

	public void jsonParser(String result) {
		try {
			JSONObject json = new JSONObject(result);
			JSONObject response = json.getJSONObject("response");
			JSONObject user = response.getJSONObject("user");
			String userName = user.getString("firstName");
			// Set user name UI
			eventHandler.onForsquareLoggedIn(userName);
			JSONObject venue = user.getJSONObject("checkins")
					.getJSONArray("items").getJSONObject(0)
					.getJSONObject("venue");
			String placeName = venue.getString("name");
			eventHandler.onPlaceCheckin(placeName);

			// save in share prefrences
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString("PREF_FourSquare_UserName", userName);
			editor.putString("PREF_FourSquare_LastPlace", placeName);

			// Commit the edits!
			editor.commit();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLastcheckin() {
		return lastcheckin;
	}

	public void setLastcheckin(String lastcheckin) {
		this.lastcheckin = lastcheckin;
	}

}
