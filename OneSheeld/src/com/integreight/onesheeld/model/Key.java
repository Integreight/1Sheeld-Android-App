package com.integreight.onesheeld.model;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.R.styleable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

public class Key extends Button {

	private boolean dragging = false;
	private boolean outOfBounds = false;
	private Drawable normalBackground;
	private Drawable pressedBackground;
	private int row;
	private int column;
	KeyTouchEventListener eventListener;

	public void setEventListener(KeyTouchEventListener eventListener) {
		this.eventListener = eventListener;
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
//		row = -1;
//		column = -1;
		setKeyColor(this, normalBackground);
		
	}

	public void setCounterpart(int id) {
		Activity context = (Activity) getContext();

		context.findViewById(id);
	}

	public boolean isDragging() {
		return dragging;
	}

	private boolean hitFeedback() {
		return true;
	}

	private void beginDrag() {
		setKeyColor(this, pressedBackground);
	}

	private void updateDrag(float x, float y) {
		final boolean inside = getBackground().getBounds().contains((int) x,
				(int) y);

		if (inside == outOfBounds) {
			Drawable color;

			if (inside) {
				color = pressedBackground;
			} else {
				color = normalBackground;
			}

			setKeyColor(this, color);

			outOfBounds = !inside;
		}
	}

	private void endDrag() {
		if (!outOfBounds) {
			setKeyColor(this, normalBackground);

			performClick();

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getActionMasked();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			
			break;

		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:

			if (dragging) {

				break;
			}

			

			// fall through

		default:
			return super.onTouchEvent(event);
		}

		final float x = event.getX();
		final float y = event.getY();

		if (action == MotionEvent.ACTION_DOWN) {
			dragging = true;
			outOfBounds = false;
			Log.d("Keypad", "DOWN=Row: " + row + ", Column: " + column);
			if(eventListener!=null)eventListener.onPressed(this);
			beginDrag();

			return hitFeedback();
		} else // MOVE or UP
		{
			updateDrag(x, y);

			if (action == MotionEvent.ACTION_UP) {
				endDrag();
				Log.d("Keypad", "UP=Row: " + row + ", Column: " + column);
				if(eventListener!=null)eventListener.onReleased(this);
				dragging = false;
			}
		}

		return true;
	}

	static final int fadeDuration = 500;

	public static void fadeViewToAlpha(View v, int toAlpha) {
		AlphaAnimation anim = new AlphaAnimation(1 - toAlpha, toAlpha);

		anim.setDuration(fadeDuration);

		v.setVisibility(toAlpha == 0 ? View.INVISIBLE : View.VISIBLE);

		v.startAnimation(anim);
	}

	@SuppressLint("NewApi")
	public static void setKeyColor(Button key, Drawable bg) {
//		GradientDrawable background = (GradientDrawable) key.getBackground();
//
//		background.setColorFilter(new LightingColorFilter(color, 0));
		key.setBackground(bg);
		
		//key.setTextColor(bg);
	}
	
	public static interface KeyTouchEventListener{
		void onPressed(Key k);
		void onReleased(Key k);
	}
}
