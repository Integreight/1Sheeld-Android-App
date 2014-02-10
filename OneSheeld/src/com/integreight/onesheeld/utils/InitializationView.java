package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class InitializationView extends LinearLayout {
	LinearLayout view;

	public InitializationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		view = (LinearLayout) ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.initialization_view, this, false);
		setView();
	}

	private void setView() {
		
	}
}
