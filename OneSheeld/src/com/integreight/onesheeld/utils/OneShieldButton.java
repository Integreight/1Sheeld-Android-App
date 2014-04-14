package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.OneSheeldApplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class OneShieldButton extends Button {
	public OneShieldButton(Context context) {
		super(context);
		setTypeface(((OneSheeldApplication) context.getApplicationContext()).appFont);
	}

	public OneShieldButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(((OneSheeldApplication) context.getApplicationContext()).appFont);
	}

}
