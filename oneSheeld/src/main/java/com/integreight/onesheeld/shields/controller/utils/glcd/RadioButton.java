package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 7/22/2015.
 */
public class RadioButton implements ButtonShape {
    // btnX , btnY are the center of the circle
    float btnX, btnY, btnRadius, btnWidth, btnHeight;
    RadioGroup radioGroup;
    int btnTouchId;
    String btnText = "";
    byte size = 0;
    boolean isSelected = false, isPressed = false, visibility = true;

    public RadioButton(GlcdShield controller, float x, float y, byte size, int touchId, String text) {
        this.size = size;
        setSize(size);
        this.btnX = x + btnRadius;
        this.btnY = y + btnRadius;
        this.btnText = text;
        this.btnWidth = btnRadius + btnRadius + btnRadius + controller.getView().getStringWidth(text, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        this.btnHeight = btnRadius + btnRadius + btnRadius;

        this.btnTouchId = touchId;
        applyTouch(controller);
        isSelected = false;
        isPressed = false;
    }

    public void setRadioGroup(RadioGroup radioGroup) {
        this.radioGroup = radioGroup;
    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    @Override
    public void applyTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX - btnRadius));
            params.add((int) (btnY - btnRadius));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnRadius));
            params.add(btnTouchId);
            controller.doOrder(GlcdShield.ORDER_APPLYTOUCH, params);
        }
    }

    @Override
    public void clearTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX - btnRadius));
            params.add((int) (btnY - btnRadius));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnRadius));
            params.add(null);
            controller.doOrder(GlcdShield.ORDER_APPLYTOUCH, params);
        }
    }

    @Override
    public void setBtnTouchId(GlcdShield controller, int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(controller);
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        if (isPressed == true) {
            setSelected(!isSelected);
            if (isSelected) {
                if (radioGroup != null)
                    radioGroup.select(this);
            }
            this.isPressed = isPressed;
        }

        if ((isPressed && isSelected) || (!isPressed && !isSelected)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return false;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSize(byte size) {
        this.size = size;
        switch (this.size) {
            case 0:
                this.btnRadius = 5;
                break;
            case 1:
                this.btnRadius = 10;
                break;
            case 2:
                this.btnRadius = 20;
                break;
        }

    }

    public void setText(GlcdView view, String text) {
        this.btnText = text;
        this.btnWidth = btnX + btnRadius + btnRadius + btnRadius + view.getStringWidth(text, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        this.btnHeight = btnY + btnRadius + btnRadius + btnRadius;
        ;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            view.fillCircle(btnX, btnY, btnRadius, GlcdShield.WHITE);
            view.drawCircle(btnX, btnY, btnRadius, GlcdShield.BLACK);
            if (isSelected) view.fillCircle(btnX, btnY, btnRadius - 2, GlcdShield.BLACK);
            view.drawString(btnText, btnX + btnRadius + 2, btnY - btnRadius + 2, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR, GlcdShield.BLACK);
        }
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
}
