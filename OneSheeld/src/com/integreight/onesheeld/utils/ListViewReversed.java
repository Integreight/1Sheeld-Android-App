package com.integreight.onesheeld.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewReversed extends ListView {

	public ListViewReversed(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.setChildrenDrawingOrderEnabled(true);
	}

	// @Override
	// protected int getChildDrawingOrder (int childCount, int i) {
	//
	// return childCount-1 - i;
	//
	// }
}
