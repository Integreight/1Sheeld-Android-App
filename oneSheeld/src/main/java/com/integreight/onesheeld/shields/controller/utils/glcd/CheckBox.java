package com.integreight.onesheeld.shields.controller.utils.glcd;

import android.util.Log;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 7/22/2015.
 */
public class CheckBox implements ButtonShape {
    float btnX,btnY,btnSize,btnWidth,btnHeight;
    int btnTouchId;
    String btnText = "";
    boolean isSelected = false,isPressed=false,visibility=true;
    byte size;

    public CheckBox(GlcdView view,float x, float y, byte size, int touchId, String text){
        this.btnX = x;
        this.btnY = y;
        this.btnText = text;
        setSize(view,size);
        setBtnTouchId(view,touchId);
        isSelected = false;
        isPressed = false;
    }

    @Override
    public void applyTouch(GlcdView view){
        float xMax = btnX+btnSize+view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        float yMax = btnY+btnHeight;
        for (float x=btnX;x<xMax;x++){
            for (float y=btnY;y<yMax;y++){
                view.setTouch((int) x, (int) y, btnTouchId);
            }
        }
    }

    @Override
    public void setBtnTouchId(GlcdView view,int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(view);
    }

    @Override
    public void setIsPressed(boolean isPressed) {
        if (this.isPressed == true && isPressed == false)
            setSelected(!isSelected);
        this.isPressed = isPressed;
    }

    @Override
    public void setTouched(int touchX, int touchY) {

    }

    public void setSelected(Boolean selected){
        isSelected = selected;
    }

    public boolean getSelected(){
        return isSelected;
    }

    public void setSize(GlcdView view,byte size) {
        this.size = size;
        switch (size){
            case 0:
                btnSize = 5;
                break;
            case 1:
                btnSize = 10;
                break;
            case 2:
                btnSize = 20;
                break;
        }
        this.btnWidth = btnSize+view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (btnSize < view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR))
            btnSize = view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        this.btnHeight = btnSize;
    }

    public void setText(GlcdView view,String text) {
        this.btnText = text;
        this.btnWidth = btnSize+view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (btnSize < view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR))
            btnSize = view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
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
        if(visibility) {
            view.clear(view.WHITE, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight, true, false);

            view.fillRectangle(btnX, btnY, btnSize - 1, btnSize, view.WHITE);
            view.drawRectangle(btnX, btnY, btnSize - 1, btnSize, view.BLACK);
            if (isSelected) view.fillRectangle(btnX, btnY, btnSize - 1, btnSize, view.BLACK);
            view.drawString(btnText, btnX + btnSize + 2, btnY + (btnSize / 2) - (view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR) / 2) + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
        }
    }
}
