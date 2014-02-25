package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.OneSheeldApplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class OneShieldTextView extends TextView {

	public OneShieldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(((OneSheeldApplication) context.getApplicationContext()).appFont);
	}

}
