package com.integreight.onesheeld.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.integreight.onesheeld.R;

public class ListViewReversed extends ListView {

	OnScrollListener onScroll;
	int mLastFirstVisibleItem = 0;
	boolean isScrollingDown = false;
	int oneHundredDp = 0;

	public ListViewReversed(Context context, AttributeSet attrs) {
		super(context, attrs);
		ViewGroup searchArea = (ViewGroup) ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.transparent_shields_list_search_area, this, false);
		addHeaderView(searchArea);
		searchArea.removeAllViews();
		oneHundredDp = (int) (100 * context.getResources().getDisplayMetrics().density + .5f);
		setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				final ListView lw = ListViewReversed.this;

				if (view.getId() == lw.getId() && onScroll != null) {
					final int currentFirstVisibleItem = lw
							.getFirstVisiblePosition();
					if (currentFirstVisibleItem > mLastFirstVisibleItem) {
						// onScroll.onDown();
						isScrollingDown = true;
						Log.i("a", "scrolling down...");
					} else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
						// onScroll.onUp();
						isScrollingDown = false;
						Log.i("a", "scrolling up...");
					}
					mLastFirstVisibleItem = currentFirstVisibleItem;
				}
			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == 0) {
					lastOffSet = verticalOffset;
					if (onScroll != null) {
						if (isScrollingDown)
							onScroll.onDown();
						else
							onScroll.onUp();
					}
				}

			}
		});
		// TODO Auto-generated constructor stub
	}

	public void setOnScroll(OnScrollListener onScroll) {
		this.onScroll = onScroll;
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
	private int lastOffSet = 0, verticalOffset = 0;

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		verticalOffset = computeVerticalScrollOffset();
		if (onScroll != null)
			onScroll.translate(lastOffSet - verticalOffset);
		System.out.println((lastOffSet - verticalOffset) + "  **");
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public static interface OnScrollListener {
		public void onUp();

		public void onDown();

		public void translate(int topMargin);
	}
}
