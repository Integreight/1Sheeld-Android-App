package com.integreight.onesheeld.shields.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.ShieldsOperationActivity;
import com.integreight.onesheeld.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.controller.TwitterShield.TwitterEventHandler;
import com.integreight.onesheeld.shields.controller.TwitterShield.checkLogin;

public class TwitterFragment extends SherlockFragment {

	TwitterShield twitterShield;
	ShieldsOperationActivity activity;
	TextView lastTweetTextView;
	TextView userNameTextView;
	MenuItem twitterLogin;
	MenuItem twitterLogout;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.twitter_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (activity.getFirmata() == null) {
			activity.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(activity.getFirmata());
		}

		checkLogin();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		lastTweetTextView = (TextView) getView().findViewById(
				R.id.twitter_shield_last_tweet_textview);
		userNameTextView = (TextView) getView().findViewById(
				R.id.twitter_shield_username_textview);
		activity = (ShieldsOperationActivity) getActivity();

	}

	private TwitterEventHandler twitterEventHandler = new TwitterEventHandler() {

		@Override
		public void onRecieveTweet(String tweet) {
			// TODO Auto-generated method stub
			lastTweetTextView.setText(tweet);
			Toast.makeText(activity, "Tweet posted!", Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onTwitterLoggedIn() {
			// TODO Auto-generated method stub
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					buttonToLoggedIn();
				}
			});
		}

		@Override
		public void onTwitterError(final String error) {
			// TODO Auto-generated method stub
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
					buttonToLoggedIn();

				}
			});

		}

	};

	private OneSheeldServiceHandler serviceHandler = new OneSheeldServiceHandler() {

		@Override
		public void onServiceConnected(ArduinoFirmata firmata) {
			// TODO Auto-generated method stub

			initializeFirmata(firmata);

		}

		@Override
		public void onServiceDisconnected() {
			// TODO Auto-generated method stub

		}
	};

	private void initializeFirmata(ArduinoFirmata firmata) {
		if (twitterShield != null)
			return;

		twitterShield = new TwitterShield(firmata, activity);
		twitterShield.setTwitterEventHandler(twitterEventHandler);
		checkLogin();
	}

	private void checkLogin() {
		if (twitterShield != null && twitterShield.isTwitterLoggedInAlready()) {
			buttonToLoggedIn();
		}

		else if (twitterShield != null
				&& !twitterShield.isTwitterLoggedInAlready()) {
			buttonToLoggedOut();
			Uri uri = activity.getIntent().getData();
			new checkLogin().execute(uri);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.twitter_shield_menu, menu);
		twitterLogin = (MenuItem) menu.findItem(R.id.login_to_twitter_menuitem);
		twitterLogout = (MenuItem) menu
				.findItem(R.id.logout_from_twitter_menuitem);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.logout_from_twitter_menuitem:
			logoutFromTwitter();
			return true;
		case R.id.login_to_twitter_menuitem:
			twitterShield.loginToTwitter();
			return true;
		}
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
		twitterShield.logoutFromTwitter();
		buttonToLoggedOut();
	}

	private void buttonToLoggedOut() {
		if(twitterLogout!=null)twitterLogout.setVisible(false);
		if(twitterLogin!=null)twitterLogin.setVisible(true);
		if(userNameTextView!=null)userNameTextView.setVisibility(View.INVISIBLE);
	}

	private void buttonToLoggedIn() {
		if(twitterLogin!=null)twitterLogin.setVisible(false);
		if(twitterLogout!=null)twitterLogout.setVisible(true);
		if(userNameTextView!=null)userNameTextView.setVisibility(View.VISIBLE);
		userNameTextView.setText("Logged in as: @"+twitterShield.getUsername());
	}

}
