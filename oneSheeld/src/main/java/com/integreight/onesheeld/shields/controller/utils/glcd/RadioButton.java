package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 7/22/2015.
 */
public class RadioButton implements ButtonShape {
    // btnX , btnY are the center of the circle
    float btnX,btnY,btnRadius,btnWidth,btnHeight;
    RadioGroup radioGroup;
    int btnTouchId;
    String btnText = "";
    byte size = 0;
    boolean isSelected = false,isPressed=false,visibility= true;

    public RadioButton (GlcdView view,float x,float y,byte size,int touchId,String text){
        this.size = size;
        setSize(size);
        this.btnX = x-btnRadius;
        this.btnY = y-btnRadius;
        this.btnText = text;
        this.btnWidth = btnRadius+btnRadius+btnRadius+view.getStringWidth(text,GlcdView.TEXT_SMALL,GlcdView.FONT_ARIEL_REGULAR);
        this.btnHeight = btnRadius+btnRadius+btnRadius;

        this.btnTouchId = touchId;
        applyTouch(view);
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
    public void applyTouch(GlcdView view){
//        for (float x=btnX-btnRadius;x<btnX+btnWidth;x++){
//            for (float y=btnY-btnRadius;y<btnY+btnRadius;y++){
//            }
//        }

        List<Integer> params = new ArrayList<>();
        params.add((int) (btnX-btnRadius));
        params.add((int) (btnY-btnRadius));
        params.add((int) (btnX+btnWidth));
        params.add((int) (btnY+btnRadius));
        params.add(btnTouchId);
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(null);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(view.ORDER_APPLYTOUCH, params, premissions);
    }

    @Override
    public void setBtnTouchId(GlcdView view,int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(view);
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        if (isPressed == true) {
            setSelected(true);
            if (radioGroup != null)
                radioGroup.select(this);
            this.isPressed = isPressed;
        }
        return true;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return false;
    }

    public void setSelected(Boolean selected){
        isSelected = selected;
    }

    public boolean getSelected(){
        return isSelected;
    }

    public void setSize(byte size) {
        this.size = size;
        switch (this.size){
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

    public void setText(GlcdView view,String text) {
        this.btnText = text;
        this.btnWidth = btnX+btnRadius+btnRadius+btnRadius+view.getStringWidth(text,GlcdView.TEXT_SMALL,GlcdView.FONT_ARIEL_REGULAR);
        this.btnHeight = btnY+btnRadius+btnRadius+btnRadius;;
    }

    @Override
    public void draw(GlcdView view) {
        clearDraw(view);
        if (visibility) {
//            view.clear(view.WHITE, (int) (btnX - btnRadius), (int) (btnY - btnRadius), (int) btnWidth, (int) btnHeight, true, false);

            view.fillCircle(btnX, btnY, btnRadius, view.WHITE);
            view.drawCircle(btnX, btnY, btnRadius, view.BLACK);
            if (isSelected) view.fillCircle(btnX, btnY, btnRadius - 2, view.BLACK);
            view.drawString(btnText, btnX + btnRadius + 2, btnY - btnRadius + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
        }
    }

    @Override
    public void clearDraw(GlcdView view) {
//            view.clear(view.WHITE, (int) (btnX - btnRadius), (int) (btnY - btnRadius), (int) btnWidth, (int) btnHeight, true, false);
            view.fillCircle(btnX, btnY, btnRadius, view.WHITE);
            view.drawCircle(btnX, btnY, btnRadius, view.WHITE);
            view.fillCircle(btnX, btnY, btnRadius - 2, view.WHITE);
            view.drawString(btnText, btnX + btnRadius + 2, btnY - btnRadius + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.WHITE);

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
