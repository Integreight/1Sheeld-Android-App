package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.concurrent.CopyOnWriteArrayList;

public class MutitouchKeysContainer extends RelativeLayout {
    public int currentIndex = -1;
    public String currentTag = null;
    private CopyOnWriteArrayList<PinData> childrenRects = new CopyOnWriteArrayList<PinData>();
    private CopyOnWriteArrayList<PinData> pressedRects = new CopyOnWriteArrayList<PinData>();
    private int maxPointers = 1;

    public MutitouchKeysContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        maxPointers = getMaxPointers(context);
    }

    private int getMaxPointers(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
                && (pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT) || pm
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND)))
            return 2;
        return 1;
    }

    private void loadRects(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof Key) {
                Key v = (Key) vg.getChildAt(i);

                childrenRects.add(new PinData(((String) v.getTag()), new Rect(
                        vg.getLeft() + v.getLeft(), vg.getTop() + v.getTop(),
                        vg.getLeft() + v.getRight(), vg.getTop()
                        + v.getBottom()), i, v));
            } else if (vg.getChildAt(i) instanceof ViewGroup) {
                loadRects((ViewGroup) vg.getChildAt(i));
            }
        }
    }

    private synchronized PinData getTouhedIndex(int x, int y,
                                                CopyOnWriteArrayList<PinData> data) {
        if (childrenRects == null || childrenRects.size() == 0) {
            loadRects(this);
        }
        for (PinData item : data) {
            if (item.rect.contains(x, y))
                return item;
        }
        return new PinData("", null, -1, null);
    }

    private synchronized void down(int x, int y) {
        PinData item = getTouhedIndex(x, y, childrenRects);
        if (item.index != -1 && !pressedRects.contains(item)) {
            pressedRects.add(item);
            if (item.key != null && item.key.eventListener != null)
                item.key.eventListener.onPressed(item.key);
        }
    }

    private synchronized void up(int x, int y) {
        PinData item = getTouhedIndex(x, y, childrenRects);
        if (item.index != -1 && pressedRects.contains(item)) {
            if (item.key != null && item.key.eventListener != null)
                item.key.eventListener.onReleased(item.key);
            pressedRects.remove(item);
        }
    }

    private synchronized void move(MotionEvent event) {
        for (PinData item : pressedRects) {
            boolean pressed = false;
            for (int i = 0; i < (event.getPointerCount() > maxPointers ? maxPointers
                    : event.getPointerCount()); i++) {
                if (item.rect
                        .contains((int) event.getX(i), (int) event.getY(i)))
                    pressed = true;
            }
            if (!pressed) {
                if (item.key != null && item.key.eventListener != null)
                    item.key.eventListener.onReleased(item.key);
                pressedRects.remove(item);
            }
        }
        for (int i = 0; i < (event.getPointerCount() > maxPointers ? maxPointers
                : event.getPointerCount()); i++) {
            down((int) event.getX(i), (int) event.getY(i));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getActionIndex() <= maxPointers - 1)
                    down((int) event.getX(event.getActionIndex()),
                            (int) event.getY(event.getActionIndex()));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getActionIndex() <= maxPointers - 1)
                    up((int) event.getX(event.getActionIndex()),
                            (int) event.getY(event.getActionIndex()));
                break;
            case MotionEvent.ACTION_MOVE:
                move(event);
                break;
            case MotionEvent.ACTION_DOWN:
                down((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_UP:
                up((int) event.getX(), (int) event.getY());
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return true;
    }

    public class PinData {
        public String tag;
        public Rect rect;
        public int index;
        public Key key;

        public PinData() {
            // TODO Auto-generated constructor stub
        }

        public PinData(String tag, Rect rect, int index, Key key) {
            super();
            this.tag = tag;
            this.rect = rect;
            this.index = index;
            this.key = key;
        }

    }

}
