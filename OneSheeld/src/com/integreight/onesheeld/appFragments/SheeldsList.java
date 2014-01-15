package com.integreight.onesheeld.appFragments;

import com.integreight.onesheeld.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SheeldsList extends Fragment {
	View v;
	boolean isInflated = false;
	private ListView shieldsList;
	private static SheeldsList thisInstance;

	public static SheeldsList getInstance() {
		if (thisInstance == null) {
			thisInstance = new SheeldsList();
		}
		return thisInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		isInflated = false;
		if (v == null) {
			v = inflater.inflate(R.layout.app_sheelds_list, container, false);
			isInflated = true;
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setRetainInstance(true);
		if (isInflated) {
			initView();
		}
		super.onActivityCreated(savedInstanceState);
	}

	private void initView() {
		shieldsList = (ListView) getView().findViewById(R.id.sheeldsList);
	}
}
