package com.integreight.onesheeld.utils.customviews;

import com.integreight.onesheeld.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class PinView extends ImageView {
	public int index;
	public int top, bottom, right, left;

	public PinView(Context context, AttributeSet attr) {
		super(context, attr);
		setContentDescription(context.getResources().getString(
				R.string.app_name));
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		top = getTop();
		left = getLeft();
		right = getRight();
		bottom = getBottom();
		super.onWindowFocusChanged(hasWindowFocus);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	// private Rect rect; // Variable rect to hold the bounds of the view
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
	// // Construct a rect of the view's bounds
	// rect = new Rect(getLeft(), getTop(), getRight(),
	// getBottom());
	// focusListner.focusOnThisChild(index);
	// return true;
	//
	// }
	// if (event.getAction() == MotionEvent.ACTION_MOVE) {
	// if (!rect.contains(getLeft() + (int) event.getX(),getTop()
	// + (int) event.getY())) {
	// return false;
	// } else {
	// focusListner.focusOnThisChild(index);
	// return true;
	// }
	// }
	// return false;
	// }
}
