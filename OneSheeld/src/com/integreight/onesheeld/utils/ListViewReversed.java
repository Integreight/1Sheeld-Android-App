package com.integreight.onesheeld.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.integreight.onesheeld.R;

public class ListViewReversed extends ListView {

	int lastMotionY = 0;
	RelativeLayout.LayoutParams params;
	int maxMargin = 0;
	View header;

	public ListViewReversed(Context context, AttributeSet attrs) {
		super(context, attrs);
		LinearLayout searchArea = (LinearLayout) ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.shields_list_search_area, this, false);
		addHeaderView(searchArea);
		// maxMargin = (int) (100 * getResources().getDisplayMetrics().density -
		// .5f);
		// params = (android.widget.RelativeLayout.LayoutParams)
		// getLayoutParams();
		// setBackgroundColor(Color.TRANSPARENT);
		// TODO Auto-generated constructor stub
	}

	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	// // TODO Auto-generated method stub
	// return false;
	// }

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	// if (params == null) {
	// params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
	// }
	// switch (ev.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// lastMotionY = (int) ev.getY();
	// return super.onTouchEvent(ev);
	// case MotionEvent.ACTION_MOVE:
	// if (getChildAt(0).getTop() == 0 && params.topMargin <= maxMargin
	// && params.topMargin >= 0) {
	// int y = (int) ((ev.getY() - lastMotionY));
	// int expectedMargin = params.topMargin + y;
	// if (y > 0) {
	// if (expectedMargin <= maxMargin)
	// params.topMargin = expectedMargin;
	// else
	// params.topMargin = maxMargin;
	// } else {
	// if (expectedMargin >= 0)
	// params.topMargin = expectedMargin;
	// else
	// params.topMargin = 0;
	// }
	// // lastMotionY = (int) ev.getY();
	// requestLayout();
	// return true;
	// }
	// return super.dispatchTouchEvent(ev);
	// case MotionEvent.ACTION_UP:
	// if (params.topMargin != 0 && params.topMargin != maxMargin) {
	// if (params.topMargin >= maxMargin / 2) {
	// params.topMargin = maxMargin;
	// } else
	// params.topMargin = 0;
	// requestLayout();
	// return true;
	// }
	// return super.dispatchTouchEvent(ev);
	// default:
	// return super.dispatchTouchEvent(ev);
	// }
	// }
}
