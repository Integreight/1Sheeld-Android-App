package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 7/22/2015.
 */
public class CheckBox implements ButtonShape {
    float btnX, btnY, btnSize, btnWidth, btnHeight;
    int btnTouchId;
    String btnText = "";
    boolean isSelected = false, isPressed = false, visibility = true;
    byte size;

    public CheckBox(GlcdView view, float x, float y, byte size, int touchId, String text) {
        this.btnX = x;
        this.btnY = y;
        this.btnText = text;
        setSize(view, size);
        setBtnTouchId(view, touchId);
        isSelected = false;
        isPressed = false;
    }

    @Override
    public void applyTouch(GlcdView view) {
        List<Integer> params = new ArrayList<>();
        params.add((int) (btnX));
        params.add((int) (btnY));
        params.add((int) (btnX + btnWidth));
        params.add((int) (btnY + btnHeight));
        params.add(btnTouchId);
        List<Boolean> premissions = new ArrayList<>();
        premissions.add(null);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(view.ORDER_APPLYTOUCH, params, premissions);
    }

    @Override
    public void clearTouch(GlcdView view) {
        List<Integer> params = new ArrayList<>();
        params.add((int) (btnX));
        params.add((int) (btnY));
        params.add((int) (btnX + btnWidth));
        params.add((int) (btnY + btnHeight));
        params.add(null);
        List<Boolean> premissions = new ArrayList<>();
        premissions.add(null);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(view.ORDER_APPLYTOUCH, params, premissions);
    }

    @Override
    public void setBtnTouchId(GlcdView view, int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(view);
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        if (isPressed) {
            setSelected(!isSelected);
        }
        if ((isPressed && isSelected) || (!isPressed && !isSelected)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return true;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSize(GlcdView view, byte size) {
        this.size = size;
        switch (size) {
            case 0:
                btnSize = view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
                this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
                break;
            case 1:
                btnSize = view.getCharHeight(view.TEXT_MEDUIM, view.FONT_ARIEL_REGULAR);
                this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_MEDUIM, view.FONT_ARIEL_REGULAR);
                break;
            case 2:
                btnSize = view.getCharHeight(view.TEXT_LARGE, view.FONT_ARIEL_REGULAR);
                this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_LARGE, view.FONT_ARIEL_REGULAR);
                break;
        }
        this.btnHeight = btnSize;
    }

    public void setText(GlcdView view, String text) {
        this.btnText = text;
        if (size == 0)
            this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        else if (size == 1)
            this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_MEDUIM, view.FONT_ARIEL_REGULAR);
        else if (size == 2)
            this.btnWidth = btnSize + view.getStringWidth(btnText, view.TEXT_LARGE, view.FONT_ARIEL_REGULAR);
        this.btnHeight = btnSize;
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.btnX = x;
        this.btnY = y;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            view.fillRectangle(btnX, btnY, btnSize - 1, btnSize, view.WHITE);
            view.drawRectangle(btnX, btnY, btnSize - 1, btnSize, view.BLACK);
            if (isSelected) view.fillRectangle(btnX, btnY, btnSize - 1, btnSize, view.BLACK);
            switch (size) {
                case 0:
                    view.drawString(btnText, btnX + btnSize + 2, btnY + (btnSize / 2) - (view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR) / 2) + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
                    break;
                case 1:
                    view.drawString(btnText, btnX + btnSize + 2, btnY + (btnSize / 2) - (view.getCharHeight(view.TEXT_MEDUIM, view.FONT_ARIEL_REGULAR) / 2) + 2, view.TEXT_MEDUIM, view.FONT_ARIEL_REGULAR, view.BLACK);
                    break;
                case 2:
                    view.drawString(btnText, btnX + btnSize + 2, btnY + (btnSize / 2) - (view.getCharHeight(view.TEXT_LARGE, view.FONT_ARIEL_REGULAR) / 2) + 2, view.TEXT_LARGE, view.FONT_ARIEL_REGULAR, view.BLACK);
                    break;
            }
        }
    }
}
