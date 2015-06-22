package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdView extends View implements OnTouchListener {

    Context context;
    private Canvas canvas;
    int background = 0;
    Paint paint;
    int glcdWidth=256,glcdHeight=128;
    ArrayList<ArrayList<Integer>> Dots,Touchs;
    ArrayList<ButtonShape> buttons;
    int BLACK= Color.parseColor("#11443d"),WHITE=Color.parseColor("#338f45");
    float pixelX,pixelY,originX,originY,width,height,ascpectRatio;

    public static final int TEXT_SMALL=0,TEXT_MEDUIM=1,TEXT_LARGE=2;

    private boolean isInt=false;
    private int currentPressedKey = 0;

    public GlcdView(Context context) {
        super(context);
        this.context = context;
        paint = new Paint();
        setOnTouchListener(this);
        clear(WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        ascpectRatio = glcdWidth/glcdHeight;
        width = canvas.getWidth();
        height = canvas.getWidth()/ascpectRatio;
        originY = (canvas.getHeight() - height) / ascpectRatio;
        pixelX = width/glcdWidth;
        pixelY = height/glcdHeight;

        //   draw Background
        paint.setColor(background);
        canvas.drawRect(originX, originY, originX + (glcdWidth * pixelX), originY + (glcdHeight * pixelY), paint);

        //------------------------------------

        if (isInt == false) {
            isInt = true;

            /*button btn = new button(0, 30, 100, 30, "Hello");
            btn.setIsPressed(false);
            buttons.add(btn);
            btn.setBtnTouchId(buttons.size());
            btn.applyTouch();*/

            /*radioButton btn1 = new radioButton(5,5,5,"Male");
            btn1.setIsPressed(false);
            btn1.setSelected(false);
            buttons.add(btn1);
            btn1.setBtnTouchId(buttons.size());
            btn1.applyTouch();

            radioButton btn2 = new radioButton(5,20,5,"Female");
            btn2.setIsPressed(false);
            btn2.setSelected(false);
            buttons.add(btn2);
            btn2.setBtnTouchId(buttons.size());
            btn2.applyTouch();

            RadioGroup radioGroup = new RadioGroup();
            radioGroup.add(btn1);
            radioGroup.add(btn2);*/


            /*checkBox btn = new checkBox(5,5,12,"Check box");
            btn.setIsPressed(false);
            buttons.add(btn);
            btn.setBtnTouchId(buttons.size());
            btn.applyTouch();*/

            /*HorSlider btn = new HorSlider(15,15,100,10,BLACK);
            btn.setIsPressed(false);
            buttons.add(btn);
            btn.setBtnTouchId(buttons.size());
            btn.applyTouch();*/

        }

        //------------------------------------
        for (int btnCount=0;btnCount<buttons.size();btnCount++){
            buttons.get(btnCount).draw();
        }


        //drawString("Hello World!!",5,50,TEXT_MEDUIM,BLACK);
        refresh(false);
    }

    private void clear(int background){
        paint = new Paint();
        this.background = background;
        Dots = new ArrayList<ArrayList<Integer>>();
        Touchs = new ArrayList<ArrayList<Integer>>();
        buttons = new ArrayList<ButtonShape>();
        for (int x=0;x<glcdWidth;x++){
            ArrayList<Integer> tempDots = new ArrayList<Integer>();
            ArrayList<Integer> tempTouchs = new ArrayList<Integer>();
            for (int y=0;y<glcdHeight;y++){
                tempDots.add(this.background);
                tempTouchs.add(0);
            }
            Dots.add(tempDots);
            Touchs.add(tempTouchs);
        }
    }

    private void clear(int background,int x,int y,int width,int height,boolean clearGraphics,boolean clearTouch){
        paint = new Paint();
        paint.setColor(background);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((x * pixelX) + originX, (y * pixelY) + originY, ((x + width) * pixelX) + originX, ((y + height) * pixelY) + originY, paint);
        for (int X=x;X<width+x;X++){
            ArrayList<Integer> tempDots = Dots.get(X);
            ArrayList<Integer> tempTouchs = Touchs.get(X);
            for (int Y=y;Y<height+y;Y++){
                if (clearGraphics) tempDots.set(Y,background);
                if (clearTouch) tempTouchs.set(Y,0);
            }
        }
    }

    private void clear(int background,int x,int y,int width,int height){
        clear(background, x, y, width, height, true, true);
    }

    private void refresh(boolean doInvalidation){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        // draw pixels
        for (int x = 0;x<glcdWidth;x++){
            for (int y=0;y<glcdHeight;y++){
                if (Dots.get(x).get(y) != background) {
                    paint.setColor(Dots.get(x).get(y));
                    canvas.drawRect(x*pixelX + originX, y*pixelY + originY, (x+1)*pixelX + originX, (y+1)*pixelY + originY, paint);
                }
            }
        }

        if (doInvalidation) {
            invalidate();
            requestLayout();
        }
    }

    private void refresh(boolean doInvalidation,int x,int y,int width,int height){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x * originX, y * originY, (x + width) * originX, (y + height) * originY, paint);
        // draw pixels
        for (int X = x;X<width+x;X++){
            for (int Y=x;Y<height+y;Y++){
                if (Dots.get(X).get(Y) != background) {
                    paint.setColor(Dots.get(X).get(Y));
                    canvas.drawRect(X*pixelX + originX, Y*pixelY + originY, (X+1)*pixelX + originX, (Y+1)*pixelY + originY, paint);
                }
            }
        }

        if (doInvalidation) {
            invalidate((int) (x * pixelX), (int) (y * pixelY), (int) ((x + width) * pixelX), (int) ((y + height) * pixelY));
        }
    }

    public float absDiff(float a, float b){
        if (a>b)
            return a-b;
        return b-a;
    }

    public void setPixel(int x, int y, int color){
        if (x < Dots.size() && x >= 0)
            if (y < Dots.get(x).size() && y >= 0)
                Dots.get(x).set(y,color);
    }

    public void setTouch(int x, int y, int touchId){
        if (x < Touchs.size() && x >= 0)
            if (y < Touchs.get(x).size() && y >= 0)
                Touchs.get(x).set(y,touchId);
    }

    public int getTouch(int x, int y){
        if (x < Touchs.size() && x >= 0)
            if (y < Touchs.get(x).size() && y >= 0)
                return Touchs.get(x).get(y);
        return 0;
    }

    public void drawLine(float x1,float y1,float x2,float y2,int color) {
        float deltaX, deltaY, x, y;
        boolean steep;
        float error, yStep;
        if (x1 > glcdWidth) x1 = 0;
        if (x2 > glcdWidth) x2 = 0;
        if (y1 > glcdHeight) y1 = 0;
        if (y2 > glcdHeight) y1 = 0;

        steep = absDiff(y1, y2) > absDiff(x1, x2);
        if (steep) {
            float temp0 = y1;
            y1 = x1;
            x1 = temp0;
            float temp1 = y2;
            y2 = x2;
            x2 = temp1;
        }
        if (x1 > x2) {
            float temp0 = x1;
            x1 = x2;
            x2 = temp0;
            float temp1 = y1;
            y1 = y2;
            y2 = temp1;
        }

        deltaX = x2 - x1;
        deltaY = absDiff(y1, y2);
        error = deltaX / 2;
        y = y1;
        if (y1 < y2)
            yStep = 1;
        else
            yStep = -1;

        for (x = x1; x <= x2; x++) {
            if (steep)
                setPixel((int) y, (int) x, color);
            else
                setPixel((int) x, (int) y, color);
            error -= deltaY;
            if (error < 0) {
                y = y + yStep;
                error += deltaX;
            }
        }
    }

    public void drawRectangle(float x,float y,float width,float height,int color){
        if (x+width > glcdWidth)
            width = glcdWidth-x-1;
        if (y+height > glcdHeight)
            height = glcdHeight-y-1;

        drawLine(x,y,x+width,y,color);
        drawLine(x, y, x, y + height, color);
        drawLine(x + width, y, x + width, y + height, color);
        drawLine(x, y + height, x + width, y + height, color);
    }

    public void fillRectangle(float x,float y,float width,float height,int color){
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x * pixelX + originX, y * pixelY + originY, (x + width + 1) * pixelX + originX, (y + height + 1) * pixelY + originY, paint);
    }

    public void drawRoundRectangle(float x,float y,float width,float height,float radius,int color){
        if (width >= height) {
            if (radius > (width / 2))
                radius = (width / 2);
        }else {
            if (radius > (height/2))
                radius = (height/2);
        }

        drawLine(x + radius, y, x + width - (radius), y, color);
        drawLine(x + radius, y + height, x + width - (radius), y + height, color);
        drawLine(x, y + radius, x, y + height - (radius), color);
        drawLine(x + width, y + radius, x + width, y + height - (radius), color);

        float tSwitch;
        float x1=0,y1=radius;
        tSwitch = 3-2*radius;
        while (x1<=y1){
            setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
            setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);

            setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
            setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);

            setPixel((int) (x + width - radius + x1), (int) (y + height - radius + y1), color);
            setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), color);

            setPixel((int) (x + radius - x1), (int) (y + height - radius + y1), color);
            setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);

            if (tSwitch <0)
                tSwitch += (4*x1+6);
            else {
                tSwitch += (4*(x1-y1)+10);
                y1--;
            }
            x1++;
        }
    }

    public void drawShadowRoundRectangle(float x, float y, float width, float height, float radius, int color){
        if (width >= height) {
            if (radius > (width / 2))
                radius = (width / 2);
        }else {
            if (radius > (height/2))
                radius = (height/2);
        }

        drawLine(x + radius, y, x + width - (radius), y, color);
        drawLine(x + radius, y + height, x + width - (radius), y + height, background);
        drawLine(x, y + radius, x, y + height - (radius), color);
        drawLine(x + width, y + radius, x + width, y + height - (radius), background);

        float tSwitch;
        float x1=0,y1=radius;
        tSwitch = 3-2*radius;
        while (x1<=y1){
            setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
            setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);

            setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
            setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);

            setPixel((int) (x + width - radius + x1), (int) (y + height - radius + y1), background);
            setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), background);

            setPixel((int) (x + radius - x1), (int) (y + height - radius + y1), color);
            setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);

            if (tSwitch <0)
                tSwitch += (4*x1+6);
            else {
                tSwitch += (4*(x1-y1)+10);
                y1--;
            }
            x1++;
        }
    }

    public void fillRoundRectangle(float x,float y,float width,float height,float radius,int color){
        if (width >= height) {
            if (radius > (width / 2))
                radius = (width / 2);
        }else {
            if (radius > (height/2))
                radius = (height/2);
        }


        drawRoundRectangle(x, y, width, height, radius, color);
        fillRectangle(x + radius, y, width - 2 * radius, height, color);
        fillRectangle(x, y + radius, width, height - 2 * radius, color);

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(((x + radius) * pixelX) + originX + (pixelX / 2), ((y + radius) * pixelY) + originY + (pixelX / 2), (radius * pixelX), paint);
        canvas.drawCircle(((x + width - radius) * pixelX) + originX + (pixelX / 2), ((y + radius) * pixelY) + originY + (pixelX / 2), (radius * pixelX), paint);
        canvas.drawCircle(((x + radius) * pixelX) + originX + (pixelX / 2), ((y + height - radius) * pixelY) + originY + (pixelX / 2), (radius * pixelX), paint);
        canvas.drawCircle(((x + width - radius) * pixelX) + originX + (pixelX / 2), ((y + height - radius) * pixelY) + originY + (pixelX / 2), (radius * pixelX), paint);

    }

    public void drawCircle(float xCenter,float yCenter,float radius,int color){
        drawRoundRectangle(xCenter - radius, yCenter - radius, 2 * radius, 2 * radius, radius, color);
    }

    public void fillCircle(float xCenter,float yCenter, float radius, int color) {
        drawCircle(xCenter, yCenter, radius, color);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((xCenter * pixelX) + originX + (pixelX / 2), (yCenter * pixelY) + originY + (pixelX / 2), (radius * pixelX), paint);
    }

    public void drawChar(char c,float x,float y,int textSize,int color){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        font1 font01 = new font1();

        if (c < font01.getFirst_char() || c > font01.getFirst_char()+font01.getChar_Count()){
            return;
        }
        c -= font01.getFirst_char();

        float multiplier = 1;

        for (float i=0;i<font01.getFont_height();i+= multiplier){
            int chByte = font01.getFont()[c][(int) i];
            for (float j=0;j<font01.getFont_width();j+= multiplier){
                if ((chByte & 0x80)== 0x80) {
                    if (textSize == TEXT_LARGE) {
                        canvas.drawRect((y+(j*4))*pixelX + originX,(x+(i*4))*pixelY + originY, (y+(j*4)+4)*pixelX + originX,(x+(i*4)+4)*pixelY + originY, paint);
                    }else if (textSize == TEXT_MEDUIM) {
                        canvas.drawRect((y + (j * 2)) * pixelX + originX, (x + (i * 2)) * pixelY + originY, (y + (j * 2) + 2) * pixelX + originX, (x + (i * 2) + 2) * pixelY + originY, paint);
                    }else if (textSize == TEXT_SMALL)
                        setPixel((int) (y + j), (int) (x + i), color);
                }
                chByte <<= 1;
            }
        }
    }

    public void drawString(String text,float x,float y,int textSize,int color){
        if (text != null) {
            int spacer;
            switch (textSize){
                case TEXT_SMALL:
                    spacer = 8;
                    break;
                case TEXT_MEDUIM:
                    spacer = 16;
                    break;
                case TEXT_LARGE:
                    spacer = 32;
                    break;
                default:
                    spacer = 16;
                    textSize = TEXT_MEDUIM;
                    break;
            }
            for (int charCount = 0; charCount < text.length(); charCount++) {
                drawChar(text.charAt(charCount), y, x + (charCount * spacer),textSize,color);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == 1 || action == 0) {
            float x = event.getX();
            float y = event.getY();
            if (x >= originX && x < originX + width && y >= originY && y < originY + height) {
                x -= originX;
                y -= originY;
                x /= pixelX;
                y /= pixelY;
                if (getTouch((int) x,(int) y) != 0){
                    if (action == 1) {
                        releaseThis(getTouch((int) x,(int) y) - 1);
                    }else
                        pressThis(getTouch((int) x, (int) y)-1);

                }else {
                    releaseThis(currentPressedKey);
                }
            }else {
                releaseThis(currentPressedKey);
            }
        }else if (action == 2){
            float x = event.getX();
            float y = event.getY();
            if (x >= originX && x < originX + width && y >= originY && y < originY + height) {
                x -= originX;
                y -= originY;
                x /= pixelX;
                y /= pixelY;
                if (getTouch((int) x, (int) y) != 0){
                        touchThis(getTouch((int) x,(int) y) - 1,(int) x,(int) y);
                }
            }
        }
        return true;
    }

    public void pressThis(int key){
        if (!buttons.isEmpty()) {
            if (buttons.get(currentPressedKey).getClass().equals(Button.class)) releaseThis(currentPressedKey);
            buttons.get(key).setIsPressed(true);
        }
    }

    public void releaseThis(int key){
        if (!buttons.isEmpty()) {
            if (buttons.get(currentPressedKey).getClass().equals(radioButton.class)){
            }else {
                buttons.get(key).setIsPressed(false);
            }
        }
    }


    public void touchThis(int key,int touchX,int touchY){
        if (!buttons.isEmpty()) {
            if (buttons.get(currentPressedKey).getClass().equals(HorSlider.class)){
                buttons.get(currentPressedKey).setTouched(touchX,touchY);
            }
        }
    }

    public interface Shape{
        void draw();
    }

    public interface ButtonShape extends Shape{
        void draw();
        void setIsPressed(boolean isPressed);
        void setTouched(int touchX,int touchY);
        void setBtnTouchId(int btnTouchId);
        void applyTouch();
    }

    public class RadioGroup{
        SparseArray<radioButton> radios;
        public RadioGroup(){
            radios = new SparseArray<radioButton>();
        }

        public void add(radioButton radioButton){
            radios.append(radios.size(),radioButton);
            radioButton.setRadioGroup(this);
        }

        public void remove(radioButton radioButton){
            radios.remove(radios.keyAt(radios.indexOfValue(radioButton)));
        }

        public void select(radioButton radioButton){
            reset();
            radios.get(radios.keyAt(radios.indexOfValue(radioButton))).setSelected(true);
        }

        public void reset(){
            for(int radiosCount=0;radiosCount<radios.size();radiosCount++){
                radios.get(radios.keyAt(radiosCount)).setSelected(false);
            }
        }
    }

    private class ProgressBar implements Shape{

        float x,y,width,height,start,end,currentValue;
        int color;

        public ProgressBar(float x,float y,float width,float height,int color){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            this.currentValue = start;
        }

        public ProgressBar(float x,float y,float width,float height,int color,float value){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.currentValue = value;
        }

        public ProgressBar(float x,float y,float width,float height,int color,float start,float end){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.start = start;
            this.end = end;
            this.currentValue = start;
        }

        public ProgressBar(float x,float y,float width,float height,int color,float start,float end,float value){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.start = start;
            this.end = end;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.currentValue = value;
        }

        public void setCurrentValue(float value) {
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.currentValue = value;
        }

        @Override
        public void draw() {
            float progress = (((currentValue-start)*((x+width)-(x+5)))/(end-start))+(x+5);

            drawRoundRectangle(x,y,width,height,5,color);
            fillRoundRectangle(x,y,progress,height,5,color);
        }
    }

    private class AnalogGauge implements Shape{
        private float xCenter, yCenter,radius,start=0,end=100,currentValue=0,angleStart=2.355f,angleEnd=7.065f;
        int color;
        public AnalogGauge(float xCenter,float yCenter,float radius,int color,float start,float end){
            this.xCenter = xCenter;
            this.yCenter = yCenter;
            this.radius = radius;
            if (start > end){
                float temp = end;
                end = start;
                start = temp;
            }
            this.start = start;
            this.end = end;
            this.color = color;
            this.currentValue = 0;
        }

        public AnalogGauge(float xCenter,float yCenter,float radius,int color,float start,float end,float currentValue){
            this.xCenter = xCenter;
            this.yCenter = yCenter;
            this.radius = radius;
            if (start > end){
                float temp = end;
                end = start;
                start = temp;
            }
            this.start = start;
            this.end = end;
            this.color = color;
            this.currentValue = currentValue;
        }

        @Override
        public void draw() {
            //using Linear Interpolation get the angle corresponding to the given value
            //http://www.ajdesigner.com/phpinterpolation/linear_interpolation_equation.php
            float angle = (((currentValue-start)*(angleEnd-angleStart))/(end-start))+angleStart;

            GlcdView.this.fillCircle(xCenter, yCenter, radius, GlcdView.this.background);
            drawPartOfCircle(radius, color);
            drawPartOfCircle((float) (radius * 0.8), color);
            drawPointer((float) (radius * 0.7), angle, color);
        }

        public void draw(float value){
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.currentValue = value;
            draw();
        }

        private void drawIndicator(float angleInRadian,int color){
            float x1 = (float) (xCenter + (radius * 0.8 * Math.cos(angleInRadian))) ,x2=(float) (xCenter + (radius * Math.cos(angleInRadian)));
            float y1 = (float) (yCenter + (radius * 0.8 * Math.sin(angleInRadian))) ,y2=(float) (yCenter + (radius * Math.sin(angleInRadian)));
            GlcdView.this.drawLine(x1, y1, x2, y2, color);
        }

        private void drawPointer(float radius,float angleInRadian,int color){
            float x1 = xCenter,x2=(float) (xCenter+(radius*Math.cos(angleInRadian)));
            float y1 = yCenter,y2=(float) (yCenter + (radius * Math.sin(angleInRadian)));
            GlcdView.this.drawLine(x1, y1, x2, y2, color);
        }

        private void drawPartOfCircle(float radius,int color){
            float x = xCenter - radius;
            float y = yCenter - radius;
            float width = 2*radius,height = 2*radius;

            if (width >= height) {
                if (radius > (width / 2))
                    radius = (width / 2);
            }else {
                if (radius > (height/2))
                    radius = (height/2);
            }

            float tSwitch;
            float x1=0,y1=radius;
            tSwitch = 3-2*radius;
            while (x1<=y1){
                GlcdView.this.setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
                GlcdView.this.setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);

                GlcdView.this.setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
                GlcdView.this.setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);

                GlcdView.this.setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), color);

                GlcdView.this.setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);


                if (tSwitch <0)
                    tSwitch += (4*x1+6);
                else {
                    tSwitch += (4*(x1-y1)+10);
                    y1--;
                }
                x1++;
            }

        }

    }

    private class HorSlider implements ButtonShape{

        float btnX,btnY,btnWidth,btnHeight,start,end,currentValue;
        int btnTouchId;
        boolean isPressed=false;
        int color;

        public HorSlider(float x, float y, float width, float height, int color){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            this.btnTouchId = 0;
            this.currentValue = start;
        }

        public HorSlider(float x, float y, float width, float height, int color, int touchId){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            this.btnTouchId = touchId;
            this.currentValue = start;
        }

        public HorSlider(float x, float y, float width, float height, int color, float value){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.btnTouchId = 0;
            this.currentValue = value;
        }

        public HorSlider(float x, float y, float width, float height, int color, float value, int touchId){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = 0;
            this.end = 100;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.btnTouchId = touchId;
            this.currentValue = value;
        }

        public HorSlider(float x, float y, float width, float height, int color, float start, float end){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = start;
            this.end = end;
            this.btnTouchId = 0;
            this.currentValue = start;
        }

        public HorSlider(float x, float y, float width, float height, int color, float start, float end, int touchId){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = start;
            this.end = end;
            this.btnTouchId = touchId;
            this.currentValue = start;
        }

        public HorSlider(float x, float y, float width, float height, int color, float start, float end, float value){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = start;
            this.end = end;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.btnTouchId = 0;
            this.currentValue = value;
        }

        public HorSlider(float x, float y, float width, float height, int color, float start, float end, float value, int touchId){
            this.btnX = x;
            this.btnY = y;
            this.btnWidth = width;
            this.btnHeight = height;
            this.color = color;
            this.start = start;
            this.end = end;
            if (value < start)
                value = start;
            else if (value > end)
                value = end;
            this.btnTouchId = touchId;
            this.currentValue = value;
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
        public void draw() {

            float progress = (((currentValue-start)*((btnX+btnWidth-(btnHeight/2))-(btnX+(btnHeight/2))))/(end-start))+(btnX+(btnHeight/2));

            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
            drawLine(btnX, btnY + (btnHeight / 2), btnX + btnWidth, btnY + (btnHeight / 2), BLACK);
            fillCircle(progress, btnY + (btnHeight / 2), btnHeight / 2, BLACK);
            refresh(true, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1);
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
        public void setBtnTouchId(int btnTouchId) {
            this.btnTouchId = btnTouchId;
        }

        @Override
        public void applyTouch() {
            for (float x=btnX;x<btnX+btnWidth;x++){
                for (float y=btnY;y<btnY+btnHeight;y++){
                    setTouch((int) x,(int) y,btnTouchId);
                }
            }
        }
    }

    public class radioButton implements ButtonShape{
        float btnX,btnY,btnRadius,btnWidth,btnHeight;
        RadioGroup radioGroup;
        int btnTouchId;
        String btnText = "";
        boolean isSelected = false,isPressed=false;

        public radioButton (float xCenter,float yCenter,float radius,String text,int touchId){
            this.btnX = xCenter;
            this.btnY = yCenter;
            this.btnRadius = radius;
            this.btnText = text;
            this.btnTouchId = touchId;
            isSelected = false;

            this.btnWidth = (2*btnRadius)+btnText.length()*8;
            if (radius < 8)
                this.btnHeight = 15;
            else
                this.btnHeight = 2*btnRadius;
        }

        public radioButton (float xCenter,float yCenter,float radius,String text){
            this.btnX = xCenter;
            this.btnY = yCenter;
            this.btnRadius = radius;
            this.btnText = text;
            this.btnTouchId = 0;
            isSelected = false;

            this.btnWidth = (2*btnRadius)+btnText.length()*8;
            if (radius < 8)
                this.btnHeight = 15;
            else
                this.btnHeight = 2*btnRadius;
        }

        public void setRadioGroup(RadioGroup radioGroup) {
            this.radioGroup = radioGroup;
        }

        public RadioGroup getRadioGroup() {
            return radioGroup;
        }

        @Override
        public void applyTouch(){
            float right = btnX+btnRadius+(btnText.length()*8);

            for (float x=btnX-btnRadius;x<right;x++){
                for (float y=btnY-btnRadius;y<btnY+btnRadius;y++){
                    setTouch((int) x,(int) y,btnTouchId);
                }
            }
        }

        @Override
        public void setBtnTouchId(int btnTouchId) {
            this.btnTouchId = btnTouchId;
        }

        @Override
        public void setIsPressed(boolean isPressed) {
            setSelected(true);
            if (radioGroup != null)
                radioGroup.select(this);
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

        @Override
        public void draw() {
            clear(background,(int) (btnX-btnRadius),(int) (btnY-btnRadius),(int) btnWidth,(int) btnHeight,true,false);

            fillCircle(btnX, btnY, btnRadius, WHITE);
            drawCircle(btnX, btnY, btnRadius, BLACK);
            if (isSelected) fillCircle(btnX, btnY, btnRadius - 2, BLACK);
            drawString(btnText, btnX + btnRadius,btnY-btnRadius,TEXT_SMALL,BLACK);

            refresh(true,(int) (btnX-btnRadius),(int) (btnY-btnRadius),(int) btnWidth,(int) btnHeight);
        }
    }

    public class checkBox implements ButtonShape{
        float btnX,btnY,btnSize,btnWidth,btnHeight;
        int btnTouchId;
        String btnText = "";
        boolean isSelected = false,isPressed=false;

        public checkBox(float x, float y, float size, String text, int touchId){
            this.btnX = x;
            this.btnY = y;
            this.btnSize = size;
            this.btnText = text;
            this.btnTouchId = touchId;
            isSelected = false;
            this.btnWidth = btnSize+btnText.length()*8;
            if (btnSize < 15)
                btnSize = 15;
            this.btnHeight = btnSize;
        }

        public checkBox(float x, float y, float size, String text){
            this.btnX = x;
            this.btnY = y;
            this.btnSize = size;
            this.btnText = text;
            this.btnTouchId = 0;
            isSelected = false;
            this.btnWidth = btnSize+btnText.length()*8;
            if (btnSize < 12)
                btnSize = 12;
            this.btnHeight = btnSize;
        }

        @Override
        public void applyTouch(){
            float right = btnSize+(btnText.length()*8);

            for (float x=btnX;x<right;x++){
                for (float y=btnY;y<btnY+btnSize;y++){
                    setTouch((int) x, (int) y, btnTouchId);
                }
            }
        }

        @Override
        public void setBtnTouchId(int btnTouchId) {
            this.btnTouchId = btnTouchId;
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

        @Override
        public void draw() {
            clear(background, (int) btnX, (int) btnY, (int) btnWidth+1, (int) btnHeight, true, false);

            fillRectangle(btnX, btnY, btnSize - 1, btnSize, WHITE);
            drawRectangle(btnX, btnY, btnSize - 1, btnSize, BLACK);
            if (isSelected) fillRectangle(btnX, btnY, btnSize - 1, btnSize, BLACK);
            drawString(btnText, btnX + btnSize,btnY,TEXT_SMALL,BLACK);

            refresh(true, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight);
        }
    }

    public class button implements ButtonShape {
        float btnX,btnY,btnWidth,btnHeight;
        int btnTouchId;
        String btnText = "";
        float btnTextX,btnTextY;
        boolean isPressed=false;

        public button (float x,float y,float width,float height,String text){
            this.btnX = x;
            this.btnY = y;
            if (width < 16)
                this.btnWidth = 16;
            else
                this.btnWidth = width;
            if (height < 16)
                this.btnHeight = 16;
            else
                this.btnHeight = height;

            if (((int)(this.btnWidth/8)) < text.length()){
                this.btnText = text.substring(0, ((int) (width/8))-2);
                this.btnText += "..";
            }else{
                this.btnText = text;
            }

            btnTextX = btnX+(this.btnWidth-(text.length()*8))/2;
            btnTextY = btnY+(this.btnHeight-16)/2;
            isPressed=false;
        }

        @Override
        public void applyTouch(){
            for (float x=btnX;x<btnX+btnWidth;x++){
                for (float y=btnY;y<btnY+btnHeight;y++){
                    setTouch((int) x, (int) y, btnTouchId);
                }
            }
        }

        @Override
        public void setBtnTouchId(int btnTouchId) {
            this.btnTouchId = btnTouchId;
        }

        @Override
        public void draw(){
            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
            if (isPressed){
                pressDraw();
            }else {
                releaseDraw();
            }
            refresh(true, (int) btnX, (int) btnY, (int) btnWidth+1, (int) btnHeight+1);
        }

        @Override
        public void setIsPressed(boolean isPressed) {
            this.isPressed = isPressed;
        }

        @Override
        public void setTouched(int touchX, int touchY) {

        }

        private void releaseDraw() {
            fillRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK);
            fillRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, WHITE);

            drawRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, BLACK); //BLack
            drawShadowRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK); //Black

            drawString(this.btnText, btnTextX + 2, btnTextY + 2, TEXT_SMALL, BLACK);
        }

        private void pressDraw(){

            //fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 3, BLACK);
            //fillRoundRectangle(btnX , btnY , btnWidth - 3, btnHeight - 3, 3, WHITE);

            drawRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK); // black
            //drawRoundRectangle(btnX , btnY , btnWidth -3, btnHeight -3, 3, BLACK);  // black

//            drawString(this.btnText, btnTextX + pixelX, btnTextY + pixelX, TEXT_SMALL, BLACK);
            drawString(this.btnText, btnTextX, btnTextY, TEXT_SMALL, BLACK);
        }

    }


    // fonts
    private class font{
        protected int font_width=0,font_height=0,first_char = 0,char_Count=0;
        protected int[][] font = new int[][]{};

        public int getChar_Count() {
            return char_Count;
        }

        public int getFirst_char() {
            return first_char;
        }

        public int getFont_height() {
            return font_height;
        }

        public int getFont_width() {
            return font_width;
        }

        public int[][] getFont() {
            return font;
        }
    }

    public class font1 extends font{

        public font1(){
            font_width=8;
            font_height=15;
            first_char = 32;
            char_Count=95;
            font = new int[][]{
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},	// ' '
                    {0x00,0x00,0x18,0x3C,0x3C,0x3C,0x18,0x18,0x18,0x00,0x18,0x18,0x00,0x00,0x00,0x00},	// '!'
                    {0x00,0x63,0x63,0x63,0x22,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},	// '"'
                    {0x00,0x00,0x00,0x36,0x36,0x7F,0x36,0x36,0x36,0x7F,0x36,0x36,0x00,0x00,0x00,0x00},	// '#'
                    {0x0C,0x0C,0x3E,0x63,0x61,0x60,0x3E,0x03,0x03,0x43,0x63,0x3E,0x0C,0x0C,0x00,0x00},	// '$'
                    {0x00,0x00,0x00,0x00,0x00,0x61,0x63,0x06,0x0C,0x18,0x33,0x63,0x00,0x00,0x00,0x00},	// '%'
                    {0x00,0x00,0x00,0x1C,0x36,0x36,0x1C,0x3B,0x6E,0x66,0x66,0x3B,0x00,0x00,0x00,0x00},
                    {0x00,0x30,0x30,0x30,0x60,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x0C,0x18,0x18,0x30,0x30,0x30,0x30,0x18,0x18,0x0C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x18,0x0C,0x0C,0x06,0x06,0x06,0x06,0x0C,0x0C,0x18,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x42,0x66,0x3C,0xFF,0x3C,0x66,0x42,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x18,0x18,0x18,0xFF,0x18,0x18,0x18,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0x18,0x18,0x30,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0x18,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x01,0x03,0x07,0x0E,0x1C,0x38,0x70,0xE0,0xC0,0x80,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x63,0x6B,0x6B,0x63,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},	// '0'
                    {0x00,0x00,0x0C,0x1C,0x3C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x3F,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x03,0x06,0x0C,0x18,0x30,0x61,0x63,0x7F,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x03,0x03,0x1E,0x03,0x03,0x03,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x06,0x0E,0x1E,0x36,0x66,0x66,0x7F,0x06,0x06,0x0F,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7F,0x60,0x60,0x60,0x7E,0x03,0x03,0x63,0x73,0x3E,0x00,0x00,0x00,0x00},	// '5'
                    {0x00,0x00,0x1C,0x30,0x60,0x60,0x7E,0x63,0x63,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7F,0x63,0x03,0x06,0x06,0x0C,0x0C,0x18,0x18,0x18,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x63,0x3E,0x63,0x63,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x63,0x63,0x3F,0x03,0x03,0x06,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x18,0x18,0x00,0x00,0x00,0x18,0x18,0x00,0x00,0x00,0x00},	// ':'
                    {0x00,0x00,0x00,0x00,0x00,0x18,0x18,0x00,0x00,0x00,0x18,0x18,0x18,0x30,0x00,0x00},
                    {0x00,0x00,0x00,0x06,0x0C,0x18,0x30,0x60,0x30,0x18,0x0C,0x06,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x7E,0x00,0x00,0x7E,0x00,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x60,0x30,0x18,0x0C,0x06,0x0C,0x18,0x30,0x60,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x06,0x0C,0x0C,0x0C,0x00,0x0C,0x0C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x6F,0x6B,0x6B,0x6E,0x60,0x60,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x08,0x1C,0x36,0x63,0x63,0x63,0x7F,0x63,0x63,0x63,0x00,0x00,0x00,0x00},	// 'A'
                    {0x00,0x00,0x7E,0x33,0x33,0x33,0x3E,0x33,0x33,0x33,0x33,0x7E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x1E,0x33,0x61,0x60,0x60,0x60,0x60,0x61,0x33,0x1E,0x00,0x00,0x00,0x00},	// 'C'
                    {0x00,0x00,0x7C,0x36,0x33,0x33,0x33,0x33,0x33,0x33,0x36,0x7C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7F,0x33,0x31,0x34,0x3C,0x34,0x30,0x31,0x33,0x7F,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7F,0x33,0x31,0x34,0x3C,0x34,0x30,0x30,0x30,0x78,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x1E,0x33,0x61,0x60,0x60,0x6F,0x63,0x63,0x37,0x1D,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x63,0x63,0x63,0x7F,0x63,0x63,0x63,0x63,0x63,0x00,0x00,0x00,0x00},	// 'H'
                    {0x00,0x00,0x3C,0x18,0x18,0x18,0x18,0x18,0x18,0x18,0x18,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x0F,0x06,0x06,0x06,0x06,0x06,0x06,0x66,0x66,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x73,0x33,0x36,0x36,0x3C,0x36,0x36,0x33,0x33,0x73,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x78,0x30,0x30,0x30,0x30,0x30,0x30,0x31,0x33,0x7F,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x77,0x7F,0x6B,0x63,0x63,0x63,0x63,0x63,0x63,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x63,0x73,0x7B,0x7F,0x6F,0x67,0x63,0x63,0x63,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x1C,0x36,0x63,0x63,0x63,0x63,0x63,0x63,0x36,0x1C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7E,0x33,0x33,0x33,0x3E,0x30,0x30,0x30,0x30,0x78,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x63,0x63,0x63,0x63,0x6B,0x6F,0x3E,0x06,0x07,0x00,0x00},
                    {0x00,0x00,0x7E,0x33,0x33,0x33,0x3E,0x36,0x36,0x33,0x33,0x73,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x3E,0x63,0x63,0x30,0x1C,0x06,0x03,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0xFF,0xDB,0x99,0x18,0x18,0x18,0x18,0x18,0x18,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x63,0x63,0x63,0x63,0x63,0x63,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x63,0x63,0x63,0x63,0x63,0x63,0x36,0x1C,0x08,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x63,0x63,0x63,0x63,0x63,0x6B,0x6B,0x7F,0x36,0x36,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0xC3,0xC3,0x66,0x3C,0x18,0x18,0x3C,0x66,0xC3,0xC3,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0xC3,0xC3,0xC3,0x66,0x3C,0x18,0x18,0x18,0x18,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x7F,0x63,0x43,0x06,0x0C,0x18,0x30,0x61,0x63,0x7F,0x00,0x00,0x00,0x00},	// 'Z'
                    {0x00,0x00,0x3C,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x3C,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x80,0xC0,0xE0,0x70,0x38,0x1C,0x0E,0x07,0x03,0x01,0x00,0x00,0x00,0x00},	// '\'
                    {0x00,0x00,0x3C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x3C,0x00,0x00,0x00,0x00},
                    {0x08,0x1C,0x36,0x63,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x00,0x00,0x00},	// '^'
                    {0x18,0x18,0x0C,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3C,0x46,0x06,0x3E,0x66,0x66,0x3B,0x00,0x00,0x00,0x00},	// '_'
                    {0x00,0x00,0x70,0x30,0x30,0x3C,0x36,0x33,0x33,0x33,0x33,0x6E,0x00,0x00,0x00,0x00},	// '`'
                    {0x00,0x00,0x00,0x00,0x00,0x3E,0x63,0x60,0x60,0x60,0x63,0x3E,0x00,0x00,0x00,0x00},	// 'a'
                    {0x00,0x00,0x0E,0x06,0x06,0x1E,0x36,0x66,0x66,0x66,0x66,0x3B,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3E,0x63,0x63,0x7E,0x60,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x1C,0x36,0x32,0x30,0x7C,0x30,0x30,0x30,0x30,0x78,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3B,0x66,0x66,0x66,0x66,0x3E,0x06,0x66,0x3C,0x00,0x00},
                    {0x00,0x00,0x70,0x30,0x30,0x36,0x3B,0x33,0x33,0x33,0x33,0x73,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x0C,0x0C,0x00,0x1C,0x0C,0x0C,0x0C,0x0C,0x0C,0x1E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x06,0x06,0x00,0x0E,0x06,0x06,0x06,0x06,0x06,0x66,0x66,0x3C,0x00,0x00},
                    {0x00,0x00,0x70,0x30,0x30,0x33,0x33,0x36,0x3C,0x36,0x33,0x73,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x1C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x0C,0x1E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x6E,0x7F,0x6B,0x6B,0x6B,0x6B,0x6B,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x6E,0x33,0x33,0x33,0x33,0x33,0x33,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3E,0x63,0x63,0x63,0x63,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x6E,0x33,0x33,0x33,0x33,0x3E,0x30,0x30,0x78,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3B,0x66,0x66,0x66,0x66,0x3E,0x06,0x06,0x0F,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x6E,0x3B,0x33,0x30,0x30,0x30,0x78,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x3E,0x63,0x38,0x0E,0x03,0x63,0x3E,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x08,0x18,0x18,0x7E,0x18,0x18,0x18,0x18,0x1B,0x0E,0x00,0x00,0x00,0x00},	// 't'
                    {0x00,0x00,0x00,0x00,0x00,0x66,0x66,0x66,0x66,0x66,0x66,0x3B,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x63,0x63,0x36,0x36,0x1C,0x1C,0x08,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x63,0x63,0x63,0x6B,0x6B,0x7F,0x36,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x63,0x36,0x1C,0x1C,0x1C,0x36,0x63,0x00,0x00,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x63,0x63,0x63,0x63,0x63,0x3F,0x03,0x06,0x3C,0x00,0x00},
                    {0x00,0x00,0x00,0x00,0x00,0x7F,0x66,0x0C,0x18,0x30,0x63,0x7F,0x00,0x00,0x00,0x00},	// 'z'
                    {0x00,0x00,0x0E,0x18,0x18,0x18,0x70,0x18,0x18,0x18,0x18,0x0E,0x00,0x00,0x00,0x00},	// '{'
                    {0x00,0x00,0x18,0x18,0x18,0x18,0x18,0x00,0x18,0x18,0x18,0x18,0x18,0x00,0x00,0x00},	// '|'
                    {0x00,0x00,0x70,0x18,0x18,0x18,0x0E,0x18,0x18,0x18,0x18,0x70,0x00,0x00,0x00,0x00},	// '}'
                    {0x00,0x00,0x3B,0x6E,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00}	// '~'
            };
        }

    }

}
