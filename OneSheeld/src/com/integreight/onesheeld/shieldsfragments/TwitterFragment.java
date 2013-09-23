package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.TwitterShield;
import com.integreight.onesheeld.shields.TwitterShield.TwitterEventHandler;

public class TwitterFragment extends Fragment {

	TwitterShield twitter;
	ShieldsOperationActivity activity;
	TextView lastTweetTextView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.twitter_shield_fragment_layout,
				container, false);
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

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		lastTweetTextView = (TextView) getView().findViewById(
				R.id.twitter_shield_last_tweet_textview);
		activity = (ShieldsOperationActivity) getActivity();
	}

	private TwitterEventHandler twitterEventHandler = new TwitterEventHandler() {

		@Override
		public void onRecieveTweet(String tweet) {
			// TODO Auto-generated method stub
			lastTweetTextView.setText(tweet);
			Toast.makeText(getActivity(), "Tweet posted!", Toast.LENGTH_SHORT).show();

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
		if (twitter != null)return;

		twitter = new TwitterShield(firmata);
		twitter.setTwitterEventHandler(twitterEventHandler);

	}

}
