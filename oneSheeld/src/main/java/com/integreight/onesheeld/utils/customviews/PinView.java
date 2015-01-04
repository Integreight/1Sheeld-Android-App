package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.integreight.onesheeld.R;

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
}
