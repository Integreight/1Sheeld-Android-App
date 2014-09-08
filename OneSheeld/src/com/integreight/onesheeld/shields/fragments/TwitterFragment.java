package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.controller.TwitterShield.TwitterEventHandler;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TwitterFragment extends ShieldFragmentParent<TwitterFragment> {

	LinearLayout lastTweetTextContainer;
	OneSheeldTextView userNameTextView;
	Button twitterLogin;
	Button twitterLogout;
	ProgressBar progress;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.twitter_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;

	}

	@Override
	public void onStart() {

		initializeFirmata();

		if (((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag())) != null
				&& ((TwitterShield) getApplication().getRunningShields().get(
						getControllerTag())).isTwitterLoggedInAlready()) {
			buttonToLoggedIn();
		} else {
			buttonToLoggedOut();
		}
		twitterLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (ConnectionDetector.isConnectingToInternet(activity))
					((TwitterShield) getApplication().getRunningShields().get(
							getControllerTag())).login();
				else
					Toast.makeText(
							getApplication().getApplicationContext(),
							"Please check your Internet connection and try again.",
							Toast.LENGTH_SHORT).show();

			}
		});
		twitterLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				logoutFromTwitter();
			}
		});
		// v.findViewById(R.id.twitter_shield_imageview).setOnClickListener(
		// new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// ((TwitterShield) getApplication().getRunningShields()
		// .get(getControllerTag())).uploadPhoto(
		// Environment.getExternalStorageDirectory()
		// + "/Pictures/Screenshots/test.png",
		// "Testing 1Sheeld");
		// }
		// });
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		lastTweetTextContainer = (LinearLayout) v.findViewById(R.id.tweetsCont);
		userNameTextView = (OneSheeldTextView) v
				.findViewById(R.id.twitter_shield_username_textview);
		twitterLogin = (Button) v.findViewById(R.id.login);
		twitterLogout = (Button) v.findViewById(R.id.logout);
		progress = (ProgressBar) v.findViewById(R.id.progress);
		super.onActivityCreated(savedInstanceState);

	}

	private TwitterEventHandler twitterEventHandler = new TwitterEventHandler() {

		@Override
		public void onRecieveTweet(final String tweet) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						OneSheeldTextView tweetItem = (OneSheeldTextView) activity
								.getLayoutInflater().inflate(
										R.layout.tweet_item,
										lastTweetTextContainer, false);
						tweetItem.setText(tweet);
						lastTweetTextContainer.addView(tweetItem);
						((ScrollView) lastTweetTextContainer.getParent())
								.invalidate();
					}
				});
			}
		}

		@Override
		public void onTwitterLoggedIn(final String userName) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						userNameTextView.setText("Logged in as: @" + userName);
						buttonToLoggedIn();
					}
				});
			}
		}

		@Override
		public void onTwitterError(final String error) {
			// TODO Auto-generated method stub

			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(activity, error, Toast.LENGTH_SHORT)
								.show();

					}
				});
			}

		}

		@Override
		public void startProgress() {
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (progress != null && canChangeUI()) {
						progress.setVisibility(View.VISIBLE);
					}
				}
			});
		}

		@Override
		public void stopProgress() {
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (progress != null && canChangeUI()) {
						progress.setVisibility(View.GONE);
					}
				}
			});
		}

	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new TwitterShield(activity, getControllerTag()));
		((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setTwitterEventHandler(twitterEventHandler);
		checkLogin();
	}

	private void checkLogin() {
		if (((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag())) != null
				&& ((TwitterShield) getApplication().getRunningShields().get(
						getControllerTag())).isTwitterLoggedInAlready()) {
			userNameTextView.setVisibility(View.VISIBLE);
			userNameTextView.setText("Logged in as: @"
					+ ((TwitterShield) getApplication().getRunningShields()
							.get(getControllerTag())).getUsername());
			twitterLogout.setVisibility(View.VISIBLE);
			twitterLogin.setVisibility(View.INVISIBLE);
			lastTweetTextContainer.setVisibility(View.VISIBLE);
			lastTweetTextContainer.removeAllViews();
			v.invalidate();
		} else {
			userNameTextView.setVisibility(View.INVISIBLE);
			userNameTextView.setText("");
			twitterLogout.setVisibility(View.INVISIBLE);
			twitterLogin.setVisibility(View.VISIBLE);
			lastTweetTextContainer.setVisibility(View.INVISIBLE);
			lastTweetTextContainer.removeAllViews();
			v.invalidate();
		}
	}

	/**
	 * Function to login twitter
	 * */

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	@Override
	public void onCreateOptionsMenu(android.view.Menu menu,
			android.view.MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Function to update status
	 * */

	/**
	 * Function to logout from twitter It will just clear the application shared
	 * preferences
	 * */
	private void logoutFromTwitter() {
		// Clear the shared preferences
		((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag())).logoutFromTwitter();
		buttonToLoggedOut();
	}

	private void buttonToLoggedOut() {
		if (twitterLogout != null) {
			twitterLogout.setVisibility(View.INVISIBLE);
		}
		if (twitterLogin != null) {
			twitterLogin.setVisibility(View.VISIBLE);
		}
		if (userNameTextView != null) {
			userNameTextView.setVisibility(View.INVISIBLE);
		}
		if (lastTweetTextContainer != null) {
			lastTweetTextContainer.setVisibility(View.INVISIBLE);
			lastTweetTextContainer.removeAllViews();
		}
		v.invalidate();
	}

	private void buttonToLoggedIn() {
		if (twitterLogout != null) {
			twitterLogout.setVisibility(View.VISIBLE);
		}
		if (twitterLogin != null) {
			twitterLogin.setVisibility(View.INVISIBLE);
		}
		if (userNameTextView != null) {
			userNameTextView.setVisibility(View.VISIBLE);
		}
		if (lastTweetTextContainer != null) {
			lastTweetTextContainer.setVisibility(View.VISIBLE);
			lastTweetTextContainer.removeAllViews();
		}
		v.invalidate();
	}

	@Override
	public void doOnServiceConnected() {
	}

}
