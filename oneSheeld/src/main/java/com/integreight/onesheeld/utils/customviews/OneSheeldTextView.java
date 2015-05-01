package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.integreight.onesheeld.OneSheeldApplication;

public class OneSheeldTextView extends TextView {

    public OneSheeldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(
                ((OneSheeldApplication) context.getApplicationContext()).appFont,
                getTypeface() == null ? Typeface.NORMAL : getTypeface()
                        .getStyle() == Typeface.BOLD ? Typeface.BOLD
                        : Typeface.NORMAL);
        setPadding(getPaddingLeft(),
                getPaddingTop()
                        - ((int) (1.5 * context.getResources()
                        .getDisplayMetrics().density + .5f)),
                getPaddingRight(), getBottom());
    }

    public OneSheeldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(
                ((OneSheeldApplication) context.getApplicationContext()).appFont,
                getTypeface() == null ? Typeface.NORMAL : getTypeface()
                        .getStyle() == Typeface.BOLD ? Typeface.BOLD
                        : Typeface.NORMAL);
        setPadding(getPaddingLeft(),
                getPaddingTop()
                        - ((int) (1.5 * context.getResources()
                        .getDisplayMetrics().density + .5f)),
                getPaddingRight(), getBottom());
    }

    public OneSheeldTextView(Context context) {
        super(context);
        setTypeface(
                ((OneSheeldApplication) context.getApplicationContext()).appFont,
                getTypeface() == null ? Typeface.NORMAL : getTypeface()
                        .getStyle() == Typeface.BOLD ? Typeface.BOLD
                        : Typeface.NORMAL);
        setPadding(getPaddingLeft(),
                getPaddingTop()
                        - ((int) (1.5 * context.getResources()
                        .getDisplayMetrics().density + .5f)),
                getPaddingRight(), getBottom());
    }

}
