package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MultiTouchContainer extends RelativeLayout {
    public int currentIndex = -1;
    public String currentTag = null;
    private ArrayList<PinData> childrenRects = new ArrayList<PinData>();

    public MultiTouchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    private synchronized PinData getTouhedIndex(MotionEvent event) {
        if (childrenRects == null || childrenRects.size() == 0) {
            loadRects(this);
        }
        for (PinData item : childrenRects) {
            if (item.rect.contains((int) event.getX(), (int) event.getY()))
                return item;
        }
        return new PinData("", null, -1, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Key key;
        if (event.getAction() == MotionEvent.ACTION_MOVE
                || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            PinData item = getTouhedIndex(event);
            if (item.index != currentIndex) {
                if (currentIndex != -1) {
                    key = childrenRects.get(currentIndex).key;
                    key.eventListener.onReleased(key);
                }
                currentIndex = item.index;
                currentTag = item.tag;
                if (item.index != -1) {
                    key = childrenRects.get(currentIndex).key;
                    key.eventListener.onPressed(key);
                }
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            PinData item = getTouhedIndex(event);
            if (currentIndex != -1) {
                key = childrenRects.get(currentIndex).key;
                key.eventListener.onReleased(key);
            }
            currentIndex = item.index;
            if (currentIndex != -1) {
                key = childrenRects.get(currentIndex).key;
                key.eventListener.onReleased(key);
            }
            currentTag = item.tag;
            return true;
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
