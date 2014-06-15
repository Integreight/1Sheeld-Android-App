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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.LcdShieldd;
import com.integreight.onesheeld.shields.controller.LcdShieldd.LcdEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.RotatingTextView;

public class LcdFragmentPre extends ShieldFragmentParent<LcdFragmentPre> {
	View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if (v == null)
			v = inflater.inflate(R.layout.lcd_shield_fragment_layout,
					container, false);
		else
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
		draw(0,
				0,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).rows,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).columns);
	}

	@Override
	public void onStop() {
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
				cellCont.addView(cur);
				cellCont.addView(bg);
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
				cell.setText(((LcdShieldd) getApplication().getRunningShields()
						.get(getControllerTag())).chars[i][j] + "");
				cell.setSingleLine(true);
				cellCont.setLayoutParams(cellParams);
				RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, height);
				cell.setLayoutParams(childParams);
				cellCont.addView(cell);
				rowTxt.addView(cellCont);
			}
			rowCont.addView(rowBG);
			rowCont.addView(rowTxt);
			verticalContainer.setGravity(Gravity.CENTER);
			verticalContainer.addView(rowCont);
		}
	}

	private ViewGroup getCellContainerBG(int row, int col) {
		return ((ViewGroup) ((ViewGroup) ((ViewGroup) verticalContainer
				.getChildAt(((LcdShieldd) getApplication().getRunningShields()
						.get(getControllerTag())).rows - row - 1))
				.getChildAt(0)).getChildAt(col));
	}

	private ViewGroup getCellContainerTxt(int row, int col) {
		return ((ViewGroup) ((ViewGroup) ((ViewGroup) verticalContainer
				.getChildAt(((LcdShieldd) getApplication().getRunningShields()
						.get(getControllerTag())).rows - row - 1))
				.getChildAt(1)).getChildAt(col));
	}

	private ViewGroup getRowTxt(int row) {
		return (ViewGroup) ((ViewGroup) ((ViewGroup) verticalContainer
				.getChildAt(((LcdShieldd) getApplication().getRunningShields()
						.get(getControllerTag())).rows - row - 1)))
				.getChildAt(1);
	}

	public void scrollRow(int row) {
		Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
				0, Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT,
				-1);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setRepeatMode(Animation.RESTART);
		anim.setDuration(10000);
		getRowTxt(0).startAnimation(anim);
	}

	public void noBlinkCell() {
		isBlinking = false;
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).getChildAt(0)
				.startAnimation(
						AnimationUtils.loadAnimation(getActivity(),
								R.anim.no_blink_cell));
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).getChildAt(0)
				.setVisibility(View.INVISIBLE);
	}

	private boolean isBlinking = false;

	public void blinkCell() {
		isBlinking = true;
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).getChildAt(0)
				.setVisibility(View.VISIBLE);
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).getChildAt(0)
				.startAnimation(
						AnimationUtils.loadAnimation(getActivity(),
								R.anim.blink_cell));
	}

	public void noBlinkCellContainer() {
		isBlinking = false;
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).startAnimation(
				AnimationUtils.loadAnimation(getActivity(),
						R.anim.no_blink_cell));
	}

	public void blinkCellContainer() {
		getCellContainerBG(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), R.anim.blink_cell));
	}

	public void setCursor(int row, int col) {
		if (row != -1) {
			final boolean wasBlinking = isBlinking;
			if (((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currRowIndx != -1
					&& ((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currColIndx != -1) {
				noBlinkCell();
				getCellContainerBG(
						((LcdShieldd) getApplication().getRunningShields().get(
								getControllerTag())).currRowIndx,
						((LcdShieldd) getApplication().getRunningShields().get(
								getControllerTag())).currColIndx).getChildAt(0)
						.setVisibility(View.GONE);
			}
			((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currRowIndx = row;
			((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currColIndx = col;
			if (((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currColIndx <= -1)
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx = 0;
			if (((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currRowIndx <= -1)
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx = 0;
			if (((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currColIndx >= ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).columns)
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx = ((LcdShieldd) getApplication()
						.getRunningShields().get(getControllerTag())).columns - 1;
			if (((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currRowIndx >= ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).rows)
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx = ((LcdShieldd) getApplication()
						.getRunningShields().get(getControllerTag())).rows - 1;
			// getCellContainerBG(
			// ((LcdShieldd) getApplication().getRunningShields().get(
			// getControllerTag())).currRowIndx,
			// ((LcdShieldd) getApplication().getRunningShields().get(
			// getControllerTag())).currColIndx).getChildAt(0)
			// .setVisibility(View.VISIBLE);
			if (wasBlinking)
				blinkCell();
		} else
			((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).currRowIndx = -1;

	}

	public void clear(boolean changeCursor) {
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
		if (changeCursor)
			setCursor(0, 0);
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

	public void write(char ch, boolean mooveCursor) {
		if (((LcdShieldd) getApplication().getRunningShields().get(
				getControllerTag())).currRowIndx != -1) {

			((RotatingTextView) getCellContainerTxt(
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currRowIndx,
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currColIndx).getChildAt(0))
					.startAnimation(AnimationUtils.loadAnimation(getActivity(),
							R.anim.rotate_lcd));

			((RotatingTextView) getCellContainerTxt(
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currRowIndx,
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currColIndx).getChildAt(0)).isAnimated = true;
			((RotatingTextView) getCellContainerTxt(
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currRowIndx,
					((LcdShieldd) getApplication().getRunningShields().get(
							getControllerTag())).currColIndx).getChildAt(0))
					.setText(ch + "");
			((LcdShieldd) getApplication().getRunningShields().get(
					getControllerTag())).chars[((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).currRowIndx][((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).currColIndx] = ch;
			// if (mooveCursor) {
			int targeRowIndx = ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).currRowIndx;
			int targetColIndx = ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).currColIndx;
			if (targetColIndx < ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).columns - 1)
				targetColIndx = ((LcdShieldd) getApplication()
						.getRunningShields().get(getControllerTag())).currColIndx + 1;
			else {
				if (((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx == 0) {
					targeRowIndx = 1;
					targetColIndx = 0;
				} else {
					targeRowIndx = 0;
					targetColIndx = 0;
				}
			}
			setCursor(targeRowIndx, targetColIndx);
		}
		// }
	}

	private int lastScrollLeft = 0;

	public void scrollDisplayLeft() {
		lastScrollLeft += 1;
		clear(false);
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows; i++) {
			for (int j = lastScrollLeft; j < ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).columns; j++) {
				try {
					((TextView) getCellContainerTxt(i, j - lastScrollLeft)
							.getChildAt(0)).startAnimation(AnimationUtils
							.loadAnimation(getActivity(), R.anim.rotate_lcd));
					((TextView) getCellContainerTxt(i, j - lastScrollLeft)
							.getChildAt(0))
							.setText(((LcdShieldd) getApplication()
									.getRunningShields()
									.get(getControllerTag())).chars[i][j]
									+ "");
				} catch (Exception e) {
				}
			}
		}
		setCursor(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx - 1);
	}

	public void scrollDisplayRight() {
		lastScrollLeft -= 1;
		clear(false);
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows; i++) {
			for (int j = 0; j < ((LcdShieldd) getApplication()
					.getRunningShields().get(getControllerTag())).columns
					+ lastScrollLeft; j++) {
				try {
					((RotatingTextView) getCellContainerTxt(i,
							j - lastScrollLeft).getChildAt(0))
							.startAnimation(AnimationUtils.loadAnimation(
									getActivity(), R.anim.rotate_lcd));
					((RotatingTextView) getCellContainerTxt(i,
							j - lastScrollLeft).getChildAt(0))
							.setText(((LcdShieldd) getApplication()
									.getRunningShields()
									.get(getControllerTag())).chars[i][j]
									+ "");
				} catch (Exception e) {
					Log.e("", "", e);
				}
			}
		}
		setCursor(
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currRowIndx,
				((LcdShieldd) getApplication().getRunningShields().get(
						getControllerTag())).currColIndx + 1);
	}

	@SuppressWarnings("unused")
	private void redraw() {
		for (int i = 0; i < ((LcdShieldd) getApplication().getRunningShields()
				.get(getControllerTag())).rows; i++) {
			LinearLayout rowTxt = (LinearLayout) ((ViewGroup) verticalContainer
					.getChildAt(((LcdShieldd) getApplication()
							.getRunningShields().get(getControllerTag())).rows
							- i - 1)).getChildAt(1);
			for (int j = 0; j < rowTxt.getChildCount(); j++) {
				((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
						.getChildAt(0))
						.setText(((LcdShieldd) getApplication()
								.getRunningShields().get(getControllerTag())).chars[i][j]
								+ "");
			}
		}
	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

		@Override
		public void onLcdError(final String error) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}

		@Override
		public void setCursor(final int x, final int y) {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.setCursor(x, y);
					}
				});
			}
		}

		@Override
		public void write(final char ch, final boolean mooveCursor) {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.write(ch, mooveCursor);
					}
				});
			}
		}

		@Override
		public void blink() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.blinkCell();
					}
				});
			}
		}

		@Override
		public void noBlink() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.noBlinkCell();
					}
				});
			}
		}

		@Override
		public void cursor() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.blinkCell();
					}
				});
			}
		}

		@Override
		public void noCursor() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.noBlinkCell();
					}
				});
			}
		}

		@Override
		public void clear() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.clear(false);
					}
				});
			}
		}

		@Override
		public void scrollDisplayLeft() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.scrollDisplayLeft();
					}
				});
			}
		}

		@Override
		public void scrollDisplayRight() {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						LcdFragmentPre.this.scrollDisplayRight();
					}
				});
			}
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
