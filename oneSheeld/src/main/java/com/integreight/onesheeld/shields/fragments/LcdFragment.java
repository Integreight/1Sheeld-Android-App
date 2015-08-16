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
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LcdShield.LcdEventHandler;
import com.integreight.onesheeld.utils.customviews.RotatingTextView;

public class LcdFragment extends ShieldFragmentParent<LcdFragment> {
    private boolean drawn = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.lcd_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnViewCreated(View v, Bundle savedInstanceState) {
        verticalContainer = (LinearLayout) v
                .findViewById(R.id.verticalContainer);
        draw(0, 0, ((LcdShield) getApplication()
                        .getRunningShields().get(getControllerTag())).rows,
                ((LcdShield) getApplication().getRunningShields()
                        .get(getControllerTag())).columns);
        drawn = true;
        v.findViewById(R.id.bg).setBackgroundColor(Color.BLUE);
    }

    @Override
    public void doOnStart() {
        uiHandler = new Handler();
        ((LcdShield) getApplication().getRunningShields().get(
                getControllerTag())).setLcdEventHandler(lcdEventHandler);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                redraw(((LcdShield) getApplication().getRunningShields().get(
                        getControllerTag())).chars);
                drawn = true;
            }
        }, 0);
    }

    @Override
    public void doOnStop() {
        try {
            clear(false, false);
            drawn = false;
        } catch (Exception e) {
        }
    }

    LinearLayout verticalContainer;

    public void draw(int initRow, int initCol, int rowsEnd, int columnsEnd) {
        float scale = getResources().getDisplayMetrics().density;
        int height = (int) (30 * scale + .5f);
        int cellMargine = (int) (scale + .5f);
        verticalContainer.removeAllViews();
        Typeface tf = Typeface.createFromAsset(getAppActivity().getAssets(),
                "lcd_font.ttf");
        for (int i = initRow; i < rowsEnd; i++) {
            RelativeLayout rowCont = new RelativeLayout(getAppActivity());
            LinearLayout rowBG = new LinearLayout(getAppActivity());
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
                RelativeLayout cellCont = new RelativeLayout(getAppActivity());
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
                TextView bg = new TextView(getAppActivity());
                bg.setLayoutParams(childParams);
                bg.setBackgroundColor(0x99000000);
                RelativeLayout.LayoutParams curParams = new RelativeLayout.LayoutParams(
                        height / 10, height);
                TextView cur = new TextView(getAppActivity());
                cur.setLayoutParams(curParams);
                cur.setBackgroundColor(0xffffffff);
                cur.setVisibility(View.GONE);
                cellCont.addView(bg);
                cellCont.addView(cur);
                cellCont.setClickable(true);
                rowBG.addView(cellCont);
            }
            LinearLayout rowTxt = new LinearLayout(getAppActivity());
            RelativeLayout.LayoutParams paramsCh2 = new RelativeLayout.LayoutParams(
                    height, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rowTxt.setLayoutParams(paramsCh2);
            rowTxt.setOrientation(LinearLayout.VERTICAL);
            for (int j = 0; j < columnsEnd; j++) {
                RelativeLayout cellCont = new RelativeLayout(getAppActivity());
                RotatingTextView cell = new RotatingTextView(getAppActivity());
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
        int row = curIndx >= ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).columns ? 1 : 0;
        int col = curIndx >= ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).columns ? curIndx
                - ((LcdShield) getApplication().getRunningShields().get(
                getControllerTag())).columns : curIndx;
        return ((ViewGroup) ((ViewGroup) ((ViewGroup) verticalContainer
                .getChildAt(((LcdShield) getApplication().getRunningShields()
                        .get(getControllerTag())).rows - row - 1))
                .getChildAt(0)).getChildAt(col));
    }

    public synchronized void noCursor() {
        for (int i = 0; i < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows
                * ((LcdShield) getApplication().getRunningShields().get(
                getControllerTag())).columns; i++) {
            final int currIndx = i;
            if (currIndx > -1
                    && currIndx < (((LcdShield) getApplication()
                    .getRunningShields().get(getControllerTag())).columns * ((LcdShield) getApplication()
                    .getRunningShields().get(getControllerTag())).rows)) {
                getCellContainerBG(currIndx).getChildAt(1).startAnimation(
                        AnimationUtils.loadAnimation(getAppActivity(),
                                R.anim.no_blink_cell));
                getCellContainerBG(currIndx).getChildAt(1).setVisibility(
                        View.INVISIBLE);
            }
        }
    }

    public synchronized void cursor() {
        final int currIndx = ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).currIndx;
        if (currIndx > -1
                && currIndx < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).columns
                * ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows) {
            getCellContainerBG(currIndx).getChildAt(1).setVisibility(
                    View.VISIBLE);
            getCellContainerBG(currIndx).getChildAt(1).startAnimation(
                    AnimationUtils.loadAnimation(getAppActivity(),
                            R.anim.blink_cell));
        }
    }

    public synchronized void noBlink() {
        for (int i = 0; i < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows
                * ((LcdShield) getApplication().getRunningShields().get(
                getControllerTag())).columns; i++) {
            final int currIndx = i;
            if (currIndx > -1
                    && currIndx < (((LcdShield) getApplication()
                    .getRunningShields().get(getControllerTag())).columns * ((LcdShield) getApplication()
                    .getRunningShields().get(getControllerTag())).rows)) {
                getCellContainerBG(currIndx).getChildAt(0).startAnimation(
                        AnimationUtils.loadAnimation(getAppActivity(),
                                R.anim.no_blinking_cell));
            }
        }
    }

    public synchronized void blink() {
        final int currIndx = ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).currIndx;
        if (currIndx > -1
                && currIndx < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).columns
                * ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows) {
            getCellContainerBG(currIndx).getChildAt(0).setVisibility(
                    View.VISIBLE);
            getCellContainerBG(currIndx).getChildAt(0).startAnimation(
                    AnimationUtils.loadAnimation(getAppActivity(),
                            R.anim.blink_cell));
        }
    }

    public synchronized void clear(boolean changeCursor, boolean clearView) {
        for (int i = 0; i < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows; i++) {
            LinearLayout rowTxt = (LinearLayout) ((ViewGroup) verticalContainer
                    .getChildAt(((LcdShield) getApplication()
                            .getRunningShields().get(getControllerTag())).rows
                            - i - 1)).getChildAt(1);
            for (int j = 0; j < rowTxt.getChildCount(); j++) {
                ((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
                        .getChildAt(0)).setText("");
            }
        }
    }

    private synchronized void redraw(char[] arr) {
        for (int i = 0; i < ((LcdShield) getApplication().getRunningShields()
                .get(getControllerTag())).rows; i++) {
            LinearLayout rowTxt = (LinearLayout) ((ViewGroup) verticalContainer
                    .getChildAt(((LcdShield) getApplication()
                            .getRunningShields().get(getControllerTag())).rows
                            - i - 1)).getChildAt(1);
            for (int j = 0; j < rowTxt.getChildCount(); j++) {
                (((ViewGroup) rowTxt.getChildAt(j))
                        .getChildAt(0)).startAnimation(AnimationUtils
                        .loadAnimation(getAppActivity(), R.anim.rotate_lcd));
                ((RotatingTextView) ((ViewGroup) rowTxt.getChildAt(j))
                        .getChildAt(0))
                        .setText(arr[(i * ((LcdShield) getApplication()
                                .getRunningShields().get(getControllerTag())).columns)
                                + j]
                                + "");
            }
        }
    }

    private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

        @Override
        public void updateLCD(final char[] arrayToUpdate) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI() && drawn)
                        redraw(arrayToUpdate);
                }
            });

        }

        @Override
        public void blink() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI() && drawn)
                        LcdFragment.this.blink();
                }
            });
        }

        @Override
        public void noBlink() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI() && drawn)
                        LcdFragment.this.noBlink();
                }
            });
        }

        @Override
        public void cursor() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI() && drawn)
                        LcdFragment.this.cursor();
                }
            });
        }

        @Override
        public void noCursor() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI() && drawn)
                        LcdFragment.this.noCursor();
                }
            });
        }

    };

    private void initializeFirmata() {
        if ((getApplication().getRunningShields().get(getControllerTag())) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new LcdShield(getAppActivity(), getControllerTag()));
        ((LcdShield) getApplication().getRunningShields().get(
                getControllerTag())).setLcdEventHandler(lcdEventHandler);
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
