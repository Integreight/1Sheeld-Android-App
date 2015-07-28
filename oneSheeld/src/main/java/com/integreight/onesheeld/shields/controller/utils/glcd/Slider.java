package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 7/22/2015.
 */
public class Slider implements ButtonShape{

    float btnX,btnY,btnWidth,btnHeight,start,end,currentValue;
    int btnTouchId;
    boolean isPressed=false,visibility=true;

    public Slider(GlcdView view,float x, float y, float width, float height,int touchId){
        this.btnX = x;
        this.btnY = y;
        this.btnWidth = width;
        this.btnHeight = height;
        this.start = 0;
        this.end = 100;
        this.btnTouchId = touchId;
        applyTouch(view);
        this.currentValue = start;
        this.isPressed = false;
    }

    public void setCurrentValue(float value) {
        if (value < start)
            value = start;
        else if (value > end)
            value = end;
        this.currentValue = value;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    @Override
    public void draw(GlcdView view) {
        clearDraw(view);
        if (visibility) {
            // Using linear interpolation eguation to get the average value.
            //http://www.ajdesigner.com/phpinterpolation/linear_interpolation_equation.php
            float progress = (((currentValue - start) * ((btnX + btnWidth - (btnHeight / 2)) - (btnX + (btnHeight / 2)))) / (end - start)) + (btnX + (btnHeight / 2));

            view.drawLine(btnX, btnY + (btnHeight / 2), btnX + btnWidth, btnY + (btnHeight / 2), view.BLACK);
            view.fillCircle(progress, btnY + (btnHeight / 2), btnHeight / 2, view.BLACK);
        }
    }

    @Override
    public void clearDraw(GlcdView view) {
//            view.clear(view.WHITE, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
        List<Integer> params = new ArrayList<>();
        params.add(view.WHITE);
        params.add((int) btnX);
        params.add((int) btnY);
        params.add((int) btnWidth + 1);
        params.add((int) btnHeight + 1);
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(true);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(GlcdView.ORDER_CLEAR, params, premissions);
    }

    public void setStart(float start) {
        this.start = start;
        setCurrentValue(currentValue);
    }

    public void setEnd(float end) {
        this.end = end;
        setCurrentValue(currentValue);
    }

    public void setWidth(float width) {
        this.btnWidth = width;
    }

    public void setHeight(float height) {
        this.btnHeight = height;
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
    public void setIsPressed(boolean isPressed) {
        this.isPressed = false;
    }

    @Override
    public void setTouched(int touchX, int touchY) {
        currentValue = (((touchX-btnX)*(end-start))/(btnX+btnWidth-btnX))+start;
    }

    @Override
    public void setBtnTouchId(GlcdView view,int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(view);
    }

    @Override
    public void applyTouch(GlcdView view) {
//        for (float x=btnX;x<btnX+btnWidth;x++){
//            for (float y=btnY;y<btnY+btnHeight;y++){
//            }
//        }
        List<Integer> params = new ArrayList<>();
        params.add((int) (btnX));
        params.add((int) (btnY));
        params.add((int) (btnX+btnWidth));
        params.add((int) (btnY+btnHeight));
        params.add(btnTouchId);
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(null);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(view.ORDER_APPLYTOUCH, params, premissions);
    }

}
