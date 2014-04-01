package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.KeyboardShield;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class KeyboardFragment extends ShieldFragmentParent<KeyboardFragment> {
	EditText editText;
	Editable mytext;
	Button done;
	private KeyboardEventHandler eventHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.keyboard_shield_fragment_layout,
				container, false);
		return v;
	}

	@Override
	public void onStart() {

		super.onStart();

	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		editText = (EditText) getView().findViewById(R.id.keyboard_myEdit_txt);
		editText.setMaxLines(Integer.MAX_VALUE);
		editText.setSingleLine(false);
		done = (Button) getView().findViewById(R.id.keyboard_done_bt);

		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mytext = s;

			}
		});
		done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mytext.length() > 0) {
					if (eventHandler != null)
						eventHandler.onDonePressed(mytext);

				}
			}
		});
	}

	public void setKeyboardEventHandler(
			KeyboardEventHandler keyboardEventHandler) {
		this.eventHandler = keyboardEventHandler;

	}

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new KeyboardShield(getActivity(), getControllerTag()));

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setKeyboardEventHandler(((KeyboardShield) getApplication()
				.getRunningShields().get(getControllerTag()))
				.getKeyboardEventHandler());

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

	public static interface KeyboardEventHandler {
		void onDonePressed(Editable myText);
	}
}
