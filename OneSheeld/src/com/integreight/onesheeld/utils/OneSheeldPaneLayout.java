package com.integreight.onesheeld.utils;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class OneSheeldPaneLayout extends SlidingPaneLayout {
	public boolean isDragable = true;

	public OneSheeldPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (isDragable) {
			return false;
		}
		return super.onInterceptTouchEvent(arg0);
	}

}
