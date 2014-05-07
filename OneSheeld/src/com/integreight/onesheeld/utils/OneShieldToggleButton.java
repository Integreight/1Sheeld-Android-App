package com.integreight.onesheeld.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.integreight.onesheeld.OneSheeldApplication;

public class OneShieldToggleButton extends ToggleButton {
	public OneShieldToggleButton(Context context) {
		super(context);
		setTypeface(
				((OneSheeldApplication) context.getApplicationContext()).appFont,
				getTypeface() == null ? Typeface.NORMAL : getTypeface()
						.getStyle() == Typeface.BOLD ? Typeface.BOLD
						: Typeface.NORMAL);
	}

	public OneShieldToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(
				((OneSheeldApplication) context.getApplicationContext()).appFont,
				getTypeface() == null ? Typeface.NORMAL : getTypeface()
						.getStyle() == Typeface.BOLD ? Typeface.BOLD
						: Typeface.NORMAL);
	}

}
