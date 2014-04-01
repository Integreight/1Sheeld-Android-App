package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.controller.TwitterShield.TwitterEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class TwitterFragment extends ShieldFragmentParent<TwitterFragment> {

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

		initializeFirmata();
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		lastTweetTextView = (TextView) getView().findViewById(
				R.id.twitter_shield_last_tweet_textview);
		userNameTextView = (TextView) getView().findViewById(
				R.id.twitter_shield_username_textview);
		super.onActivityCreated(savedInstanceState);

	}

	private TwitterEventHandler twitterEventHandler = new TwitterEventHandler() {

		@Override
		public void onRecieveTweet(final String tweet) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				lastTweetTextView.post(new Runnable() {

					@Override
					public void run() {
						lastTweetTextView.setText(tweet);
						Toast.makeText(getActivity(), "Tweet posted!",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

		@Override
		public void onTwitterLoggedIn(final String userName) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				getActivity().runOnUiThread(new Runnable() {

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

			if (canChangeUI())
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT)
								.show();

					}
				});

		}

	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new TwitterShield(getActivity(), getControllerTag()));
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
		} else {
			userNameTextView.setVisibility(View.INVISIBLE);
			userNameTextView.setText("");
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
		inflater.inflate(R.menu.twitter_shield_menu, menu);
		twitterLogin = (MenuItem) menu.findItem(R.id.login_to_twitter_menuitem);
		twitterLogout = (MenuItem) menu
				.findItem(R.id.logout_from_twitter_menuitem);

		if (((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag())) != null
				&& ((TwitterShield) getApplication().getRunningShields().get(
						getControllerTag())).isTwitterLoggedInAlready()) {
			buttonToLoggedIn();
		} else {
			buttonToLoggedOut();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout_from_twitter_menuitem:
			logoutFromTwitter();
			return true;
		case R.id.login_to_twitter_menuitem:
			((TwitterShield) getApplication().getRunningShields().get(
					getControllerTag())).login();
			// buttonToLoggedIn();
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
		((TwitterShield) getApplication().getRunningShields().get(
				getControllerTag())).logoutFromTwitter();
		buttonToLoggedOut();
	}

	private void buttonToLoggedOut() {
		if (twitterLogout != null)
			twitterLogout.setVisible(false);
		if (twitterLogin != null)
			twitterLogin.setVisible(true);
		if (userNameTextView != null)
			userNameTextView.setVisibility(View.INVISIBLE);
	}

	private void buttonToLoggedIn() {
		if (twitterLogin != null) {
			twitterLogin.setVisible(false);
		}
		if (twitterLogout != null) {
			twitterLogout.setVisible(true);
		}
		if (userNameTextView != null)
			userNameTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void doOnServiceConnected() {
	}

}
