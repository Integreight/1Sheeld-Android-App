package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
		((CheckBox) v.findViewById(R.id.showAgain))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						app.setShownTutAgain(isChecked);
					}
				});
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
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.tut_last_frag, container, false);
		return v;
	}
}
