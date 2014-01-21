package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class MusicPlayerFragment extends
		ShieldFragmentParent<MusicPlayerFragment> {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.musicplayer_shield_fragment_layout,
				container, false);
	}
}
