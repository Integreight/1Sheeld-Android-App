package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.Key;
import com.integreight.onesheeld.Key.KeyTouchEventListener;
import com.integreight.onesheeld.R;

public class KeypadFragment extends Fragment {

	KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

		@Override
		public void onReleased(Key k) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPressed(Key k) {
			// TODO Auto-generated method stub

		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		return inflater.inflate(R.layout.keypad_shield_fragment_layout,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		initializeKeysEventHandler((ViewGroup) getView());
		super.onActivityCreated(savedInstanceState);
	}

	private void initializeKeysEventHandler(ViewGroup viewGroup) {
		ViewGroup keypad = (ViewGroup) ((ViewGroup) viewGroup.getChildAt(0))
				.getChildAt(1);
		for (int i = 0; i < keypad.getChildCount(); i++) {
			ViewGroup keypadRow = (ViewGroup) keypad.getChildAt(i);
			for (int j = 0; j < keypadRow.getChildCount(); j++) {
				View key = keypadRow.getChildAt(j);
				if (key instanceof Key) {
					((Key) key).setEventListener(touchEventListener);
				}

			}

		}
	}

	public void onKeypadKeyPress(View v) {
		Key pressedKey = (Key) v;
		pressedKey.getRow();
		// switch (v.getId()) {
		// case R.id.keypad_key_1:
		// break;
		// case R.id.keypad_key_2:
		// break;
		// case R.id.keypad_key_3:
		// break;
		// case R.id.keypad_key_A:
		// break;
		// case R.id.keypad_key_4:
		// break;
		// case R.id.keypad_key_5:
		// break;
		// case R.id.keypad_key_6:
		// break;
		// case R.id.keypad_key_B:
		// break;
		// case R.id.keypad_key_7:
		// break;
		// case R.id.keypad_key_8:
		// break;
		// case R.id.keypad_key_9:
		// break;
		// case R.id.keypad_key_C:
		// break;
		// case R.id.keypad_key_Star:
		// break;
		// case R.id.keypad_key_0:
		// break;
		// case R.id.keypad_key_Hash:
		// break;
		// case R.id.keypad_key_D:
		// break;
		//
		// default:
		// Log.d("Keypad", "Def");
		// break;
		// }
	}

}
