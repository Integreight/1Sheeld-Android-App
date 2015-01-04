package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.integreight.onesheeld.R;

public class RotatingTextView extends TextView {
    public boolean isAnimated = false;

    public RotatingTextView(Context context) {
        super(context);
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate_lcd));
    }
}