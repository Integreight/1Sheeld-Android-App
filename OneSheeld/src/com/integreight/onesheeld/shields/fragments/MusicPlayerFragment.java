package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.fragments.settings.MusicShieldSettings;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class MusicPlayerFragment extends
		ShieldFragmentParent<MusicPlayerFragment> {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.musicplayer_shield_fragment_layout,
				container, false);
	}

	@Override
	public void onStart() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		((MainActivity) getActivity())
				.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settingsViewContainer,
						MusicShieldSettings.getInstance()).commit();
		hasSettings = true;
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(false);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
}
