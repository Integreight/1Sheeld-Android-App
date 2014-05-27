package com.integreight.onesheeld.shields.fragments;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.KeyboardShield;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class KeyboardFragment extends ShieldFragmentParent<KeyboardFragment>
		implements OnClickListener {
	private Button mBSpace, mBenter, mBack, mBChange, mNum;
	private boolean isEdit1 = true;
	private String mUpper = "upper", mLower = "lower";
	private int w, mWindowWidth;
	private String sL[] = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
			"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
			"x", "y", "z", "ç", "à", "é", "è", "û", "î" };
	private String cL[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z", "ç", "à", "é", "è", "û", "î" };
	private String nS[] = { "!", ")", "'", "#", "3", "$", "%", "&", "8", "*",
			"?", "/", "+", "-", "9", "0", "1", "4", "@", "5", "7", "(", "2",
			"\"", "6", "_", "=", "]", "[", "<", ">", "|" };
	private Button mB[] = new Button[32];
	EditText mEt1;
	Editable mytext;
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
	public void setUserVisibleHint(boolean isVisibleToUser) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mEt1 = (EditText) getView().findViewById(R.id.keyboard_myEdit_txt);
		mEt1.setMaxLines(Integer.MAX_VALUE);
		mEt1.setSingleLine(false);
		hideDefaultKeyboard();
		// adjusting key regarding window sizes
		setKeys();
		setFrow();
		setSrow();
		setTrow();
		setForow();

		mEt1.addTextChangedListener(new TextWatcher() {

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
				Log.d("Keyboard::Character", s + "");

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
		void onKeyPressed(String myString);

		void onEnterOrbspacepressed(char myChar);
	}

	@Override
	public void onClick(View v) {

		if (v == mBChange) {

			if (mBChange.getTag().equals(mUpper)) {
				changeSmallLetters();
				changeSmallTags();
			} else if (mBChange.getTag().equals(mLower)) {
				changeCapitalLetters();
				changeCapitalTags();
			}

		} else if (v != mBenter && v != mBack && v != mBChange && v != mNum) {
			addText(v);

		} else if (v == mBenter) {
			// represent Enter button and send frame
			int ascii = (int) '\n';
			Log.d("KeyBoard", "char::ASCII =" + ascii);
			addTextEnter('\n');

		} else if (v == mBack) {
			// represent backspace button and send frame
			// addTextBSpace("\b");
			int ascii = (int) '\b';
			Log.d("KeyBoard", "char::ASCII =" + ascii);
			addTextBSpace('\b');

		} else if (v == mNum) {
			String nTag = (String) mNum.getTag();
			if (nTag.equals("num")) {
				// show unused characters
				mB[26].setVisibility(Button.VISIBLE);
				mB[27].setVisibility(Button.VISIBLE);
				mB[28].setVisibility(Button.VISIBLE);
				mB[29].setVisibility(Button.VISIBLE);
				mB[30].setVisibility(Button.VISIBLE);
				mB[31].setVisibility(Button.VISIBLE);

				changeSyNuLetters();
				changeSyNuTags();
				((ViewGroup) mBChange.getParent())
						.setVisibility(Button.INVISIBLE);

			}
			if (nTag.equals("ABC")) {
				// hidden unused characters
				mB[26].setVisibility(Button.INVISIBLE);
				mB[27].setVisibility(Button.INVISIBLE);
				mB[28].setVisibility(Button.INVISIBLE);
				mB[29].setVisibility(Button.INVISIBLE);
				mB[30].setVisibility(Button.INVISIBLE);
				mB[31].setVisibility(Button.INVISIBLE);

				changeCapitalLetters();
				changeCapitalTags();
			}

		}

	}

	private void hideDefaultKeyboard() {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

	private void addText(View v) {
		if (isEdit1 == true) {
			String b = "";
			b = (String) v.getTag();
			if (b != null) {
				// adding text in Edittext
				// mEt.append(b);
				Log.d("KeyBoard", "char =" + b);
				int ascii = (int) b.charAt(0);
				Log.d("KeyBoard", "char::ASCII =" + ascii);

				eventHandler.onKeyPressed(b);

			}
		}

	}

	private void addTextEnter(char enterText) {
		eventHandler.onEnterOrbspacepressed(enterText);

	}

	private void addTextBSpace(char bspaceText) {
		eventHandler.onEnterOrbspacepressed(bspaceText);

	}

	private void changeSmallLetters() {
		((ViewGroup) mBChange.getParent()).setVisibility(Button.VISIBLE);
		for (int i = 0; i < sL.length; i++)
			mB[i].setText(sL[i]);
		mNum.setTag("12#");
	}

	private void changeSmallTags() {
		for (int i = 0; i < sL.length; i++)
			mB[i].setTag(sL[i]);
		mBChange.setTag("lower");
		mNum.setTag("num");
	}

	private void changeCapitalLetters() {
		((ViewGroup) mBChange.getParent()).setVisibility(Button.VISIBLE);
		for (int i = 0; i < cL.length; i++)
			mB[i].setText(cL[i]);
		mBChange.setTag("upper");
		mNum.setText("12#");

	}

	private void changeCapitalTags() {
		for (int i = 0; i < cL.length; i++)
			mB[i].setTag(cL[i]);
		mNum.setTag("num");

	}

	private void changeSyNuLetters() {

		for (int i = 0; i < nS.length; i++)
			mB[i].setText(nS[i]);
		mNum.setText("ABC");
	}

	private void changeSyNuTags() {
		for (int i = 0; i < nS.length; i++)
			mB[i].setTag(nS[i]);
		mNum.setTag("ABC");
	}

	private void setFrow() {
		w = (mWindowWidth / 13);
		w = w - 15;
		mB[16].setWidth(w);
		mB[22].setWidth(w + 3);
		mB[4].setWidth(w);
		mB[17].setWidth(w);
		mB[19].setWidth(w);
		mB[24].setWidth(w);
		mB[20].setWidth(w);
		mB[8].setWidth(w);
		mB[14].setWidth(w);
		mB[15].setWidth(w);
		mB[16].setHeight(50);
		mB[22].setHeight(50);
		mB[4].setHeight(50);
		mB[17].setHeight(50);
		mB[19].setHeight(50);
		mB[24].setHeight(50);
		mB[20].setHeight(50);
		mB[8].setHeight(50);
		mB[14].setHeight(50);
		mB[15].setHeight(50);

	}

	private void setSrow() {
		w = (mWindowWidth / 10);
		mB[0].setWidth(w);
		mB[18].setWidth(w);
		mB[3].setWidth(w);
		mB[5].setWidth(w);
		mB[6].setWidth(w);
		mB[7].setWidth(w);
		mB[26].setWidth(w);
		mB[9].setWidth(w);
		mB[10].setWidth(w);
		mB[11].setWidth(w);
		mB[26].setWidth(w);

		mB[0].setHeight(50);
		mB[18].setHeight(50);
		mB[3].setHeight(50);
		mB[5].setHeight(50);
		mB[6].setHeight(50);
		mB[7].setHeight(50);
		mB[9].setHeight(50);
		mB[10].setHeight(50);
		mB[11].setHeight(50);
		mB[26].setHeight(50);
	}

	private void setTrow() {
		w = (mWindowWidth / 12);
		mB[25].setWidth(w);
		mB[23].setWidth(w);
		mB[2].setWidth(w);
		mB[21].setWidth(w);
		mB[1].setWidth(w);
		mB[13].setWidth(w);
		mB[12].setWidth(w);
		mB[27].setWidth(w);
		mB[28].setWidth(w);
		mBack.setWidth(w);

		mB[25].setHeight(50);
		mB[23].setHeight(50);
		mB[2].setHeight(50);
		mB[21].setHeight(50);
		mB[1].setHeight(50);
		mB[13].setHeight(50);
		mB[12].setHeight(50);
		mB[27].setHeight(50);
		mB[28].setHeight(50);
		mBack.setHeight(50);

	}

	private void setForow() {
		w = (mWindowWidth / 10);
		mBSpace.setWidth(w * 4);
		mBSpace.setHeight(50);
		mB[29].setWidth(w);
		mB[29].setHeight(50);

		mB[30].setWidth(w);
		mB[30].setHeight(50);

		mB[31].setHeight(50);
		mB[31].setWidth(w);
		mBenter.setWidth(w + (w / 1));
		mBenter.setHeight(50);

	}

	private void setKeys() {
		try {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getActivity().getWindow().getWindowManager().getDefaultDisplay()
					.getMetrics(displaymetrics);
			mWindowWidth = displaymetrics.widthPixels;
		} catch (Exception ignored) {
		}
		// includes window decorations (statusbar bar/menu bar)
		if (Build.VERSION.SDK_INT >= 17)
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize", Point.class).invoke(
						getActivity().getWindow().getWindowManager()
								.getDefaultDisplay(), realSize);
				mWindowWidth = realSize.x;
			} catch (Exception ignored) {
			} // getting
		// window
		// height
		// getting ids from xml files
		mB[0] = (Button) getView().findViewById(R.id.xA);
		mB[1] = (Button) getView().findViewById(R.id.xB);
		mB[2] = (Button) getView().findViewById(R.id.xC);
		mB[3] = (Button) getView().findViewById(R.id.xD);
		mB[4] = (Button) getView().findViewById(R.id.xE);
		mB[5] = (Button) getView().findViewById(R.id.xF);
		mB[6] = (Button) getView().findViewById(R.id.xG);
		mB[7] = (Button) getView().findViewById(R.id.xH);
		mB[8] = (Button) getView().findViewById(R.id.xI);
		mB[9] = (Button) getView().findViewById(R.id.xJ);
		mB[10] = (Button) getView().findViewById(R.id.xK);
		mB[11] = (Button) getView().findViewById(R.id.xL);
		mB[12] = (Button) getView().findViewById(R.id.xM);
		mB[13] = (Button) getView().findViewById(R.id.xN);
		mB[14] = (Button) getView().findViewById(R.id.xO);
		mB[15] = (Button) getView().findViewById(R.id.xP);
		mB[16] = (Button) getView().findViewById(R.id.xQ);
		mB[17] = (Button) getView().findViewById(R.id.xR);
		mB[18] = (Button) getView().findViewById(R.id.xS);
		mB[19] = (Button) getView().findViewById(R.id.xT);
		mB[20] = (Button) getView().findViewById(R.id.xU);
		mB[21] = (Button) getView().findViewById(R.id.xV);
		mB[22] = (Button) getView().findViewById(R.id.xW);
		mB[23] = (Button) getView().findViewById(R.id.xX);
		mB[24] = (Button) getView().findViewById(R.id.xY);
		mB[25] = (Button) getView().findViewById(R.id.xZ);
		mB[26] = (Button) getView().findViewById(R.id.xS1);
		mB[27] = (Button) getView().findViewById(R.id.xS2);
		mB[28] = (Button) getView().findViewById(R.id.xS3);
		mB[29] = (Button) getView().findViewById(R.id.xS4);
		mB[30] = (Button) getView().findViewById(R.id.xS5);
		mB[31] = (Button) getView().findViewById(R.id.xS6);
		mBSpace = (Button) getView().findViewById(R.id.xSpace);
		mBenter = (Button) getView().findViewById(R.id.xDone);
		mBChange = (Button) getView().findViewById(R.id.xChange);
		mBack = (Button) getView().findViewById(R.id.xBack);
		mNum = (Button) getView().findViewById(R.id.xNum);
		for (int i = 0; i < mB.length; i++)
			mB[i].setOnClickListener(this);
		mBSpace.setOnClickListener(this);
		mBenter.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mBChange.setOnClickListener(this);
		mNum.setOnClickListener(this);

		// Hidden unused characters
		mB[26].setVisibility(Button.INVISIBLE);
		mB[27].setVisibility(Button.INVISIBLE);
		mB[28].setVisibility(Button.INVISIBLE);
		mB[29].setVisibility(Button.INVISIBLE);
		mB[30].setVisibility(Button.INVISIBLE);
		mB[31].setVisibility(Button.INVISIBLE);

	}

}
