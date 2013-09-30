package com.integreight.onesheeld.shieldsfragments;

import android.content.Intent;
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
import com.facebook.Session;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.FacebookShield;
import com.integreight.onesheeld.shields.FacebookShield.FacebookEventHandler;

public class FacebookFragment extends SherlockFragment {

	FacebookShield facebookShield;
	ShieldsOperationActivity activity;
	TextView lastPostTextView;
	TextView userNameTextView;
	MenuItem facebookLogin;
	MenuItem facebookLogout;
	Bundle savedInstanceState;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.facebook_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);

		this.savedInstanceState=savedInstanceState;
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(getActivity(), requestCode,
				resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		lastPostTextView = (TextView) getView().findViewById(
				R.id.facebook_shield_last_post_textview);
		userNameTextView = (TextView) getView().findViewById(
				R.id.facebook_shield_username_textview);
		activity = (ShieldsOperationActivity) getActivity();

	}

	private FacebookEventHandler facebookEventHandler = new FacebookEventHandler() {

		@Override
		public void onRecievePost(String post) {
			// TODO Auto-generated method stub
			lastPostTextView.setText(post);
			Toast.makeText(activity, "Posted on your wall!", Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onFacebookLoggedIn() {
			// TODO Auto-generated method stub
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					buttonToLoggedIn();
					activity.setSupportProgressBarIndeterminateVisibility(false);
				}
			});
		}

		@Override
		public void onFacebookError(final String error) {
			// TODO Auto-generated method stub
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
					buttonToLoggedIn();
					activity.setSupportProgressBarIndeterminateVisibility(false);
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
		if (facebookShield != null)
			return;

		facebookShield = new FacebookShield(firmata, activity,this,savedInstanceState);
		facebookShield.setFacebookEventHandler(facebookEventHandler);
		checkLogin();
	}

	private void checkLogin() {
		if (facebookShield!=null&&facebookShield.isFacebookLoggedInAlready()) {
			buttonToLoggedIn();
		} else {
			buttonToLoggedOut();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.facebook_shield_menu, menu);
		facebookLogin = (MenuItem) menu
				.findItem(R.id.login_to_facebook_menuitem);
		facebookLogout = (MenuItem) menu
				.findItem(R.id.logout_from_facebook_menuitem);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.logout_from_facebook_menuitem:
			logoutFromFacebook();
			return true;
		case R.id.login_to_facebook_menuitem:
			loginToFacebook();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void logoutFromFacebook() {
		facebookShield.logoutFromFacebook();
		buttonToLoggedOut();
	}

	private void loginToFacebook() {

		facebookShield.loginToFacebook();
		activity.setSupportProgressBarIndeterminateVisibility(true);
	}

	private void buttonToLoggedOut() {
		facebookLogout.setVisible(false);
		facebookLogin.setVisible(true);
		userNameTextView.setVisibility(View.INVISIBLE);
	}

	private void buttonToLoggedIn() {
		facebookLogin.setVisible(false);
		facebookLogout.setVisible(true);
		userNameTextView.setVisibility(View.VISIBLE);
		userNameTextView.setText("Logged in as: "
				+ facebookShield.getUsername());
	}

}
