package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;

public class TutorialLastFragment extends Fragment {
	View v;

	public static TutorialLastFragment newInstance(int indx) {
		TutorialLastFragment fragment = new TutorialLastFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		final OneSheeldApplication app = (OneSheeldApplication) getActivity()
				.getApplication();
		final CheckBox cb = (CheckBox) v.findViewById(R.id.showAgain);
		cb.setChecked(true);
		v.findViewById(R.id.goBtn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						app.setShownTutAgain(((CheckBox) v
								.findViewById(R.id.showAgain)).isChecked());
						app.setTutShownTimes(app.getTutShownTimes() + 1);
						getActivity().finish();
					}
				});
		((ViewGroup) cb.getParent())
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						cb.setChecked(!((CheckBox) v
								.findViewById(R.id.showAgain)).isChecked());
					}
				});
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.tut_last_frag, container, false);
		return v;
	}
}
