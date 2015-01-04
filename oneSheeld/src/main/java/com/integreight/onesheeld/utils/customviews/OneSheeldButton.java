package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.integreight.onesheeld.OneSheeldApplication;

public class OneSheeldButton extends Button {
    public OneSheeldButton(Context context) {
        super(context);
        setTypeface(
                ((OneSheeldApplication) context.getApplicationContext()).appFont,
                getTypeface() == null ? Typeface.NORMAL : getTypeface()
                        .getStyle() == Typeface.BOLD ? Typeface.BOLD
                        : Typeface.NORMAL);
    }

    public OneSheeldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(
                ((OneSheeldApplication) context.getApplicationContext()).appFont,
                getTypeface() == null ? Typeface.NORMAL : getTypeface()
                        .getStyle() == Typeface.BOLD ? Typeface.BOLD
                        : Typeface.NORMAL);
    }

}
