package com.integreight.onesheeld.shields.controller;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.controller.utils.TwitterAuthorization;
import com.integreight.onesheeld.shields.controller.utils.TwitterDialog;
import com.integreight.onesheeld.shields.controller.utils.TwitterDialogListner;
import com.integreight.onesheeld.utils.AlertDialogManager;
import com.integreight.onesheeld.utils.ControllerParent;

public class TwitterShield extends ControllerParent<TwitterShield> {
	private TwitterEventHandler eventHandler;
	private String lastTweet;
	private static final byte TWITTER_COMMAND = (byte) 0x30;
	private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;

	public String getUsername() {
		return mSharedPreferences.getString(PREF_KEY_TWITTER_USERNAME, "");
	}

	// Preference Constants
	// static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	static final String PREF_KEY_TWITTER_USERNAME = "TwitterUsername";

	final String PREFS_NAME = "pref";
	TwitterFactory factory;
	Twitter twitter;
	RequestToken requestToken;
	private final String CALLBACKURL = "oob";
	String authUrl;

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	public String getLastTweet() {
		return lastTweet;
	}

	public TwitterShield() {
		super();
	}

	@Override
	public ControllerParent<TwitterShield> setTag(String tag) {
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		getApplication().getAppFirmata().initUart();
		return super.setTag(tag);
	}

	public TwitterShield(Activity activity, String tag) {
		super(activity, tag);
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		getApplication().getAppFirmata().initUart();
	}

	@Override
	public void onUartReceive(byte[] data) {
		if (data.length < 2)
			return;
		byte command = data[0];
		byte methodId = data[1];
		int n = data.length - 2;
		byte[] newArray = new byte[n];
		System.arraycopy(data, 2, newArray, 0, n);
		if (command == TWITTER_COMMAND) {
			String tweet = new String(newArray);
			lastTweet = tweet;
			if (isTwitterLoggedInAlready())
				if (methodId == UPDATE_STATUS_METHOD_ID) {
					tweet(tweet);
					eventHandler.onRecieveTweet(tweet);
				}

		}
		super.onUartReceive(data);
	}

	public void setTwitterEventHandler(TwitterEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		getApplication().getAppFirmata().initUart();
		CommitInstanceTotable();
	}

	public void tweet(final String tweet) {
		factory = new TwitterFactory();
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(
				mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null),
				mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null));
		AccessToken accestoken = new AccessToken(mSharedPreferences.getString(
				PREF_KEY_OAUTH_TOKEN, null), mSharedPreferences.getString(
				PREF_KEY_OAUTH_SECRET, null));
		twitter.setOAuthAccessToken(accestoken);
		StatusUpdate st = new StatusUpdate(tweet);
		try {
			twitter.updateStatus(st);
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

	public void login() {
		factory = new TwitterFactory();
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(TwitterAuthorization.CONSUMER_KEY,
				TwitterAuthorization.CONSUMER_SECRET);
		if (mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null) != null
				&& mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null) != null) {
			AccessToken accestoken = new AccessToken(
					mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null),
					mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null));
			twitter.setOAuthAccessToken(accestoken);
		} else {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					try {
						requestToken = twitter
								.getOAuthRequestToken(CALLBACKURL);
						authUrl = requestToken.getAuthenticationURL();
					} catch (TwitterException e) {

					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {

					final TwitterDialogListner listener = new TwitterDialogListner() {
						@Override
						public void onComplete() {
							SharedPreferences.Editor editor = mSharedPreferences
									.edit();
							editor.putString(PREF_KEY_OAUTH_TOKEN,
									TwitterAuthorization.FETCHED_ACCESS_TOKEN);
							editor.putString(PREF_KEY_OAUTH_SECRET,
									TwitterAuthorization.FETCHED_SECRET_TOKEN);
							editor.putString(PREF_KEY_TWITTER_USERNAME,
									TwitterAuthorization.TWITTER_USER_NAME);
							editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
							// Commit the edits!
							editor.commit();

						}

						@Override
						public void onError(String error) {
						}

						@Override
						public void onCancel() {

						}
					};
					new Handler().post(new Runnable() {

						@Override
						public void run() {
							TwitterDialog mDialog = new TwitterDialog(
									getActivity(), authUrl, twitter,
									requestToken, listener);
							mDialog.show();
						}
					});

					super.onPostExecute(result);
				}

			}.execute(null, null);

		}
	}

	public static interface TwitterEventHandler {
		void onRecieveTweet(String tweet);

		void onTwitterLoggedIn();

		void onTwitterError(String error);
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

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		
	}

}
