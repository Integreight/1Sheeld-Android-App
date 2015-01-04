package com.integreight.onesheeld.utils.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.integreight.onesheeld.R;

public class Key extends Button {
    private boolean outOfBounds = false;
    private Drawable normalBackground;
    private Drawable pressedBackground;
    private int row;
    private int column;
    public KeyTouchEventListener eventListener;

    public void setEventListener(final KeyTouchEventListener eventListener) {
        this.eventListener = new KeyTouchEventListener() {

            @Override
            public void onReleased(Key k) {
                endDrag();
                eventListener.onReleased(k);
            }

            @Override
            public void onPressed(Key k) {
                beginDrag();
                eventListener.onPressed(k);
            }
        };
    }

    public Key(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttributesFromXml(context, attrs);
        init();
    }

    public Key(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributesFromXml(context, attrs);
        init();

    }

    private void setAttributesFromXml(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Key);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.Key_column:
                    column = a.getInteger(attr, -1);
                    break;
                case R.styleable.Key_row:
                    row = a.getInteger(attr, -1);
                    break;
                case R.styleable.Key_normalbackground:
                    normalBackground = a.getDrawable(attr);
                    break;
                case R.styleable.Key_pressedbackground:
                    pressedBackground = a.getDrawable(attr);
                    break;
            }
        }
        a.recycle();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    private void init() {
        setKeyColor(this, normalBackground);

    }

    private void beginDrag() {
        setKeyColor(this, pressedBackground);
    }

    private void endDrag() {
        if (!outOfBounds) {
            setKeyColor(this, normalBackground);

            performClick();

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void setKeyColor(Button key, Drawable bg) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            key.setBackgroundDrawable(bg);
        } else {
            key.setBackground(bg);
        }
    }

    public static interface KeyTouchEventListener {
        void onPressed(Key k);

        void onReleased(Key k);
    }
}
