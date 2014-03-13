package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.OneSheeldApplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class OneShieldEditText extends AutoCompleteTextView {

	public OneShieldEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(((OneSheeldApplication) context.getApplicationContext()).appFont);
	}

}
