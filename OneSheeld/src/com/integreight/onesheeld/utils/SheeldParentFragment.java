package com.integreight.onesheeld.utils;

import android.support.v4.app.Fragment;

public class SheeldParentFragment extends Fragment {
	public static SheeldParentFragment thisInstance;

	public SheeldParentFragment() {
	}

	public static SheeldParentFragment newInstance(String tag) {
		return thisInstance;
	}
	@Override
	public void onStop() {
		
		super.onStop();
	}

}