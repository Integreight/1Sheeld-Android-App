package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 7/22/2015.
 */
public class Button implements ButtonShape {
    float btnX,btnY,btnWidth,btnHeight;
    int btnTouchId;
    String btnText = " .";
    float btnTextX,btnTextY;
    int textWidth = 0,textHeight = 0;
    boolean isPressed=false,visibility=true;
    byte style = 0;

    public Button (GlcdView view,float x,float y,float width,float height,int touchId,String text){
        this.btnX = x;
        this.btnY = y;
        //set text width and height to min
        textWidth = view.getStringWidth(" .", view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        textHeight = view.getCharHeight(view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (width < textWidth)
            this.btnWidth = textWidth;
        else {
            this.btnWidth = width;
            textWidth = view.getStringWidth(text, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
            if (width < textWidth){
                this.btnText = text.substring(0, view.getMaxCharsInWidth(text, width, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR)-2);
                this.btnText += " .";
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
        for (float x=btnX;x<btnX+btnWidth;x++){
            for (float y=btnY;y<btnY+btnHeight;y++){
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
    public void draw(GlcdView view) {
        if (visibility) {
            view.clear(view.WHITE, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
            if (isPressed) {
                pressDraw(view);
            } else {
                releaseDraw(view);
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
    }

    public void setStyle(byte style) {
        this.style = style;
    }

    public void setText(GlcdView view,String text) {
        this.btnText = text;
        textWidth = view.getStringWidth(text, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR);
        if (btnWidth < textWidth){
            this.btnText = text.substring(0, view.getMaxCharsInWidth(text, btnWidth, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR)-2);
            this.btnText += " .";
        }else{
            this.btnText = text;
        }
        btnTextX = btnX+((btnWidth-textWidth)/2);
    }

    public void setWidth(float width) {
        this.btnWidth = width;
    }

    public void setHeight(float height) {
        this.btnHeight = height;
    }

    @Override
    public void setIsPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    @Override
    public void setTouched(int touchX, int touchY) {

    }

    private void releaseDraw(GlcdView view) {
        if (style == 0){
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, view.WHITE + 1);
            view.drawRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, view.BLACK);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
        }else {
            view.fillRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, view.BLACK);
            view.fillRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, view.WHITE);

            view.drawRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, view.BLACK);
            view.drawShadowRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, view.BLACK);

            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
        }
    }

    private void pressDraw(GlcdView view){
        if (style == 0){
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, view.BLACK);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.WHITE + 1);
        }else {
            view.drawRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, view.BLACK);
            view.drawString(this.btnText, btnTextX, btnTextY, view.TEXT_SMALL, view.FONT_ARIEL_REGULAR, view.BLACK);
        }
    }

}
