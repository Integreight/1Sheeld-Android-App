package com.integreight.onesheeld.shields;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.helpers.AlertDialogManager;

public class TwitterShield {
	private ArduinoFirmata firmata;
	private static TwitterEventHandler eventHandler;
	private String lastTweet;
	Activity activity;
	private static final byte TWITTER_COMMAND = (byte) 0x30;
	private static final byte UPDATE_STATUS_METHOD_ID= (byte) 0x01;
	

	public String getUsername() {
		return  mSharedPreferences.getString(
				PREF_KEY_TWITTER_USERNAME, "");
	}

	static String TWITTER_CONSUMER_KEY = "Vx8p2DeuRnb650lh8K9n1w";
	static String TWITTER_CONSUMER_SECRET = "snSNAgKQ0z4EvGwhPNza0dys4iZipfpwRKr8FNydU";

	// Preference Constants
	// static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	static final String PREF_KEY_TWITTER_USERNAME = "TwitterUsername";

	static final String TWITTER_CALLBACK_URL = "oauth://onesheeld";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	public String getLastTweet() {
		return lastTweet;
	}

	public TwitterShield(ArduinoFirmata firmata, Activity activity) {
		this.firmata = firmata;
		this.activity = activity;
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
	}

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub
				if(data.length<2)return;
				byte command = data[0];
				byte methodId=data[1];
				int n = data.length - 2;
				byte[] newArray = new byte[n];
				System.arraycopy(data, 2, newArray, 0, n);
				if (command == TWITTER_COMMAND) {
					String tweet = new String(newArray);
					lastTweet = tweet;
					if (isTwitterLoggedInAlready())
						if(methodId==UPDATE_STATUS_METHOD_ID)
							new updateTwitterStatus().execute(tweet);

				}

			}
		});
	}

	public void setTwitterEventHandler(TwitterEventHandler eventHandler) {
		TwitterShield.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public static interface TwitterEventHandler {
		void onRecieveTweet(String tweet);

		void onTwitterLoggedIn();

		void onTwitterError(String error);
	}

	public void loginToTwitter() {
		// Check if already logged in

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				if (!isTwitterLoggedInAlready()) {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
					builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
					Configuration configuration = builder.build();

					TwitterFactory factory = new TwitterFactory(configuration);
					twitter = factory.getInstance();

					try {
						requestToken = twitter
								.getOAuthRequestToken(TWITTER_CALLBACK_URL);
						activity.startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse(requestToken.getAuthenticationURL())));
					} catch (TwitterException e) {
						e.printStackTrace();
						eventHandler.onTwitterError("Login Error");
					}
				} else {
					// activity.runOnUiThread(new Runnable() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					// // user already logged into twitter
					// Toast.makeText(activity.getApplicationContext(),
					// "Already Logged into twitter", Toast.LENGTH_LONG)
					// .show();
					//
					// }
					// });
					eventHandler.onTwitterError("Already Logged into twitter");
				}
				return null;
			}
		}.execute();

	}

	public boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	public void logoutFromTwitter() {
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.remove(PREF_KEY_TWITTER_USERNAME);
		e.commit();
	}

	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		// @Override
		// protected void onPreExecute() {
		// super.onPreExecute();
		// pDialog = new ProgressDialog(activity);
		// pDialog.setMessage("Updating to twitter...");
		// pDialog.setIndeterminate(false);
		// pDialog.setCancelable(false);
		// pDialog.show();
		// }

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token
				String access_token = mSharedPreferences.getString(
						PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(
						PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);

				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
				eventHandler.onTwitterError("Update status error");
			}
			return status;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String tweet) {
			// dismiss the dialog after getting all products
			// pDialog.dismiss();
			// updating UI from Background Thread
			// activity.runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			// // Toast.makeText(activity.getApplicationContext(),
			// // "Status tweeted successfully", Toast.LENGTH_SHORT)
			// // .show();
			// eventHandler.onTwitterStatusMessage("Status tweeted successfully");
			eventHandler.onRecieveTweet(tweet);
			// // Clearing EditText field
			// txtUpdate.setText("");
			// }
			// });
		}

	}

	public static class checkLogin extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {
			// TODO Auto-generated method stub
			Uri uri = params[0];
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					
					e.putString(PREF_KEY_TWITTER_USERNAME, user.getScreenName());
					e.commit(); // save changes

					// activity.runOnUiThread(new Runnable() {
					// public void run() {
					// // enableButtons();
					// // Displaying in xml ui
					// // lblUserName.setText(Html
					// // .fromHtml("<b>Welcome " + username
					// // + "</b>"));
					// }
					// });
					eventHandler.onTwitterLoggedIn();

				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage() + ":"
							+ e.getClass());
					eventHandler.onTwitterError("Login Error");
				}
			}
			return null;
		}

	}
}
