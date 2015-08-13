package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 7/22/2015.
 */
public class Button implements ButtonShape {
    float btnX, btnY, btnWidth, btnHeight;
    int btnTouchId;
    String btnText = "..";
    float btnTextX, btnTextY;
    int textWidth = 0, textHeight = 0;
    boolean isPressed = false, visibility = true;
    byte style = 0;
    boolean changed = false;

    public Button(GlcdShield controller, float x, float y, float width, float height, int touchId, String text) {
        this.btnX = x;
        this.btnY = y;
        this.btnWidth = width;
        this.btnHeight = height;
        //set text width and height to min
        textWidth = controller.getView().getStringWidth("..", GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        textHeight = controller.getView().getCharHeight(GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        setText(controller.getView(), text);
        if (height < textHeight)
            this.btnHeight = textHeight;

        btnTextX = btnX + ((btnWidth - textWidth) / 2);
        btnTextY = btnY + ((btnHeight - textHeight) / 2);

        this.btnTouchId = touchId;
        applyTouch(controller);

        isPressed = false;
        style = 0;
    }

    @Override
    public void applyTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX));
            params.add((int) (btnY));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnHeight));
            params.add(btnTouchId);
            controller.doOrder(GlcdShield.ORDER_APPLYTOUCH, params);
        }
    }

    @Override
    public void clearTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX));
            params.add((int) (btnY));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnHeight));
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
    public void draw(GlcdView view) {
        if (visibility) {
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
        changed = true;
    }

    public void setStyle(byte style) {
        this.style = style;
    }

    public void setText(GlcdView view, String text) {
        this.btnText = text;
        textWidth = view.getStringWidth(text, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        if (btnWidth < textWidth) {
            this.btnText = text.substring(0, view.getMaxCharsInWidth(text, btnWidth, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR) - 2);
            this.btnText += "..";
        } else {
            this.btnText = text;
        }
        textWidth = view.getStringWidth(btnText, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR);
        btnTextX = btnX + ((btnWidth - textWidth) / 2);
        btnTextY = btnY + ((btnHeight - textHeight) / 2);
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

    private void releaseDraw(GlcdView view) {
        if (style == 0) {
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, GlcdShield.WHITE + 1);
            view.drawRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, GlcdShield.BLACK);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR, GlcdShield.BLACK);
        } else {
            view.fillRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, GlcdShield.BLACK);
            view.fillRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, GlcdShield.WHITE);

            view.drawRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, GlcdShield.BLACK);
            view.drawShadowRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, GlcdShield.BLACK);

            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR, GlcdShield.BLACK);
        }
    }

    private void pressDraw(GlcdView view) {
        if (style == 0) {
            view.fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, GlcdShield.BLACK);
            view.drawString(this.btnText, btnTextX + 2, btnTextY + 2, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR, GlcdShield.WHITE + 1);
        } else {
            view.drawRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, GlcdShield.BLACK);
            view.drawString(this.btnText, btnTextX, btnTextY, GlcdShield.TEXT_SMALL, GlcdShield.FONT_ARIEL_REGULAR, GlcdShield.BLACK);
        }
    }

}
