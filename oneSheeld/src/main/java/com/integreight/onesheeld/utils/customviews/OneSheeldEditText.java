package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.integreight.onesheeld.OneSheeldApplication;

public class OneSheeldEditText extends AutoCompleteTextView {

	public OneSheeldEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(
				((OneSheeldApplication) context.getApplicationContext()).appFont,
				getTypeface() == null ? Typeface.NORMAL : getTypeface()
						.getStyle() == Typeface.BOLD ? Typeface.BOLD
						: Typeface.NORMAL);
	}

	// @Override
	// public boolean onKeyPreIme(int keyCode, KeyEvent event) {
	// if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
	// && event.getAction() == KeyEvent.ACTION_UP) {
	// clearFocus();
	// }
	// return super.dispatchKeyEvent(event);
	// }

}
