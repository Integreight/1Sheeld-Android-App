package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 7/22/2015.
 */
public class Button implements ButtonShape {
    float btnX,btnY,btnWidth,btnHeight;
    int btnTouchId;
    String btnText = "..";
    float btnTextX,btnTextY;
    int textWidth = 0,textHeight = 0;
    boolean isPressed=false,visibility=true;
    byte style = 0;
    boolean changed = false;

    public Button (GlcdView view,float x,float y,float width,float height,int touchId,String text){
        this.btnX = x;
        this.btnY = y;
        //set text width and height to min
        textWidth = view.getStringWidth("..", view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        textHeight = view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (width < textWidth)
            this.btnWidth = textWidth;
        else {
            this.btnWidth = width;
            textWidth = view.getStringWidth(text, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
            if (width < textWidth){
                this.btnText = text.substring(0, view.getMaxCharsInWidth(text, width, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR)-2);
                this.btnText += "..";
            }else{
                this.btnText = text;
            }
        }
        if (height < textHeight)
            this.btnHeight = textHeight;
        else
            this.btnHeight = height;

        btnTextX = btnX+((btnWidth-textWidth)/2);
        btnTextY = btnY+((btnHeight-textHeight)/2);

        this.btnTouchId = touchId;
        applyTouch(view);

        isPressed=false;
        style = 0;
    }

    @Override
    public void applyTouch(GlcdView view){
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

    @Override
    public void clearTouch(GlcdView view) {
        List<Integer> params = new ArrayList<>();
        params.add((int) (btnX));
        params.add((int) (btnY));
        params.add((int) (btnX+btnWidth));
        params.add((int) (btnY+btnHeight));
        params.add(null);
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
    public void draw(GlcdView view) {
        if (visibility) {
            if (isPressed) {
                pressDraw(view,view.BLACK);
            } else {
                releaseDraw(view,view.BLACK);
            }
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
        changed = true;
    }

    public void setStyle(byte style) {
        this.style = style;
    }

    public void setText(GlcdView view,String text) {
        this.btnText = text;
        textWidth = view.getStringWidth(text, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (btnWidth < textWidth){
            this.btnText = text.substring(0, view.getMaxCharsInWidth(text, btnWidth, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR)-2);
            this.btnText += "..";
        }else{
            this.btnText = text;
        }
        textWidth = view.getStringWidth(btnText, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        btnTextX = btnX+((btnWidth-textWidth)/2);
        btnTextY = btnY+((btnHeight-textHeight)/2);
    }

    public void setWidth(float width) {
        this.btnWidth = width;
        changed = true;
    }

    public void setHeight(float height) {
        this.btnHeight = height;
        changed = true;
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        this.isPressed = isPressed;
        return true;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return false;
    }

    public String getText() {
        return btnText;
    }

    private void releaseDraw(GlcdView view,int color) {
        if (style == 0){
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, view.WHITE + 1);
            view.drawRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, color);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, color);
        }else {
            view.fillRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, view.BLACK);
            view.fillRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, view.WHITE);

            view.drawRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, color);
            view.drawShadowRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, color);

            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, color);
        }
    }

    private void pressDraw(GlcdView view,int color){
        if (style == 0){
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, color);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.WHITE + 1);
        }else {
            view.drawRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, color);
            view.drawString(this.btnText, btnTextX, btnTextY, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, color);
        }
    }

}
