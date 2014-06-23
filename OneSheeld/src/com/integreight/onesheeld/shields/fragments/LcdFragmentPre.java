package com.integreight.onesheeld.shields.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.LcdShieldd;
import com.integreight.onesheeld.shields.controller.LcdShieldd.LcdEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.RotatingTextView;

public class LcdFragmentPre extends ShieldFragmentParent<LcdFragmentPre> {
	View v;
	private boolean drawn = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if (v == null) {
			v = inflater.inflate(R.layout.lcd_shield_fragment_layout,
					container, false);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					draw(0, 0, ((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).rows,
							((LcdShieldd) getApplication().getRunningShields()
									.get(getControllerTag())).columns);
					drawn = true;
				}
			}, 700);
		} else
			try {
				((ViewGroup) v.getParent()).removeView(v);
			} catch (Exception e) {
				// TODO: handle exception
			}
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		uiHandler = new Handler();
		((LcdShieldd) getApplication().getRunningShields().get(
				getControllerTag())).setLcdEventHandler(lcdEventHandler);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				redraw(((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).chars);
				drawn = true;
			}
		}, 700);
	}

	@Override
	public void onStop() {
		try {
			clear(false, false);
			drawn = false;
		} catch (Exception e) {
		}
		super.onStop();
	}

	LinearLayout verticalContainer, firstRow, secondRow;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		verticalContainer = (LinearLayout) v
				.findViewById(R.id.verticalContainer);
		v.findViewById(R.id.bg).setBackgroundColor(Color.BLUE);
		super.onActivityCreated(savedInstanceState);
	}

	public void draw(int initRow, int initCol, int rowsEnd, int columnsEnd) {
		float scale = getResources().getDisplayMetrics().density;
		int height = (int) (23 * scale + .5f);
		int cellMargine = (int) (scale + .5f);
		verticalContainer.removeAllViews();
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"lcd_font.ttf");
		for (int i = initRow; i < rowsEnd; i++) {
			RelativeLayout rowCont = new RelativeLayout(getActivity());
			LinearLayout rowBG = new LinearLayout(getActivity());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					height, LinearLayout.LayoutParams.WRAP_CONTENT);
			rowCont.setLayoutParams(params);
			rowBG.setClickable(true);
			rowCont.setClickable(true);
			RelativeLayout.LayoutParams paramsCh = new RelativeLayout.LayoutParams(
					height, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rowBG.setLayoutParams(paramsCh);
			rowBG.setOrientation(LinearLayout.VERTICAL);
			for (int j = initCol; j < columnsEnd; j++) {
				RelativeLayout cellCont = new RelativeLayout(getActivity());
				LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
						height, LinearLayout.LayoutParams.MATCH_PARENT);
				cellParams.weight = 1;
				cellParams.bottomMargin = cellMargine;
				cellParams.topMargin = cellMargine;
				cellParams.leftMargin = cellMargine;
				cellParams.rightMargin = cellMargine;
				cellCont.setLayoutParams(cellParams);
				RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, height);
				TextView bg = new TextView(getActivity());
				bg.setLayoutParams(childParams);
				bg.setBackgroundColor(0x99000000);
				RelativeLayout.LayoutParams curParams = new RelativeLayout.LayoutParams(
						height / 10, height);
				// curParams.topMargin = (int) (1 * scale + .5f);
				// curParams.bottomMargin = (int) (1 * scale + .5f);
				TextView cur = new TextView(getActivity());
				cur.setLayoutParams(curParams);
				cur.setBackgroundColor(0xffffffff);
				cur.setVisibility(View.GONE);
				cellCont.addView(bg);
				cellCont.addView(cur);
				cellCont.setClickable(true);
				rowBG.addView(cellCont);
			}
			LinearLayout rowTxt = new LinearLayout(getActivity());
			RelativeLayout.LayoutParams paramsCh2 = new RelativeLayout.LayoutParams(
					height, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rowTxt.setLayoutParams(paramsCh2);
			rowTxt.setOrientation(LinearLayout.VERTICAL);
			for (int j = 0; j < columnsEnd; j++) {
				RelativeLayout cellCont = new RelativeLayout(getActivity());
				RotatingTextView cell = new RotatingTextView(getActivity());
				cell.setTypeface(tf);
				LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
						height, LinearLayout.LayoutParams.MATCH_PARENT);
				cellParams.weight = 1;
				cellParams.bottomMargin = cellMargine;
				cellParams.topMargin = cellMargine;
				cellParams.leftMargin = cellMargine;
				cellParams.rightMargin = cellMargine;
				cell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
				cell.setGravity(Gravity.CENTER);
				cell.setTextColor(Color.WHITE);
				// cell.setText(((LcdShieldd)
				// getApplication().getRunningShields()
				// .get(getControllerTag())).chars[(i * columnsEnd) + j]
				// + "");
				cell.setSingleLine(true);
				cellCont.setLayoutParams(cellParams);
				RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, height);
				cell.setLayoutParams(childParams);
				cellCont.addView(cell);
				cellCont.setClickable(true);
				rowTxt.addView(cellCont);
			}
			rowCont.addView(rowBG);
			rowCont.addView(rowTxt);
			verticalContainer.setGravity(Gravity.CENTER);
			verticalContainer.addView(rowCont);
		}
	}

	private synchronized ViewGroup getCellContainerBG(int curIndx) {
		int row = curIndx >= ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).columns ? 1 : 0;
		int col = curIndx >= ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).columns ? curIndx
				- ((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).columns : curIndx;
		return ((ViewGroup) ((ViewGroup) ((ViewGroup) verticalContainer
				.getChildAt(((LcdShieldd) getApplication().getRunningShields()
						.get(getControllerTag())).rows - row - 1))
				.getChildAt(0)).getChildAt(col));
	}

	public synchronized void noBlinkCell() {
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows
				* ((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).columns; i++) {
			final int currIndx = i;
			if (currIndx > -1
					&& currIndx < (((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).columns * ((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).rows)) {
				getCellContainerBG(currIndx).getChildAt(1).startAnimation(
						AnimationUtils.loadAnimation(getActivity(),
								R.anim.no_blink_cell));
				getCellContainerBG(currIndx).getChildAt(1).setVisibility(
						View.INVISIBLE);
			}
		}
	}

	public synchronized void blinkCell() {
		final int currIndx = ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).currIndx;
		if (currIndx > -1
				&& currIndx < ((LcdShieldd) getApplication()
						.getRunningShields().get(getControllerTag())).columns
						* ((LcdShieldd) getApplication().getRunningShields()
								.get(getControllerTag())).rows) {
			getCellContainerBG(currIndx).getChildAt(1).setVisibility(
					View.VISIBLE);
			getCellContainerBG(currIndx).getChildAt(1).startAnimation(
					AnimationUtils.loadAnimation(getActivity(),
							R.anim.blink_cell));
		}
	}

	public void noBlinkCellContainer() {
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currIndx).startAnimation(
				AnimationUtils.loadAnimation(getActivity(),
						R.anim.no_blink_cell));
	}

	public void blinkCellContainer() {
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currIndx).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), R.anim.blink_cell));
	}

	public synchronized void clear(boolean changeCursor, boolean clearView) {
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows; i++) {
			LinearLayout rowTxt = (LinearLayout) ((ViewGroup) verticalContainer
					.getChildAt(((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).rows
							- i - 1)).getChildAt(1);
			for (int j = 0; j < rowTxt.getChildCount(); j++) {
				((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
						.getChildAt(0)).setText("");
			}
		}
	}

	public void blinkLCD() {
		v.findViewById(R.id.bg).startAnimation(
				AnimationUtils.loadAnimation(getActivity(),
						R.anim.blink_anim_bg));
	}

	public void noBlinkLCD() {
		v.findViewById(R.id.bg).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), R.anim.no_anim_bg));
	}

	private synchronized void redraw(char[] arr) {
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows; i++) {
			LinearLayout rowTxt = (LinearLayout) ((ViewGroup) verticalContainer
					.getChildAt(((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).rows
							- i - 1)).getChildAt(1);
			for (int j = 0; j < rowTxt.getChildCount(); j++) {
				((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
						.getChildAt(0)).startAnimation(AnimationUtils
						.loadAnimation(getActivity(), R.anim.rotate_lcd));
				((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
						.getChildAt(0))
						.setText(arr[(i * ((LcdShieldd) getApplication()
								.getRunningShields().get(getControllerTag())).columns)
								+ j]
								+ "");
			}
		}
	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

		@Override
		public void updateLCD(final char[] arrayToUpdate) {
			if (canChangeUI() && drawn)
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						redraw(arrayToUpdate);
					}
				});

		}

		@Override
		public void blink() {
			if (canChangeUI() && drawn)
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						blinkCell();
					}
				});
		}

		@Override
		public void noBlink() {
			if (canChangeUI() && drawn)
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						noBlinkCell();
					}
				});
		}

	};

	private void initializeFirmata() {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new LcdShieldd(getActivity(), getControllerTag()));
		((LcdShieldd) getApplication().getRunningShields().get(
				getControllerTag())).setLcdEventHandler(lcdEventHandler);
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

}
