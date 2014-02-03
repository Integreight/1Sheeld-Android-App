package com.integreight.onesheeld.shields.controller.utils;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;

public class SocialUtils {
	private Activity activity;
	TwitterFactory factory;
	Twitter twitter;
	RequestToken requestToken;
	private final String CALLBACKURL = "oob";
	String authUrl;

	public SocialUtils() {
		// TODO Auto-generated constructor stub
	}

	public SocialUtils(Activity activity) {
		super();
		this.activity = activity;
	}

	public void loginToTwitter() {
		factory = new TwitterFactory();
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(
				activity.getResources()
						.getString(R.string.twitter_consumer_key),
				activity.getResources().getString(
						R.string.twitter_consumer_secret));
		final Handler handle=new Handler();
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					try {
						requestToken = twitter
								.getOAuthRequestToken(CALLBACKURL);
						authUrl = requestToken.getAuthenticationURL();
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {

					final TwitterDialogListner listener = new TwitterDialogListner() {
						@Override
						public void onComplete() {
								SharedPreferences settings = activity
										.getSharedPreferences("com.integreight.onesheeld", 0);
								SharedPreferences.Editor editor = settings
										.edit();
								editor.putString(
										"FETCHED_ACCESS_TOKEN",
										SocialAuthorization.FETCHED_ACCESS_TOKEN);
								editor.putString(
										"FETCHED_SECRET_TOKEN",
										SocialAuthorization.FETCHED_SECRET_TOKEN);
								editor.putString("TWITTER_USER_NAME",
										SocialAuthorization.TWITTER_USER_NAME);
								editor.putString("FETCHED_IMAGE_URL",
										SocialAuthorization.FETCHED_IMAGE_URL);
								// Commit the edits!
								editor.commit();

						}

						@Override
						public void onError(String error) {
							Log.e("twitterError", error);
						}

						@Override
						public void onCancel() {

						}
					};
					handle.post(new Runnable() {

						@Override
						public void run() {
							TwitterDialog mDialog = new TwitterDialog(activity,
									authUrl, twitter, requestToken, listener);
							mDialog.show();
						}
					});

					super.onPostExecute(result);
				}

			}.execute(null, null);
	}
	public void tweet(final String tweet)
	{
		new AsyncTask<Void, Void, Void>() {
			protected void onPreExecute() {
			}
			@Override
			protected Void doInBackground(Void... params) {
				StatusUpdate st = new StatusUpdate(tweet
						+ activity.getResources().getString(
								R.string.app_name)
						+ " http://bit.ly/HUayMN");
				try {
					twitter.updateStatus(st);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				
			}


		}.execute(null, null);
	}
}
