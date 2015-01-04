package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;
import com.integreight.onesheeld.utils.customviews.PluginConnectingPinsView.onGetPinsView;
import com.integreight.onesheeld.utils.customviews.PluginPinsColumnContainer.PinData.TYPE;

import java.util.ArrayList;

public class PluginPinsColumnContainer extends RelativeLayout {
    public int currentIndex = -1;
    public String currentTag = null;
    private OnChildFocusListener focusListener;
    private int extraHorizontalSpace = 0, extraVerticalSpace = 0;
    private ArrayList<PinData> childrenRects = new ArrayList<PinData>();
    ImageView cursor;
    RelativeLayout.LayoutParams cursorParams;
    private onGetPinsView onGetPinsListener;
    private boolean isOnglobalCalled = false;

    public PluginPinsColumnContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        extraHorizontalSpace = (int) (50 * context.getResources()
                .getDisplayMetrics().density - .5f);
        extraVerticalSpace = (int) (1 * context.getResources()
                .getDisplayMetrics().density - .5f);
    }

    public void setup(OnChildFocusListener focusListener, ImageView cursor,
                      onGetPinsView onGetPinsListener, final int currentIndx) {
        this.focusListener = focusListener;
        this.cursor = cursor;
        this.onGetPinsListener = onGetPinsListener;
        this.currentIndex = -1;
        currentTag = null;
        isOnglobalCalled = false;
        childrenRects = new ArrayList<PinData>();
        getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (!isOnglobalCalled
                                && (childrenRects == null || childrenRects
                                .size() == 0)) {
                            childrenRects = new ArrayList<PluginPinsColumnContainer.PinData>();
                            loadRects(PluginPinsColumnContainer.this);
                            PluginPinsColumnContainer.this.onGetPinsListener
                                    .onPinsDrawn();
                            if (currentIndx != -1) {
                                for (PinData iterable_element : childrenRects) {
                                    if (ArduinoPin
                                            .valueOf(iterable_element.tag).microHardwarePin == currentIndx) {
                                        currentTag = iterable_element.tag;
                                        PluginPinsColumnContainer.this.currentIndex = iterable_element.index;
                                        setCursorTo(getDataOfTag(iterable_element.tag));
                                        loadRects(PluginPinsColumnContainer.this);
                                        break;
                                    }
                                }
                            }
                            isOnglobalCalled = true;
                        }
                    }
                });
    }

    int concatenatedLeft = 0, concatenatedTop = 0, concatenatedRight = 0;

    private boolean isPinEnabled(String tag) {
        return true;
    }

    private int getType(PinView v) {
        int type = PinData.TYPE.NOT_CONNECTED_AND_ENABLED;
        if (v.getTag() != null) {
            String tag = v.getTag().toString().startsWith("_") ? v.getTag()
                    .toString().substring(1) : v.getTag().toString();
            if (isPinEnabled(tag) == false)
                return PinData.TYPE.DISABLED;
        } else
            type = TYPE.DUMMY;
        return type;
    }

    private void loadRects(ViewGroup vg) {
        concatenatedLeft = getChildAt(1).getLeft();
        concatenatedTop = getChildAt(1).getTop();
        concatenatedRight = getChildAt(1).getRight();
        cursorParams = (LayoutParams) cursor.getLayoutParams();
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof PinView) {
                PinView v = (PinView) vg.getChildAt(i);
                int type = getType(v);
                if (type != TYPE.DUMMY) {
                    if (v.getTag().toString().equals(currentTag))
                        v.setBackgroundResource(PinData.TYPE.CONNECTED_HERE);
                    else
                        v.setBackgroundResource(type);
                    childrenRects.add(new PinData(((String) v.getTag()),
                            new Rect(
                                    concatenatedLeft
                                            + vg.getLeft()
                                            - (extraHorizontalSpace * (!v
                                            .getTag().toString()
                                            .startsWith("_") ? 2 : 1))
                                            + v.getLeft(), concatenatedTop
                                    + vg.getTop() + v.getTop()
                                    - extraVerticalSpace,
                                    concatenatedTop
                                            + vg.getLeft()
                                            + v.getRight()
                                            + (extraHorizontalSpace * (v
                                            .getTag().toString()
                                            .startsWith("_") ? 2 : 2)),
                                    concatenatedTop + vg.getTop()
                                            + v.getBottom()
                                            + extraVerticalSpace), i, type));
                }
            } else if (vg.getChildAt(i) instanceof ViewGroup) {
                loadRects((ViewGroup) vg.getChildAt(i));
            }
        }
    }

    private synchronized PinData getTouhedIndex(MotionEvent event) {
        for (PinData item : childrenRects) {
            if (item.rect.contains((int) event.getX(), (int) event.getY())
                    && item.type != PinData.TYPE.DISABLED)
                return item;
        }
        return new PinData("", null, -1, PinData.TYPE.NOT_CONNECTED_AND_ENABLED);
    }

    public void setCursorTo(PinData item) {
        cursor.setVisibility(View.VISIBLE);
        cursorParams.topMargin = item.rect.top - (cursorParams.height / 2)
                - (5 * extraVerticalSpace);
        cursorParams.leftMargin = (item.tag.startsWith("_") ? (concatenatedLeft - extraHorizontalSpace / 2)
                : (concatenatedRight + extraHorizontalSpace / 4));
        cursor.setBackgroundResource(item.tag.startsWith("_") ? R.drawable.arduino_pins_view_left_selector
                : R.drawable.arduino_pins_view_right_selector);
        cursor.requestLayout();
    }

    public PinData getDataOfTag(String tag) {
        for (PinData item : childrenRects) {
            if (tag.equals(item.tag)) {
                return item;
            }
        }
        return new PinData("", null, -1, PinData.TYPE.NOT_CONNECTED_AND_ENABLED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            PinData item = getTouhedIndex(event);
            if (item.index != currentIndex) {
                currentIndex = item.index;
                currentTag = item.tag;
                focusListener.focusOnThisChild(currentIndex,
                        currentIndex == -1 ? "" : currentTag);
                if (item.index != -1) {
                    setCursorTo(item);
                } else
                    cursor.setVisibility(View.INVISIBLE);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            PinData item = getTouhedIndex(event);
            currentIndex = item.index;
            currentTag = item.tag;
            if (item.index != -1) {
                setCursorTo(item);
            } else
                cursor.setVisibility(View.INVISIBLE);
            focusListener.selectThisChild(currentIndex, currentIndex == -1 ? ""
                    : currentTag);
            childrenRects = new ArrayList<PluginPinsColumnContainer.PinData>();
            loadRects(this);
            return true;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return true;
    }

    public static class PinData {
        public String tag;
        public Rect rect;
        public int index;
        public int type;

        public PinData() {
            // TODO Auto-generated constructor stub
        }

        public PinData(String tag, Rect rect, int index, int type) {
            super();
            this.tag = tag;
            this.rect = rect;
            this.index = index;
            this.type = type;
        }

        public static final class TYPE {
            public final static int CONNECTED_HERE = R.drawable.arduino_green_temp_pin;
            public final static int CONNECTED_OUT = R.drawable.arduino_orange_pin;
            public final static int NOT_CONNECTED_AND_ENABLED = R.drawable.arduino_default_pin;
            public final static int DISABLED = R.drawable.arduino_red_pin;
            public final static int DUMMY = R.drawable.arduino_dummy_pin;
        }

    }

}
