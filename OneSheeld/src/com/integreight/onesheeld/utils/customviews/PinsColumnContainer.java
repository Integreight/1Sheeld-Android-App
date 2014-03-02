package com.integreight.onesheeld.utils.customviews;

import java.util.ArrayList;

import com.google.android.gms.internal.cu;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PinsColumnContainer extends RelativeLayout {
	public int currentIndex = -1;
	public String currentTag = null;
	private OnChildFocusListener focusListener;
	private int extraHorizontalSpace = 0, extraVerticalSpace = 0;
	private ArrayList<PinData> childrenRects = new ArrayList<PinData>();
	ImageView cursor;
	RelativeLayout.LayoutParams cursorParams;

	public PinsColumnContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		extraHorizontalSpace = (int) (50 * context.getResources()
				.getDisplayMetrics().density - .5f);
		extraVerticalSpace = (int) (1 * context.getResources()
				.getDisplayMetrics().density - .5f);
		// setOrientation(VERTICAL);
	}

	public void setup(OnChildFocusListener focusListener, ImageView cursor) {
		this.focusListener = focusListener;
		this.cursor = cursor;
	}

	int concatenatedLeft = 0, concatenatedTop = 0, concatenatedRight = 0;

	private void loadRects(ViewGroup vg) {
		concatenatedLeft = getChildAt(1).getLeft();
		concatenatedTop = getChildAt(1).getTop();
		concatenatedRight = getChildAt(1).getRight();
		cursorParams = (LayoutParams) cursor.getLayoutParams();
		for (int i = 0; i < vg.getChildCount(); i++) {
			if (vg.getChildAt(i) instanceof PinView) {
				PinView v = (PinView) vg.getChildAt(i);
				childrenRects.add(new PinData(((String) v.getTag()), new Rect(
						concatenatedLeft
								+ vg.getLeft()
								- (extraHorizontalSpace * (!v.getTag()
										.toString().startsWith("_") ? 2 : 1))
								+ v.getLeft(), concatenatedTop + vg.getTop()
								+ v.getTop() - extraVerticalSpace,
						concatenatedTop
								+ vg.getLeft()
								+ v.getRight()
								+ (extraHorizontalSpace * (v.getTag()
										.toString().startsWith("_") ? 2 : 2)),
						concatenatedTop + vg.getTop() + v.getBottom()
								+ extraVerticalSpace), i));
			} else if (vg.getChildAt(i) instanceof ViewGroup) {
				loadRects((ViewGroup) vg.getChildAt(i));
			}
		}
	}

	private synchronized PinData getTouhedIndex(MotionEvent event) {
		if (childrenRects == null || childrenRects.size() == 0) {
			loadRects(this);
		}
		for (PinData item : childrenRects) {
			if (item.rect.contains((int) event.getX(), (int) event.getY()))
				return item;
		}
		return new PinData("", null, -1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			PinData item = getTouhedIndex(event);
			if (item.index != currentIndex) {
				currentIndex = item.index;
				currentTag = item.tag;
				focusListener.focusOnThisChild(
						currentIndex,
						currentIndex == -1 ? ""
								: (currentTag.contains("_") ? currentTag
										.substring(1) : currentTag));
				if (item.index != -1) {
					cursor.setVisibility(View.VISIBLE);
					cursorParams.topMargin = item.rect.top
							- (cursorParams.height / 2)
							- (5 * extraVerticalSpace);
					cursorParams.leftMargin = (currentTag.startsWith("_") ? (concatenatedLeft - extraHorizontalSpace / 2)
							: (concatenatedRight + extraHorizontalSpace / 4));
					cursor.setBackgroundResource(currentTag.startsWith("_") ? R.drawable.arduino_pins_view_left_selector
							: R.drawable.arduino_pins_view_right_selector);
					cursor.requestLayout();
				} else
					cursor.setVisibility(View.INVISIBLE);
				return true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			PinData item = getTouhedIndex(event);
			currentIndex = item.index;
			currentTag = item.tag;
			focusListener.selectThisChild(currentIndex, currentIndex == -1 ? ""
					: (currentTag.contains("_") ? currentTag.substring(1)
							: currentTag));
			return true;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return true;
	}

	public class PinData {
		public String tag;
		public Rect rect;
		public int index;

		public PinData() {
			// TODO Auto-generated constructor stub
		}

		public PinData(String tag, Rect rect, int index) {
			super();
			this.tag = tag;
			this.rect = rect;
			this.index = index;
		}

	}

}
