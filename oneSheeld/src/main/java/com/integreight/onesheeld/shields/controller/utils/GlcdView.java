package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.integreight.onesheeld.shields.controller.utils.glcd.*;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdView extends View implements OnTouchListener {

    Context context;
    private Canvas canvas;
    int background = 0;
    Paint paint;
    int glcdWidth=256,glcdHeight=128;
    SparseArray<SparseArray<Integer>> dots, touchs;
    SparseArray<Shape> shapes;
    SparseArray<RadioGroup> radioGroups;
    public int BLACK= Color.parseColor("#11443d"),WHITE=Color.parseColor("#338f45");
    float pixelX,pixelY,originX,originY,width,height,ascpectRatio;

    public static final int TEXT_SMALL=1,TEXT_MEDUIM=3,TEXT_LARGE=5;
    public static final int FONT_ARIEL_REGULAR=0,FONT_ARIEL_BLACK=1,FONT_ARIEL_ITALIC=3,FONT_COMICSANS=4,FONT_SERIF=5;

    private boolean isInt=false;
    private int currentPressedKey = 0;


    public static final byte SHAPE_BUTTON=0x08,SHAPE_CHECKBOX=0x0A,SHAPE_SLIDER=0x0B,SHAPE_RADIOBUTTON=0x09;
    public static final byte STATE_PRESSED=0x01,STATE_RELEASED=0x00,STATE_TOUCHED=0x02;

    public static interface GlcdViewEventListener{
        void sendTouch(byte shapeType,int key,byte state);
        void sendTouch(byte shapeType,int key,byte state,int value);
    }

    GlcdViewEventListener glcdViewEventListener;

    public GlcdViewEventListener getGlcdViewEventListener() {
        return glcdViewEventListener;
    }

    public void setGlcdViewEventListener(GlcdViewEventListener glcdViewEventListener) {
        this.glcdViewEventListener = glcdViewEventListener;
    }

    public GlcdView(Context context) {
        super(context);
        this.context = context;
        paint = new Paint();
        background = WHITE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        ascpectRatio = glcdWidth/glcdHeight;
        width = canvas.getHeight();
        height = canvas.getHeight()/ascpectRatio;
        originY = (canvas.getWidth() - height) / 2;
        originX = (canvas.getHeight() - width) / 2;
        pixelX = height/glcdHeight;
        pixelY = width/glcdWidth;

        //   draw Background
        paint.setColor(background);
        canvas.drawRect(originY, originX, originY + height, originX + width, paint);


        //------------------------------------
        //----------- display data ----------------
//        Log.d("GLCD", " canvas: "+String.valueOf(canvas.getWidth()) + "/" + String.valueOf(canvas.getHeight()));
//        Log.d("GLCD", " this: "+String.valueOf(this.width) + "/" + String.valueOf(this.height));
//        Log.d("GLCD", " glcd: "+String.valueOf(this.glcdWidth) + "/" + String.valueOf(this.glcdHeight));
//        Log.d("GLCD", " origin: "+String.valueOf(this.originX) + "," + String.valueOf(this.originY));
//        Log.d("GLCD", " pixel: "+String.valueOf(pixelX)+"/"+String.valueOf(pixelY));
//        Log.d("GLCD", " aspectRatio: " + String.valueOf(ascpectRatio));
        //------------------------------------------
        //------------------------------------
        if (isInt == false) {
            isInt = true;

            List<Integer> params;
            List<Boolean> premissions;
            params = new ArrayList<>();
            params.add(WHITE);
            premissions= new ArrayList<>();
            premissions.add(true);
            premissions.add(true);
            premissions.add(true);
            premissions.add(true);
            doOrder(ORDER_CLEAR, params, premissions);

            setOnTouchListener(this);
//            Shapes Test

//            setPixel(0, 0, BLACK);
//            setPixel(0, 127, BLACK);
//            setPixel(128,64,BLACK );
//            setPixel(255,0,BLACK);
//            setPixel(255,127,BLACK);

//            Line line = new Line(0,0,256,128);
//            shapes.append(shapes.size()+1,line);

//            Rectangle rectangle = new Rectangle(0,0,50,50,true);
//            shapes.append(shapes.size()+1,rectangle);

//            RoundRectangle roundRectangle = new RoundRectangle(0,0,50,50,5,true);
//            shapes.append(shapes.size()+1,roundRectangle);

//            Circle circle = new Circle(100,50,50,true);
//            shapes.append(shapes.size()+1,circle);

//            Ellipse ellipse = new Ellipse(50,100,50,100,true);
//            shapes.append(shapes.size()+1,ellipse);

//            TxtLabel txtLabel = new TxtLabel("Hello World!",0,0,TEXT_SMALL,FONT_ARIEL_REGULAR);
//            TxtLabel txtLabel = new TxtLabel("Hello World!",0,0,TEXT_MEDUIM,FONT_ARIEL_REGULAR);
//            TxtLabel txtLabel = new TxtLabel("Hello World!",0,0,TEXT_LARGE,FONT_ARIEL_REGULAR);
//            shapes.append(shapes.size()+1,txtLabel);

//            button btn = new button(0, 0, 100, 30, "Hello");
//            int btnKey = shapes.size()+1;
//            shapes.append(btnKey, btn);
//            btn.setBtnTouchId(btnKey);
//
//            button2D btn2 = new button2D(0, 70, 100, 30, "Hello");
//            int btnKey2 = shapes.size()+1;
//            shapes.append(btnKey2, btn2);
//            btn2.setBtnTouchId(btnKey2);
//
//            radioButton btn1 = new radioButton(5,5,5,"Male");
//            int btn1Key = shapes.size()+1;
//            shapes.append(btn1Key,btn1);
//            btn1.setBtnTouchId(btn1Key);
//
//            radioButton btn2 = new radioButton(5,50,5,"Female");
//            int btn2Key = shapes.size()+1;
//            shapes.append(btn2Key,btn2);
//            btn2.setBtnTouchId(btn2Key);
//
//            RadioGroup radioGroup = new RadioGroup();
//            radioGroup.add(btn1);
//            radioGroup.add(btn2);

//            checkBox btn = new checkBox(5,5,12,"Check box");
//            int btnKey = shapes.size()+1;
//            shapes.append(btnKey,btn);
//            btn.setBtnTouchId(btnKey);

//            HorSlider btn = new HorSlider(15,15,100,10);
//            int btnKey = shapes.size()+1;
//            shapes.append(btnKey,btn);
//            btn.setBtnTouchId(btnKey);

//            ProgressBar progressBar = new ProgressBar(0,0,100,20);
//            shapes.append(shapes.size()+1,progressBar);
//            ((ProgressBar) shapes.get(1)).setCurrentValue(50);

//            AnalogGauge analogGauge = new AnalogGauge(50,50,30,0,100);
//            shapes.append(shapes.size()+1,analogGauge);
//            ((AnalogGauge) shapes.get(1)).currentValue = 50;

        }

        //------------------------------------

        for (int shapesCount=0;shapesCount<shapes.size();shapesCount++){
            shapes.valueAt(shapesCount).draw(this);
        }

        List<Integer> params = new ArrayList<>();
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(null);
        premissions.add(null);
        premissions.add(null);
        premissions.add(true);
        doOrder(ORDER_DRAW_DOTS, params, premissions);

        invalidate();
    }

    public static final int ORDER_SETDOT=0,ORDER_SETTOUCH=1,ORDER_CLEAR=2, ORDER_DRAW_DOTS =3,ORDER_HANDLETOUCH=4,ORDER_APPLYTOUCH=5;
    boolean do4Dots= false,do4Touchs = false,do4Shapes=false,doInvalidate= false;
    boolean sendFrame = false;

    public synchronized boolean doOrder(int order,List<Integer> params,List<Boolean> premissons){
        //premissons
        if (premissons.size() < 4)
            return false;

        if (premissons.get(0) != null)  do4Dots = premissons.get(0); else do4Dots = false;
        if (premissons.get(1) != null)
            do4Touchs = premissons.get(1);
        else
            do4Touchs = false;
        if (premissons.get(2) != null)  do4Shapes = premissons.get(2); else do4Shapes = false;
        if (premissons.get(3) != null)  doInvalidate = premissons.get(3); else doInvalidate = false;


        //orders
        Integer BgColor=this.background,key=0,action=0,touchId=0,startX=0,startY=0,finalX = 0,finalY=0;

        switch (order){
            case ORDER_SETDOT:
                if (params.size() < 3)
                    return false;
                // x = params.get(0);
                // y = params.get(1);
                // color = params.get(2);

                if (params.get(0) < dots.size() && params.get(0) >= 0)
                    if (params.get(1) < dots.get(params.get(0)).size() && params.get(1) >= 0)
                        dots.get(params.get(0)).setValueAt(params.get(1),params.get(2));
                break;
            case ORDER_SETTOUCH:
                if (params.size() < 3)
                    return false;
                // x = params.get(0);
                // y = params.get(1);
                // touchId = params.get(2);
                if (params.get(0) < touchs.size() && params.get(0) >= 0)
                    if (params.get(1) < touchs.get(params.get(0)).size() && params.get(1) >= 0)
                        touchs.get(params.get(0)).setValueAt(params.get(1),params.get(2));
                break;
            case ORDER_CLEAR:
                if (params.size() < 0)
                    return false;
                BgColor=this.background;
                startX=0;
                startY=0;
                finalX = 0;
                finalY=0;

                if (params.size() == 1){
                    if(do4Dots) dots = new SparseArray<>();
                    if(do4Touchs) touchs = new SparseArray<>();
                    if(do4Shapes) shapes = new SparseArray<>();
                    radioGroups = new SparseArray<>();
                    for (int x=0;x<glcdWidth;x++){
                        SparseArray<Integer> tempDots = new SparseArray<>();
                        SparseArray<Integer> tempTouchs = new SparseArray<>();
                        for (int y=0;y<glcdHeight;y++){
                            tempDots.append(y, this.background);
                            tempTouchs.append(y, null);
                        }
                        if (do4Dots) dots.append(x, tempDots);
                        if (do4Touchs) touchs.append(x, tempTouchs);
                    }

                    BgColor = params.get(0);
                    startX = 0;
                    startY = 0;
                    finalX = glcdWidth;
                    finalY = glcdHeight;
                }else if (params.size() > 4){
                    BgColor = params.get(0);
                    startX = params.get(1);
                    if (startX < 0)
                        startX = 0;
                    else if (startX > glcdWidth)
                        startX = glcdWidth-1;

                    startY = params.get(2);
                    if (startY < 0)
                        startY = 0;
                    else if (startY > glcdHeight)
                        startY = glcdHeight-1;

                    finalX = startX+params.get(3);
                    if (finalX < 0)
                        finalX = 0;
                    else if (finalX > glcdWidth)
                        finalX = glcdWidth-1;

                    finalY = startY+params.get(4);
                    if (finalY < 0)
                        finalY = 0;
                    else if (finalY > glcdHeight)
                        finalY = glcdHeight-1;

                    if (dots.size() > 0) {
                        for (int x = startX; x < finalX; x++) {
                            if (dots.get(x).size() > 0) {
                                for (int y = startY; y < finalY; y++) {
                                    if (do4Dots)
                                        dots.get(x).setValueAt(y, BgColor);
                                    if (do4Touchs)
                                        touchs.get(x).setValueAt(y, null);
                                }
                            }
                        }
                    }
                }else{
                    return false;
                }

                break;
            case ORDER_DRAW_DOTS:
                if (params.size() < 0)
                    return false;
                startX=0;
                startY=0;
                finalX = 0;
                finalY=0;

                if (params.size() > 4){
                    startX = params.get(0);
                    if (startX < 0)
                        startX = 0;
                    else if (startX > glcdWidth)
                        startX = glcdWidth-1;

                    startY = params.get(1);
                    if (startY < 0)
                        startY = 0;
                    else if (startY > glcdHeight)
                        startY = glcdHeight-1;

                    finalX = params.get(2);
                    if (finalX < 0)
                        finalX = 0;
                    else if (finalX > glcdWidth)
                        finalX = glcdWidth-1;

                    finalY = params.get(3);
                    if (finalY < 0)
                        finalY = 0;
                    else if (finalY > glcdHeight)
                        finalY = glcdHeight-1;

                }else{
                    startX = 0;
                    startY = 0;
                    finalX = glcdWidth;
                    finalY = glcdHeight;
                }

                paint = new Paint();
                paint.setStyle(Paint.Style.FILL);

                for (int x=startX;x<finalX;x++){
                    for (int y=startY;y<finalY;y++){
                        if (x < dots.size()) {
                            if (y < dots.get(x).size()) {
                                if (dots.get(x).get(y) != background) {
                                    paint.setColor(dots.get(x).get(y));
                                    float left = (originY + height) - ((y + 1) * pixelY);
                                    float top = (x * pixelX) + originX;
                                    float right = (originY + height) - (y * pixelY);
                                    float bottom = ((x + 1) * pixelX) + originX;
                                    canvas.drawRect(left, top, right, bottom, paint);
                                }
                            }
                        }
                    }
                }

                break;
            case ORDER_HANDLETOUCH:
                if (params.size() < 2)
                    return false;

                action = params.get(0);
                startX = params.get(1);
                startY = params.get(2);

                if (touchs.size() > 0) {
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // press
                            if (currentPressedKey != 0)
                                if (shapes.indexOfKey(currentPressedKey) != -1)
                                    shapes.get(currentPressedKey).setIsPressed(false);
                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setIsPressed(true);
                                currentPressedKey = key;

                                if (glcdViewEventListener != null && sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Button.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_BUTTON, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(CheckBox.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_CHECKBOX, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(RadioButton.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_RADIOBUTTON, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_SLIDER, key, STATE_RELEASED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }

                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            //release
                            if (currentPressedKey != 0)
                                if (shapes.indexOfKey(currentPressedKey) != -1)
                                    shapes.get(currentPressedKey).setIsPressed(false);

                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setIsPressed(false);

                                if (glcdViewEventListener != null && sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Button.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_BUTTON, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(CheckBox.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_CHECKBOX, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(RadioButton.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_RADIOBUTTON, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_SLIDER, key, STATE_RELEASED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // touch
                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setTouched(startX, startY);
                                if (glcdViewEventListener != null && sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        glcdViewEventListener.sendTouch(SHAPE_SLIDER, key, STATE_RELEASED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }
                            }
                            break;
                    }
                }
                break;
            case ORDER_APPLYTOUCH:
                if (params.size() < 0)
                    return false;
                touchId=0;
                startX=0;
                startY=0;
                finalX = 0;
                finalY=0;
                if (params.size() > 4){
                    startX = params.get(0);
                    if (startX < 0)
                        startX = 0;
                    else if (startX > glcdWidth)
                        startX = glcdWidth-1;

                    startY = params.get(1);
                    if (startY < 0)
                        startY = 0;
                    else if (startY > glcdHeight)
                        startY = glcdHeight-1;

                    finalX = params.get(2);
                    if (finalX < 0)
                        finalX = 0;
                    else if (finalX > glcdWidth)
                        finalX = glcdWidth-1;

                    finalY = params.get(3);
                    if (finalY < 0)
                        finalY = 0;
                    else if (finalY > glcdHeight)
                        finalY = glcdHeight-1;

                    touchId = params.get(4);
                }else{
                    return false;
                }
                if (touchs.size() > 0) {
                    for (int x = startX; x < finalX; x++) {
                        if (touchs.get(x).size() > 0) {
                            for (int y = startY; y < finalY; y++) {
                                touchs.get(x).setValueAt(y, touchId);
                            }
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

//    public void clear(int background){
//        paint = new Paint();
//        this.background = background;
//        dots = new SparseArray<>();
//        touchs = new SparseArray<>();
//        shapes = new SparseArray<>();
//        radioGroups = new SparseArray<>();
//        for (int x=0;x<glcdWidth;x++){
//            SparseArray<Integer> tempDots = new SparseArray<>();
//            SparseArray<Integer> tempTouchs = new SparseArray<>();
//            for (int y=0;y<glcdHeight;y++){
//                tempDots.append(y, this.background);
//                tempTouchs.append(y, null);
//            }
//            dots.append(x, tempDots);
//            touchs.append(x, tempTouchs);
//        }
//    }

//    public void clear(int background,boolean ClearShapes){
//        paint = new Paint();
//        this.background = background;
//        dots = new SparseArray<>();
//        touchs = new SparseArray<>();
//        if(ClearShapes) shapes = new SparseArray<>();
//        for (int x=0;x<glcdWidth;x++){
//            SparseArray<Integer> tempDots = new SparseArray<>();
//            SparseArray<Integer> tempTouchs = new SparseArray<>();
//            for (int y=0;y<glcdHeight;y++){
//                tempDots.append(y,this.background);
//                tempTouchs.append(y,null);
//            }
//            dots.append(x,tempDots);
//            touchs.append(x,tempTouchs);
//        }
//    }

//    public void clear(int background,int x,int y,int width,int height,boolean clearGraphics,boolean clearTouch){
//        paint = new Paint();
//        paint.setColor(background);
//        paint.setStyle(Paint.Style.FILL);
//        canvas.drawRect(originY + height - (y * pixelY), (x * pixelX) + originX, originY + height - ((y + height) * pixelY), ((x + width) * pixelX) + originX, paint);
//        for (int X=x;X<width+x;X++){
//            for (int Y=y;Y<height+y;Y++){
//                if (clearGraphics) dots.get(x).setValueAt(y,this.background);
//                if (clearTouch) touchs.get(x).setValueAt(y,null);
//            }
//        }
//    }

//    public void clear(int background,int x,int y,int width,int height){
//        clear(background, x, y, width, height, true, true);
//    }

//    private void refresh(boolean doInvalidation){
//        paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        // draw pixels
//        for (int x = 0;x<glcdWidth;x++){
//            for (int y=0;y<glcdHeight;y++){
//                if (x < dots.size()) {
//                    if (y < dots.get(x).size()) {
//                        if (dots.get(x).get(y) != background) {
//                            paint.setColor(dots.get(x).get(y));
//                            float left = (originY + height) - ((y + 1) * pixelY);
//                            float top = (x * pixelX) + originX;
//                            float right = (originY + height) - (y * pixelY);
//                            float bottom = ((x + 1) * pixelX) + originX;
//                            canvas.drawRect(left, top, right, bottom, paint);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (doInvalidation) {
//            invalidate();
//        }
//    }

//    private void refresh(boolean doInvalidation,int X,int Y,int width,int height){
//        paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(background);
//        canvas.drawRect(originY + height - (Y * pixelY), (X * pixelX) + originX, originY + height - ((Y + height) * pixelY), ((X + width) * pixelX) + originX, paint);
//        // draw pixels
//        for (int x = 0;x<X;x++){
//            for (int y=0;y<Y;y++){
//                if (dots.get(x).get(y) != background) {
//                    paint.setColor(dots.get(x).get(y));
//                    float left = (originY+height)-((y+1)*pixelY);
//                    float top = (x*pixelX) + originX;
//                    float right = (originY+height)-(y*pixelY);
//                    float bottom = ((x+1)*pixelX) + originX;
//                    canvas.drawRect(left,top,right,bottom,paint);
//                }
//            }
//        }
//
//        if (doInvalidation) {
//            invalidate();
//            requestLayout();
//        }
//    }

    public float absDiff(float a, float b){
        if (a>b)
            return a-b;
        return b-a;
    }

//    private void setPixel(int x, int y, int color){
//        if (x < dots.size() && x >= 0)
//            if (y < dots.get(x).size() && y >= 0)
//                dots.get(x).setValueAt(y,color);
//    }

//    private void setTouch(int x, int y, int touchId){
//        if (x < touchs.size() && x >= 0)
//            if (y < touchs.get(x).size() && y >= 0)
//                touchs.get(x).setValueAt(y,touchId);
//    }

//    public Integer getTouch(int x, int y){
//        if (x < touchs.size() && x >= 0)
//            if (y < touchs.get(x).size() && y >= 0){
//                return touchs.get(x).get(y);
//            }
//        return null;
//    }

    public void addToShapes(Shape shape,int key){
        shapes.append(key,shape);
    }

    public Shape getFromShapes(int key){
        if (shapes != null)
            if (shapes.indexOfKey(key) > -1)
                if (shapes.size() > 0)
                    return shapes.get(key);
            return null;
    }

    public void addToRadioGroups(RadioGroup group,int key){
        radioGroups.append(key, group);
    }

    public RadioGroup getFromRadioGroups(int key){
        if (radioGroups.indexOfKey(key) <= -1)
            addToRadioGroups(new RadioGroup(),key);
        return radioGroups.get(key);
    }
//    public class point implements Shape{
//        float x,y;
//        boolean visiblity = true;
//
//        public point(float x,float y){
//            this.x = x;
//            this.y = y;
//        }
//        @Override
//        public void draw() {
//            if (visiblity)
//                setPixel(0,0,BLACK);
//        }
//
//        @Override
//        public void setVisibility(boolean visibility) {
//            this.visiblity = visibility;
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }



//    public class Line implements Shape{
//        float x1,y1,x2,y2;
//        boolean visibility = true;
//
//        public Line(float x1,float y1,float x2,float y2){
//            this.x1 = x1;
//            this.x2 = x2;
//            this.y1 = y1;
//            this.y2 = y2;
//        }
//
//        public void move(float newX1,float newY1){
//            setPoint2(x2+(newX1-x1),y2+(newY1-y1));
//            setPoint1(newX1,newY1);
//        }
//
//        public void setPoint1(float newX1,float newY1){
//            this.x1 = newX1;
//            this.y1 = newY1;
//        }
//
//        public void setPoint2(float newX2,float newY2){
//            this.x2 = newX2;
//            this.y2 = newY2;
//        }
//
//        @Override
//        public void draw() {
//            if (visibility)
//                drawLine(x1,y1,x2,y2,BLACK);
//        }
//
//        @Override
//        public void setVisibility(boolean visibility) {
//            this.visibility = visibility;
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    public void drawLine(float x1,float y1,float x2,float y2,int color) {
        float deltaX, deltaY, x, y;
        boolean steep;
        float error, yStep;

        if (x1 > glcdWidth) x1 = glcdWidth;
        if (x2 > glcdWidth) x2 = glcdWidth;
        if (y1 > glcdHeight) y1 = glcdHeight;
        if (y2 > glcdHeight) y1 = glcdHeight;

        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        if (y1 < 0) y1 = 0;
        if (y2 < 0) y1 = 0;

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
            if (steep) {
//                setPixel((int) y, (int) x, color);
                List<Integer> params = new ArrayList<>();
                params.add((int) y);
                params.add((int) x);
                params.add(color);
                List<Boolean> premissions= new ArrayList<>();
                premissions.add(true);
                premissions.add(null);
                premissions.add(null);
                premissions.add(null);
                doOrder(ORDER_SETDOT, params, premissions);
            }else {
//                setPixel((int) x, (int) y, color);
                List<Integer> params = new ArrayList<>();
                params.add((int) x);
                params.add((int) y);
                params.add(color);
                List<Boolean> premissions= new ArrayList<>();
                premissions.add(true);
                premissions.add(null);
                premissions.add(null);
                premissions.add(null);
                doOrder(ORDER_SETDOT, params, premissions);
            }
            error -= deltaY;
            if (error < 0) {
                y = y + yStep;
                error += deltaX;
            }
        }
    }

//    public class Rectangle implements Shape{
//        float x,y,width,height;
//        boolean isFill;
//
//        public Rectangle(float x,float y,float width,float height,boolean isFill){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.isFill = isFill;
//        }
//
//        public void Move(float newX,float newY){
//            this.x = newX;
//            this.y = newY;
//        }
//
//        @Override
//        public void draw() {
//            if (isFill)
//                fillRectangle(x, y, width, height, BLACK);
//            else
//                drawRectangle(x, y, width, height, BLACK);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    public void drawRectangle(float x,float y,float width,float height,int color){
        if (x > glcdWidth-1)
            x = glcdWidth-1;
        if (y > glcdHeight-1)
            y = glcdHeight-1;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

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

        if (x > glcdWidth-1)
            x = glcdWidth-1;
        if (y > glcdHeight-1)
            y = glcdHeight-1;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x+width > glcdWidth)
            width = glcdWidth-x-1;
        if (y+height > glcdHeight)
            height = glcdHeight-y-1;

        canvas.drawRect( originY+this.height-((y + height + 1) * pixelY ),x * pixelX + originX,  originY+this.height-(y * pixelY ), (x + width + 1) * pixelX + originX , paint);
    }

//    public class RoundRectangle implements Shape{
//
//        float x,y,width,height,radius;
//        boolean isFill;
//
//        public RoundRectangle(float x,float y,float width,float height,float radius,boolean isFill){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.radius = radius;
//            this.isFill = isFill;
//        }
//
//        @Override
//        public void draw() {
//            if (isFill)
//                fillRoundRectangle(x, y, width, height, radius, BLACK);
//            else
//                drawRoundRectangle(x, y, width, height, radius, BLACK);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    public void drawRoundRectangle(float x,float y,float width,float height,float radius,int color){
        if (x > glcdWidth-1)
            x = glcdWidth-1;
        if (y > glcdHeight-1)
            y = glcdHeight-1;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x+width > glcdWidth)
            width = glcdWidth-x-1;
        if (y+height > glcdHeight)
            height = glcdHeight-y-1;

        if (width < height) {
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

        if (radius > 0 ) {
            float tSwitch;
            float x1 = 0, y1 = radius;
            tSwitch = 3 - 2 * radius;
            while (x1 <= y1) {
                List<Integer> params = new ArrayList<>();
                params.add(null);
                params.add(null);
                params.add(color);
                List<Boolean> premissions= new ArrayList<>();
                premissions.add(true);
                premissions.add(null);
                premissions.add(null);
                premissions.add(null);

//                setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
//                setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);
                params.set(0, (int) (x + radius - x1));
                params.set(1, (int) (y + radius - y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + radius - y1));
                params.set(1,(int) (y + radius - x1));
                doOrder(ORDER_SETDOT, params, premissions);

//                setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
//                setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);
                params.set(0, (int) (x + width - radius + x1));
                params.set(1, (int) (y + radius - y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + width - radius + y1));
                params.set(1, (int) (y + radius - x1));
                doOrder(ORDER_SETDOT, params, premissions);

//                setPixel((int) (x + width - radius + x1), (int) (y + height - radius + y1), color);
//                setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), color);
                params.set(0, (int) (x + width - radius + x1));
                params.set(1, (int) (y + height - radius + y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + width - radius + y1));
                params.set(1,(int) (y + height - radius + x1));
                doOrder(ORDER_SETDOT, params, premissions);


//                setPixel((int) (x + radius - x1), (int) (y + height - radius + y1), color);
//                setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);
                params.set(0, (int) (x + radius - x1));
                params.set(1, (int) (y + height - radius + y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + radius - y1));
                params.set(1,(int) (y + height - radius + x1));
                doOrder(ORDER_SETDOT, params, premissions);

                if (tSwitch < 0)
                    tSwitch += (4 * x1 + 6);
                else {
                    tSwitch += (4 * (x1 - y1) + 10);
                    y1--;
                }
                x1++;
            }
        }
    }

    public void fillRoundRectangle(float x,float y,float width,float height,float radius,int color){
        if (x > glcdWidth-1)
            x = glcdWidth-1;
        if (y > glcdHeight-1)
            y = glcdHeight-1;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x+width > glcdWidth)
            width = glcdWidth-x-1;
        if (y+height > glcdHeight)
            height = glcdHeight-y-1;

        if (width < height) {
            if (radius > (width / 2))
                radius = (width / 2);
        }else {
            if (radius > (height/2))
                radius = (height/2);
        }


        drawRoundRectangle(x, y, width, height, radius, color);
        fillRectangle(x + radius, y, width - 2 * radius, height, color);
        fillRectangle(x, y + radius, width, height - 2 * radius, color);

        if (radius > 0) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(originY + this.height - ((y + radius) * pixelY) - (pixelX / 2), ((x + radius) * pixelX) + originX + (pixelX / 2), (radius * pixelX), paint);
            canvas.drawCircle(originY + this.height - ((y + radius) * pixelY) - (pixelX / 2), ((x + width - radius) * pixelX) + originX + (pixelX / 2), (radius * pixelX), paint);
            canvas.drawCircle(originY + this.height - ((y + height - radius) * pixelY) - (pixelX / 2), ((x + radius) * pixelX) + originX + (pixelX / 2), (radius * pixelX), paint);
            canvas.drawCircle(originY + this.height - ((y + height - radius) * pixelY) - (pixelX / 2), ((x + width - radius) * pixelX) + originX + (pixelX / 2), (radius * pixelX), paint);
        }
    }

    public void drawShadowRoundRectangle(float x, float y, float width, float height, float radius, int color){
        if (x > glcdWidth-1)
            x = glcdWidth-1;
        if (y > glcdHeight-1)
            y = glcdHeight-1;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x+width > glcdWidth)
            width = glcdWidth-x-1;
        if (y+height > glcdHeight)
            height = glcdHeight-y-1;

        if (width < height) {
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

        if (radius > 0) {
            float tSwitch;
            float x1 = 0, y1 = radius;
            tSwitch = 3 - 2 * radius;
            while (x1 <= y1) {
                List<Integer> params = new ArrayList<>();
                params.add(null);
                params.add(null);
                params.add(color);
                List<Boolean> premissions= new ArrayList<>();
                premissions.add(true);
                premissions.add(null);
                premissions.add(null);
                premissions.add(null);

//                setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
//                setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);
                params.set(0, (int) (x + radius - x1));
                params.set(1, (int) (y + radius - y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + radius - y1));
                params.set(1, (int) (y + radius - x1));
                doOrder(ORDER_SETDOT, params, premissions);

//                setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
//                setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);
                params.set(0, (int) (x + width - radius + x1));
                params.set(1, (int) (y + radius - y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + width - radius + y1));
                params.set(1, (int) (y + radius - x1));
                doOrder(ORDER_SETDOT, params, premissions);

//                setPixel((int) (x + width - radius + x1), (int) (y + height - radius + y1), background);
//                setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), background);
                params.set(0, (int) (x + width - radius + x1));
                params.set(1, (int) (y + height - radius + y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + width - radius + y1));
                params.set(1, (int) (y + height - radius + x1));
                doOrder(ORDER_SETDOT, params, premissions);

//                setPixel((int) (x + radius - x1), (int) (y + height - radius + y1), color);
//                setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);
                params.set(0, (int) (x + radius - x1));
                params.set(1, (int) (y + height - radius + y1));
                doOrder(ORDER_SETDOT, params, premissions);
                params.set(0, (int) (x + radius - y1));
                params.set(1,(int) (y + height - radius + x1));
                doOrder(ORDER_SETDOT, params, premissions);

                if (tSwitch < 0)
                    tSwitch += (4 * x1 + 6);
                else {
                    tSwitch += (4 * (x1 - y1) + 10);
                    y1--;
                }
                x1++;
            }
        }
    }
    
//    public class Circle implements Shape{
//        float xCenter,yCenter,radius;
//        boolean isFill;
//
//        public Circle (float xCenter,float yCenter,float radius,boolean isFill){
//            this.xCenter = xCenter;
//            this.yCenter = yCenter;
//            this.radius = radius;
//            this.isFill = isFill;
//        }
//
//        @Override
//        public void draw() {
//            if (isFill)
//                fillCircle(xCenter, yCenter, radius, BLACK);
//            else
//                drawCircle(xCenter, yCenter, radius, BLACK);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    public void drawCircle(float xCenter,float yCenter,float radius,int color){
        drawEllipse(xCenter, yCenter, radius, radius, color);
    }

    public void fillCircle(float xCenter,float yCenter, float radius, int color) {
        drawCircle(xCenter, yCenter, radius, color);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(originY+height - (yCenter * pixelY) - (pixelY / 2),originX + (xCenter * pixelX) + (pixelY / 2), (radius * pixelY), paint);
    }

//    public class Ellipse implements Shape{
//        float xCenter,yCenter,radiusX,radiusY;
//        boolean isFill;
//
//        public Ellipse (float xCenter,float yCenter,float radiusX,float radiusY,boolean isFill){
//            this.xCenter = xCenter;
//            this.yCenter = yCenter;
//            this.radiusX = radiusX;
//            this.radiusY = radiusY;
//            this.isFill = isFill;
//        }
//
//        @Override
//        public void draw() {
//            if (isFill)
//                fillEllipse(xCenter,yCenter,radiusX,radiusY,BLACK);
//            else
//                drawEllipse(xCenter, yCenter, radiusX, radiusY, BLACK);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    public void drawEllipse(float xCenter,float yCenter,float radiusX,float radiusY,int color){
//        if (radiusX == radiusY){
//            drawCircle(xCenter, yCenter, radiusX, color);
//        }else {
            float radiusXSqrt = radiusX * radiusX;
            float radiusYSqrt = radiusY * radiusY;
            float x = 0, y = radiusY;
            float Px = 0, Py = 2 * radiusXSqrt * radiusY;
            drawEllipsePoints(xCenter, yCenter, x, y, color);
            float P = (float) (radiusYSqrt - (radiusXSqrt * radiusY) + (0.25 * radiusXSqrt));
            while (Px < Py) {
                x++;
                Px = Px + 2 * radiusYSqrt;
                if (P < 0) {
                    P = P + radiusYSqrt + Px;
                } else {
                    y--;
                    Py = Py - 2 * radiusXSqrt;
                    P = P + radiusYSqrt + Px - Py;
                }
                drawEllipsePoints(xCenter, yCenter, x, y, color);
            }
            P = (float) (radiusYSqrt * (x + 0.5) * (x + 0.5) + radiusXSqrt * (y - 1) * (y - 1) - radiusXSqrt * radiusYSqrt);
            while (y > 0) {
                y--;
                Py = Py - 2 * radiusXSqrt;
                if (P > 0) {
                    P = P + radiusXSqrt - Py;
                } else {
                    x++;
                    Px = Px + 2 * radiusYSqrt;
                    P = P + radiusXSqrt - Py + Px;
                }
                drawEllipsePoints(xCenter, yCenter, x, y, color);
            }
//        }
    }

    public void fillEllipse(float xCenter,float yCenter,float radiusX,float radiusY,int color){
        if (radiusX == radiusY){
            fillCircle(xCenter,yCenter,radiusX,color);
        }else {
            float radiusXSqrt = radiusX * radiusX;
            float radiusYSqrt = radiusY * radiusY;
            float x = 0, y = radiusY;
            float Px = 0, Py = 2 * radiusXSqrt * radiusY;
            fillEllipsePoints(xCenter, yCenter, x, y, color);
            float P = (float) (radiusYSqrt - (radiusXSqrt * radiusY) + (0.25 * radiusXSqrt));
            while (Px < Py) {
                x++;
                Px = Px + 2 * radiusYSqrt;
                if (P < 0) {
                    P = P + radiusYSqrt + Px;
                } else {
                    y--;
                    Py = Py - 2 * radiusXSqrt;
                    P = P + radiusYSqrt + Px - Py;
                }
                fillEllipsePoints(xCenter, yCenter, x, y, color);
            }
            P = (float) (radiusYSqrt * (x + 0.5) * (x + 0.5) + radiusXSqrt * (y - 1) * (y - 1) - radiusXSqrt * radiusYSqrt);
            while (y > 0) {
                y--;
                Py = Py - 2 * radiusXSqrt;
                if (P > 0) {
                    P = P + radiusXSqrt - Py;
                } else {
                    x++;
                    Px = Px + 2 * radiusYSqrt;
                    P = P + radiusXSqrt - Py + Px;
                }
                fillEllipsePoints(xCenter, yCenter, x, y, color);
            }

            drawEllipse(xCenter, yCenter, radiusX, radiusY, color);
        }
    }

    private void drawEllipsePoints(float xCenter, float yCenter, float x, float y, int color){
        List<Integer> params = new ArrayList<>();
        params.add(null);
        params.add(null);
        params.add(color);
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        premissions.add(null);

//        setPixel((int) (xCenter + x), (int) (yCenter+y),color);
        params.set(0, (int) (xCenter + x));
        params.set(1, (int) (yCenter + y));
        doOrder(ORDER_SETDOT, params, premissions);

//        setPixel((int) (xCenter + x), (int) (yCenter-y),color);
        params.set(0, (int) (xCenter + x));
        params.set(1, (int) (yCenter - y));
        doOrder(ORDER_SETDOT, params, premissions);

//        setPixel((int) (xCenter - x), (int) (yCenter+y),color);
        params.set(0, (int) (xCenter - x));
        params.set(1, (int) (yCenter + y));
        doOrder(ORDER_SETDOT, params, premissions);

//        setPixel((int) (xCenter - x), (int) (yCenter - y), color);
        params.set(0, (int) (xCenter - x));
        params.set(1, (int) (yCenter - y));
        doOrder(ORDER_SETDOT, params, premissions);
    }

    private void fillEllipsePoints(float xCenter, float yCenter, float x, float y, int color){
            fillRectangle(xCenter - x, (glcdHeight - yCenter) - y, x * 2, y * 2, color);
    }

//    public class TxtLabel implements Shape {
//        String text = "";
//        int x,y;
//        int textSize = 0,textFont = 0;
//
//        public TxtLabel(String text,int x,int y,int textSize,int textFont){
//            this.x = x;
//            this.y = y;
//            this.text = text;
//            textSize %= 15;  // max text size is 15
//            this.textSize = textSize;
//            this.textFont = textFont;
//        }
//
//        @Override
//        public void draw() {
//            drawString(text,x,y,textSize,textFont,BLACK);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

    private int drawChar(char c,float x,float y,int textSize,int textFont,int color){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        font mfont;
        switch (textFont){
            case FONT_ARIEL_REGULAR:
                mfont = new ArielRegular();
                break;
            case FONT_ARIEL_BLACK:
                mfont = new ArielBlack();
                break;
            case FONT_ARIEL_ITALIC:
                mfont = new ArielItalic();
                break;
            case FONT_COMICSANS:
                mfont = new ComicSans();
                break;
            case FONT_SERIF:
                mfont = new SerifRegular();
                break;
            default:
                mfont = new ArielRegular();
                break;
        }

        if (c < mfont.getFirst_char() || c > mfont.getFirst_char()+mfont.getChar_Count()){
            return 0;
        }
        c -= mfont.getFirst_char();

        int multiplier = textSize;
        int[] data = mfont.getFont();
        int charInit = 0;
        for (int widhtAdderCount=0;widhtAdderCount<c;widhtAdderCount++){
            charInit += mfont.chars_width[widhtAdderCount]*2;
        }
        ArrayList<Integer> charBytes = new ArrayList<Integer>();
        for (int charBytesCount=0;charBytesCount<mfont.chars_width[c];charBytesCount++){
            charBytes.add(data[charInit + charBytesCount]);
            int ha = data[charInit + charBytesCount];
            for (int i=0;i<8;i++){
                if (((ha >> i ) & (0x01)) == 0x01){
                    if (multiplier == 1){
//                        setPixel((int) (y + charBytesCount), (int) (x + i), color);
                        List<Integer> params = new ArrayList<>();
                        params.add((int) (y + charBytesCount));
                        params.add((int) (x + i));
                        params.add(color);
                        List<Boolean> premissions= new ArrayList<>();
                        premissions.add(true);
                        premissions.add(null);
                        premissions.add(null);
                        premissions.add(null);
                        doOrder(ORDER_SETDOT, params, premissions);
                    }else {
//                        float left = (y + ((charBytesCount+1)*multiplier))*pixelX + originX;
//                        if (left < originX) left = originX;
//                        float top = (x+((i)*multiplier))*pixelY + originY;
//                        if (top < originY) top = originY;
//                        float right = (y + (charBytesCount*multiplier))*pixelX + originX;
//                        if (right > originX+(width)) right = originX+(width);
//                        float bottom = (x+((i+1)*multiplier))*pixelY + originY;
//                        if (bottom > originY+(height)) bottom = originY+(height);

                        float left = (originY+height)-((x+((i+1)*multiplier))*pixelY);
                        if (left < originY) left = originY;
                        float top = ((y + ((charBytesCount)*multiplier))*pixelX) + originX;
                        if (top < originX) top = originX;
                        float right = (originY+height)-((x+((i)*multiplier))*pixelY);
                        if (right > originY+(height)) right = originY+(height);
                        float bottom = ((y + ((charBytesCount+1)*multiplier))*pixelX) + originX;
                        if (bottom > originX+(width)) bottom = originX+(width);

                        canvas.drawRect(left,top,right,bottom,paint);
                     }
                }
            }
            ha = data[charInit + charBytesCount+mfont.chars_width[c]];
            if (mfont.getFont_height() > 8) {
                int k = (7-(mfont.getFont_height()-8-1));
                for (int i = k; i < 8; i++) {
                    if (((ha >> i) & (0x01)) == 0x01) {
                        if (multiplier == 1){
//                            setPixel((int) (y + charBytesCount), (int) (x + i+8-k), color);
                            List<Integer> params = new ArrayList<>();
                            params.add((int) (y + charBytesCount));
                            params.add((int) (x + i+8-k));
                            params.add(color);
                            List<Boolean> premissions= new ArrayList<>();
                            premissions.add(true);
                            premissions.add(null);
                            premissions.add(null);
                            premissions.add(null);
                            doOrder(ORDER_SETDOT, params, premissions);
                        }else {
//                            float left = (y + (charBytesCount*multiplier))*pixelX + originX;
//                            if (left < originX) left = originX;
//                            float top = (x+((i+8-k)*multiplier))*pixelY + originY;
//                            if (top < originY) top = originY;
//                            float right = (y + ((charBytesCount+1)*multiplier))*pixelX + originX;
//                            if (right > originX+(width)) right = originX+(width);
//                            float bottom = (x+((i+8-k+1)*multiplier))*pixelY + originY;
//                            if (bottom > originY+(height)) bottom = originY+(height);

                            float left = (originY+height)-((x+((i+8-k+1)*multiplier))*pixelY);
                            if (left < originY) left = originY;
                            float top = ((y + ((charBytesCount)*multiplier))*pixelX) + originX;
                            if (top < originX) top = originX;
                            float right = (originY+height)-((x+((i+8-k)*multiplier))*pixelY);
                            if (right > originY+(height)) right = originY+(height);
                            float bottom = ((y + ((charBytesCount+1)*multiplier))*pixelX) + originX;
                            if (bottom > originX+(width)) bottom = originX+(width);
                            canvas.drawRect(left,top,right,bottom, paint);
                        }
                    }
                }
            }

        }

        return mfont.chars_width[c];



    }

    public void drawString(String text,float x,float y,int textSize,int textFont,int color){

        if (text != null) {
            int yMargin = 0;
            for (int charCount = 0; charCount < text.length(); charCount++) {
                yMargin += (drawChar(text.charAt(charCount), y, x + yMargin, textSize, textFont, color)+2)*textSize;
                //Log.d("mouso" ,String.valueOf(drawChar(text.charAt(charCount), y, x + (charCount * spacer), textSize, textFont, color)*textSize));
            }
        }
    }

    public int getCharWidth(char c,int textSize,int textFont){
        font mfont;
        switch (textFont){
            case FONT_ARIEL_REGULAR:
                mfont = new ArielRegular();
                break;
            case FONT_ARIEL_BLACK:
                mfont = new ArielBlack();
                break;
            case FONT_ARIEL_ITALIC:
                mfont = new ArielItalic();
                break;
            case FONT_COMICSANS:
                mfont = new ComicSans();
                break;
            case FONT_SERIF:
                mfont = new SerifRegular();
                break;
            default:
                mfont = new ArielRegular();
                break;
        }
        if (c < mfont.getFirst_char() || c > mfont.getFirst_char()+mfont.getChar_Count()){
            return 0;
        }
        c -= mfont.getFirst_char();
        return (mfont.chars_width[c]+2)*textSize;
    }

    public int getStringWidth(String text,int textSize,int textFont){
        int yMargin = 0;
        if (text != null) {
            for (int charCount = 0; charCount < text.length(); charCount++) {
                yMargin += getCharWidth(text.charAt(charCount),textSize,textFont);
            }
        }
        return yMargin;
    }

    public int getMaxCharsInWidth(String text,float width,int textSize,int textFont){
        int yMargin = 0;
        int charCount = 0;
        if (text != null) {
            while (charCount < text.length() && yMargin<width){
                yMargin += getCharWidth(text.charAt(charCount),textSize,textFont);
                charCount++;
            }
        }
        return charCount;
    }

    public int getCharHeight(int textSize,int textFont){
        font mfont;
        switch (textFont){
            case FONT_ARIEL_REGULAR:
                mfont = new ArielRegular();
                break;
            case FONT_ARIEL_BLACK:
                mfont = new ArielBlack();
                break;
            case FONT_ARIEL_ITALIC:
                mfont = new ArielItalic();
                break;
            case FONT_COMICSANS:
                mfont = new ComicSans();
                break;
            case FONT_SERIF:
                mfont = new SerifRegular();
                break;
            default:
                mfont = new ArielRegular();
                break;
        }
        return mfont.getFont_height()*textSize;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getY();
        float y = event.getX();
        int action = event.getAction();
        if (x >= originX && x < originX + width && y >= originY && y < originY + height) {
            x -= originX;
            y = height-(y-originY);
            x /= pixelX;
            y /= pixelY;

            List<Integer> params = new ArrayList<>();
            params.add(action);
            params.add((int) x);
            params.add((int) y);
            List<Boolean> premissions= new ArrayList<>();
            premissions.add(true);
            premissions.add(true);
            premissions.add(null);
            premissions.add(null);
            doOrder(ORDER_HANDLETOUCH, params, premissions);

        }else{
            if (currentPressedKey != 0)
                if (shapes.indexOfKey(currentPressedKey) != -1)
                    shapes.get(currentPressedKey).setIsPressed(false);
        }
        return true;
    }

//    public interface Shape{
//        void draw();
//        void setIsPressed(boolean isPressed);
//        void setTouched(int touchX,int touchY);
//    }
//
//    public interface ButtonShape extends Shape{
//        void draw();
//        void setIsPressed(boolean isPressed);
//        void setTouched(int touchX,int touchY);
//        void setBtnTouchId(int btnTouchId);
//        void applyTouch();
//    }

//    public class RadioGroup{
//        SparseArray<radioButton> radios;
//        public RadioGroup(){
//            radios = new SparseArray<radioButton>();
//        }
//
//        public void add(radioButton radioButton){
//            radios.append(radios.size(),radioButton);
//            radioButton.setRadioGroup(this);
//        }
//
//        public void remove(radioButton radioButton){
//            radios.remove(radios.keyAt(radios.indexOfValue(radioButton)));
//        }
//
//        public void select(radioButton radioButton){
//            reset();
//            radios.get(radios.keyAt(radios.indexOfValue(radioButton))).setSelected(true);
//        }
//
//        public void reset(){
//            for(int radiosCount=0;radiosCount<radios.size();radiosCount++){
//                radios.get(radios.keyAt(radiosCount)).setSelected(false);
//            }
//        }
//    }

//    private class ProgressBar implements Shape{
//
//        float x,y,width,height,start,end,currentValue;
//        int color;
//
//        public ProgressBar(float x,float y,float width,float height){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.color = BLACK;
//            this.start = 0;
//            this.end = 100;
//            this.currentValue = start;
//        }
//
//        public ProgressBar(float x,float y,float width,float height,float value){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.color = BLACK;
//            this.start = 0;
//            this.end = 100;
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.currentValue = value;
//        }
//
//        public ProgressBar(float x,float y,float width,float height,float start,float end){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.color = BLACK;
//            this.start = start;
//            this.end = end;
//            this.currentValue = start;
//        }
//
//        public ProgressBar(float x,float y,float width,float height,float start,float end,float value){
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.color = BLACK;
//            this.start = start;
//            this.end = end;
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.currentValue = value;
//        }
//
//        public void setCurrentValue(float value) {
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.currentValue = value;
//        }
//
//        @Override
//        public void draw() {
//            float progress = (((currentValue-start)*((x+width)-(x+5)))/(end-start))+(x+5);
//
//            drawRoundRectangle(x,y,width,height,5,color);
//            fillRoundRectangle(x,y,progress,height,5,color);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//    }

//    private class AnalogGauge implements Shape{
//        private float xCenter, yCenter,radius,start=0,end=100,currentValue=0,angleStart=2.355f,angleEnd=7.065f;
//        int color;
//        public AnalogGauge(float xCenter,float yCenter,float radius,float start,float end){
//            this.xCenter = xCenter;
//            this.yCenter = yCenter;
//            this.radius = radius;
//            if (start > end){
//                float temp = end;
//                end = start;
//                start = temp;
//            }
//            this.start = start;
//            this.end = end;
//            this.color = BLACK;
//            this.currentValue = 0;
//        }
//
//        public AnalogGauge(float xCenter,float yCenter,float radius,float start,float end,float currentValue){
//            this.xCenter = xCenter;
//            this.yCenter = yCenter;
//            this.radius = radius;
//            if (start > end){
//                float temp = end;
//                end = start;
//                start = temp;
//            }
//            this.start = start;
//            this.end = end;
//            this.color = BLACK;
//            this.currentValue = currentValue;
//        }
//
//        @Override
//        public void draw() {
//            //using Linear Interpolation get the angle corresponding to the given value
//            //http://www.ajdesigner.com/phpinterpolation/linear_interpolation_equation.php
//            float angle = (((currentValue-start)*(angleEnd-angleStart))/(end-start))+angleStart;
//
//            GlcdView.this.fillCircle(xCenter, yCenter, radius, GlcdView.this.background);
//            drawPartOfCircle(radius, color);
//            drawPartOfCircle((float) (radius * 0.8), color);
//            drawPointer((float) (radius * 0.7), angle, color);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//
//        public void setCurrentValue(float value) {
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.currentValue = value;
//        }
//
//        private void drawIndicator(float angleInRadian,int color){
//            float x1 = (float) (xCenter + (radius * 0.8 * Math.cos(angleInRadian))) ,x2=(float) (xCenter + (radius * Math.cos(angleInRadian)));
//            float y1 = (float) (yCenter + (radius * 0.8 * Math.sin(angleInRadian))) ,y2=(float) (yCenter + (radius * Math.sin(angleInRadian)));
//            GlcdView.this.drawLine(x1, y1, x2, y2, color);
//        }
//
//        private void drawPointer(float radius,float angleInRadian,int color){
//            float x1 = xCenter,x2=(float) (xCenter+(radius*Math.cos(angleInRadian)));
//            float y1 = yCenter,y2=(float) (yCenter + (radius * Math.sin(angleInRadian)));
//            GlcdView.this.drawLine(x1, y1, x2, y2, color);
//        }
//
//        private void drawPartOfCircle(float radius,int color){
//            float x = xCenter - radius;
//            float y = yCenter - radius;
//            float width = 2*radius,height = 2*radius;
//
//            if (width >= height) {
//                if (radius > (width / 2))
//                    radius = (width / 2);
//            }else {
//                if (radius > (height/2))
//                    radius = (height/2);
//            }
//
//            float tSwitch;
//            float x1=0,y1=radius;
//            tSwitch = 3-2*radius;
//            while (x1<=y1){
//                GlcdView.this.setPixel((int) (x + radius - x1), (int) (y + radius - y1), color);
//                GlcdView.this.setPixel((int) (x + radius - y1), (int) (y + radius - x1), color);
//
//                GlcdView.this.setPixel((int) (x + width - radius + x1), (int) (y + radius - y1), color);
//                GlcdView.this.setPixel((int) (x + width - radius + y1), (int) (y + radius - x1), color);
//
//                GlcdView.this.setPixel((int) (x + width - radius + y1), (int) (y + height - radius + x1), color);
//
//                GlcdView.this.setPixel((int) (x + radius - y1), (int) (y + height - radius + x1), color);
//
//
//                if (tSwitch <0)
//                    tSwitch += (4*x1+6);
//                else {
//                    tSwitch += (4*(x1-y1)+10);
//                    y1--;
//                }
//                x1++;
//            }
//
//        }
//
//    }

//    private class HorSlider implements ButtonShape {
//
//        float btnX,btnY,btnWidth,btnHeight,start,end,currentValue;
//        int btnTouchId;
//        boolean isPressed=false;
//        int color;
//
//        public HorSlider(float x, float y, float width, float height){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnWidth = width;
//            this.btnHeight = height;
//            this.color = BLACK;
//            this.start = 0;
//            this.end = 100;
//            this.btnTouchId = 0;
//            this.currentValue = start;
//            this.isPressed = false;
//        }
//
//        public HorSlider(float x, float y, float width, float height, float value){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnWidth = width;
//            this.btnHeight = height;
//            this.color = BLACK;
//            this.start = 0;
//            this.end = 100;
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.btnTouchId = 0;
//            this.currentValue = value;
//            this.isPressed = false;
//        }
//
//        public HorSlider(float x, float y, float width, float height, float start, float end){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnWidth = width;
//            this.btnHeight = height;
//            this.color = BLACK;
//            this.start = start;
//            this.end = end;
//            this.btnTouchId = 0;
//            this.currentValue = start;
//            this.isPressed = false;
//        }
//
//        public HorSlider(float x, float y, float width, float height, float start, float end, float value){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnWidth = width;
//            this.btnHeight = height;
//            this.color = BLACK;
//            this.start = start;
//            this.end = end;
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.btnTouchId = 0;
//            this.currentValue = value;
//            this.isPressed = false;
//        }
//
//        public void setCurrentValue(float value) {
//            if (value < start)
//                value = start;
//            else if (value > end)
//                value = end;
//            this.currentValue = value;
//        }
//
//        public float getCurrentValue() {
//            return currentValue;
//        }
//
//        @Override
//        public void draw() {
//
//            float progress = (((currentValue-start)*((btnX+btnWidth-(btnHeight/2))-(btnX+(btnHeight/2))))/(end-start))+(btnX+(btnHeight/2));
//
//            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
//            drawLine(btnX, btnY + (btnHeight / 2), btnX + btnWidth, btnY + (btnHeight / 2), BLACK);
//            fillCircle(progress, btnY + (btnHeight / 2), btnHeight / 2, BLACK);
//            //refresh(true, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//            this.isPressed = false;
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//            currentValue = (((touchX-btnX)*(end-start))/(btnX+btnWidth-btnX))+start;
//        }
//
//        @Override
//        public void setBtnTouchId(int btnTouchId) {
//            this.btnTouchId = btnTouchId;
//            applyTouch();
//        }
//
//        @Override
//        public void applyTouch() {
//            for (float x=btnX;x<btnX+btnWidth;x++){
//                for (float y=btnY;y<btnY+btnHeight;y++){
//                    setTouch((int) x,(int) y,btnTouchId);
//                }
//            }
//        }
//    }

//    public class radioButton implements ButtonShape {
//        float btnX,btnY,btnRadius,btnWidth,btnHeight;
//        RadioGroup radioGroup;
//        int btnTouchId;
//        String btnText = "";
//        boolean isSelected = false,isPressed=false;
//
//        public radioButton (float xCenter,float yCenter,float radius,String text,int touchId){
//            this.btnX = xCenter;
//            this.btnY = yCenter;
//            this.btnRadius = radius;
//            this.btnText = text;
//            this.btnTouchId = touchId;
//            isSelected = false;
//            isPressed = false;
//
//            this.btnWidth = (2*btnRadius)+btnText.length()*8;
//            if (radius < 8)
//                this.btnHeight = 15;
//            else
//                this.btnHeight = 2*btnRadius;
//        }
//
//        public radioButton (float xCenter,float yCenter,float radius,String text){
//            this.btnX = xCenter;
//            this.btnY = yCenter;
//            this.btnRadius = radius;
//            this.btnText = text;
//            this.btnTouchId = 0;
//            isSelected = false;
//            isPressed = false;
//
//            this.btnWidth = (2*btnRadius)+getStringWidth(text,TEXT_SMALL,FONT_ARIEL_REGULAR);
//            if (radius < getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR))
//                this.btnHeight = getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR)*2;
//            else
//                this.btnHeight = 2*btnRadius;
//        }
//
//        public void setRadioGroup(RadioGroup radioGroup) {
//            this.radioGroup = radioGroup;
//        }
//
//        public RadioGroup getRadioGroup() {
//            return radioGroup;
//        }
//
//        @Override
//        public void applyTouch(){
//
//            for (float x=btnX-btnRadius;x<btnX+btnWidth;x++){
//                for (float y=btnY-btnRadius;y<btnY+btnRadius;y++){
//                    setTouch((int) x,(int) y,btnTouchId);
//                }
//            }
//        }
//
//        @Override
//        public void setBtnTouchId(int btnTouchId) {
//            this.btnTouchId = btnTouchId;
//            applyTouch();
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//            setSelected(true);
//            if (radioGroup != null)
//                radioGroup.select(this);
//            this.isPressed = isPressed;
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//
//        public void setSelected(Boolean selected){
//            isSelected = selected;
//        }
//
//        public boolean getSelected(){
//            return isSelected;
//        }
//
//        @Override
//        public void draw() {
//            clear(background,(int) (btnX-btnRadius),(int) (btnY-btnRadius),(int) btnWidth,(int) btnHeight,true,false);
//
//            fillCircle(btnX, btnY, btnRadius, WHITE);
//            drawCircle(btnX, btnY, btnRadius, BLACK);
//            if (isSelected) fillCircle(btnX, btnY, btnRadius - 2, BLACK);
//            drawString(btnText, btnX + btnRadius+2,btnY-btnRadius+2,TEXT_SMALL,FONT_ARIEL_REGULAR,BLACK);
//
//            //refresh(true,(int) (btnX-btnRadius),(int) (btnY-btnRadius),(int) btnWidth,(int) btnHeight);
//        }
//    }

//    public class checkBox implements ButtonShape {
//        float btnX,btnY,btnSize,btnWidth,btnHeight;
//        int btnTouchId;
//        String btnText = "";
//        boolean isSelected = false,isPressed=false;
//
//        public checkBox(float x, float y, float size, String text, int touchId){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnSize = size;
//            this.btnText = text;
//            this.btnTouchId = touchId;
//            isSelected = false;
//            isPressed = false;
//            this.btnWidth = btnSize+getStringWidth(btnText,TEXT_SMALL,FONT_ARIEL_REGULAR);
//            if (btnSize < getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR))
//                btnSize = getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR);
//            this.btnHeight = btnSize;
//        }
//
//        public checkBox(float x, float y, float size, String text){
//            this.btnX = x;
//            this.btnY = y;
//            this.btnSize = size;
//            this.btnText = text;
//            this.btnTouchId = 0;
//            isSelected = false;
//            isPressed = false;
//
//            this.btnWidth = btnSize+getStringWidth(btnText,TEXT_SMALL,FONT_ARIEL_REGULAR);
//            if (btnSize < getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR))
//                btnSize = getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR);
//            this.btnHeight = btnSize;
//        }
//
//        @Override
//        public void applyTouch(){
//            Log.d("GLCD",String.valueOf(btnX)+"+"+String.valueOf(btnSize)+"+"+String.valueOf(getStringWidth("Check Box",TEXT_SMALL,FONT_ARIEL_REGULAR)));
//            for (float x=btnX;x<btnX+btnSize+getStringWidth(btnText,TEXT_SMALL,FONT_ARIEL_REGULAR);x++){
//                for (float y=btnY;y<btnY+btnHeight;y++){
//                    setTouch((int) x, (int) y, btnTouchId);
//                }
//            }
//
//
//        }
//
//        @Override
//        public void setBtnTouchId(int btnTouchId) {
//            this.btnTouchId = btnTouchId;
//            applyTouch();
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//            if (this.isPressed == true && isPressed == false)
//                setSelected(!isSelected);
//            this.isPressed = isPressed;
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//
//        public void setSelected(Boolean selected){
//            isSelected = selected;
//        }
//
//        public boolean getSelected(){
//            return isSelected;
//        }
//
//        @Override
//        public void draw() {
//            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight, true, false);
//
//            fillRectangle(btnX, btnY, btnSize - 1, btnSize, WHITE);
//            drawRectangle(btnX, btnY, btnSize - 1, btnSize, BLACK);
//            if (isSelected) fillRectangle(btnX, btnY, btnSize - 1, btnSize, BLACK);
//            drawString(btnText, btnX + btnSize+2, btnY+(btnSize/2)-(getCharHeight(TEXT_SMALL,FONT_ARIEL_REGULAR)/2)+2, TEXT_SMALL, FONT_ARIEL_REGULAR, BLACK);
//
//            //refresh(true, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight);
//        }
//    }

//    public class button implements ButtonShape {
//        float btnX,btnY,btnWidth,btnHeight;
//        int btnTouchId;
//        String btnText = "";
//        float btnTextX,btnTextY;
//        boolean isPressed=false;
//        int textWidth = 0;
//        int textHeight = 0;
//
//        public button (float x,float y,float width,float height,String text){
//            this.btnX = x;
//            this.btnY = y;
//            //set text width and height to min
//            textWidth = getStringWidth("..",TEXT_SMALL,FONT_ARIEL_REGULAR);
//            textHeight = getCharHeight(TEXT_SMALL, FONT_ARIEL_REGULAR);
//            if (width < textWidth)
//                this.btnWidth = textWidth;
//            else {
//                this.btnWidth = width;
//                textWidth = getStringWidth(text, TEXT_SMALL, FONT_ARIEL_REGULAR);
//                if (width < textWidth){
//                    this.btnText = text.substring(0, getMaxCharsInWidth(text,width,TEXT_SMALL,FONT_ARIEL_REGULAR)-2);
//                    this.btnText += "..";
//                }else{
//                    this.btnText = text;
//                }
//            }
//            if (height < textHeight)
//                this.btnHeight = textHeight;
//            else
//                this.btnHeight = height;
//
//            btnTextX = btnX+((btnWidth-textWidth)/2);
//            btnTextY = btnY+((btnHeight-textHeight)/2);
//            isPressed=false;
//        }
//
//        @Override
//        public void applyTouch(){
//            for (float x=btnX;x<btnX+btnWidth;x++){
//                for (float y=btnY;y<btnY+btnHeight;y++){
//                    setTouch((int) x, (int) y, btnTouchId);
//                }
//            }
//        }
//
//        @Override
//        public void setBtnTouchId(int btnTouchId) {
//            this.btnTouchId = btnTouchId;
//            applyTouch();
//        }
//
//        @Override
//        public void draw(){
//            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
//            if (isPressed){
//                pressDraw();
//            }else {
//                releaseDraw();
//            }
//            //refresh(true, (int) btnX, (int) btnY, (int) btnWidth+1, (int) btnHeight+1);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//            this.isPressed = isPressed;
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//
//        private void releaseDraw() {
//            fillRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK);
//            fillRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, WHITE);
//
//            drawRoundRectangle(btnX + 2, btnY + 2, btnWidth - 2, btnHeight - 2, 2, BLACK); //BLack
//            drawShadowRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK); //Black
//
//            drawString(this.btnText, btnTextX + 2, btnTextY + 2, TEXT_SMALL,FONT_ARIEL_REGULAR, BLACK);
//        }
//
//        private void pressDraw(){
//
//            //fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 3, BLACK);
//            //fillRoundRectangle(btnX , btnY , btnWidth - 3, btnHeight - 3, 3, WHITE);
//
//            drawRoundRectangle(btnX, btnY, btnWidth - 2, btnHeight - 2, 2, BLACK); // black
//            //drawRoundRectangle(btnX , btnY , btnWidth -3, btnHeight -3, 3, BLACK);  // black
//
////            drawString(this.btnText, btnTextX + pixelX, btnTextY + pixelX, TEXT_SMALL, BLACK);
//            drawString(this.btnText, btnTextX, btnTextY, TEXT_SMALL,FONT_ARIEL_REGULAR, BLACK);
//        }
//
//    }

//    public class button2D implements ButtonShape {
//        float btnX,btnY,btnWidth,btnHeight;
//        int btnTouchId;
//        String btnText = "";
//        float btnTextX,btnTextY;
//        boolean isPressed=false;
//        int textWidth = 0;
//        int textHeight = 0;
//
//        public button2D (float x,float y,float width,float height,String text){
//            this.btnX = x;
//            this.btnY = y;
//            //set text width and height to min
//            textWidth = getStringWidth("..",TEXT_SMALL,FONT_ARIEL_REGULAR);
//            textHeight = getCharHeight(TEXT_SMALL, FONT_ARIEL_REGULAR);
//            if (width < textWidth)
//                this.btnWidth = textWidth;
//            else {
//                this.btnWidth = width;
//                textWidth = getStringWidth(text, TEXT_SMALL, FONT_ARIEL_REGULAR);
//                if (width < textWidth){
//                    this.btnText = text.substring(0, getMaxCharsInWidth(text,width,TEXT_SMALL,FONT_ARIEL_REGULAR)-2);
//                    this.btnText += "..";
//                }else{
//                    this.btnText = text;
//                }
//            }
//            if (height < textHeight)
//                this.btnHeight = textHeight;
//            else
//                this.btnHeight = height;
//
//            btnTextX = btnX+((btnWidth-textWidth)/2);
//            btnTextY = btnY+((btnHeight-textHeight)/2);
//            isPressed=false;
//        }
//
//        @Override
//        public void applyTouch(){
//            for (float x=btnX;x<btnX+btnWidth;x++){
//                for (float y=btnY;y<btnY+btnHeight;y++){
//                    setTouch((int) x, (int) y, btnTouchId);
//                }
//            }
//        }
//
//        @Override
//        public void setBtnTouchId(int btnTouchId) {
//            this.btnTouchId = btnTouchId;
//            applyTouch();
//        }
//
//        @Override
//        public void draw(){
//            clear(background, (int) btnX, (int) btnY, (int) btnWidth + 1, (int) btnHeight + 1, true, false);
//            if (isPressed){
//                pressDraw();
//            }else {
//                releaseDraw();
//            }
//            refresh(true, (int) btnX, (int) btnY, (int) btnWidth+1, (int) btnHeight+1);
//        }
//
//        @Override
//        public void setIsPressed(boolean isPressed) {
//            this.isPressed = isPressed;
//        }
//
//        @Override
//        public void setTouched(int touchX, int touchY) {
//
//        }
//
//        private void releaseDraw() {
//            fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, WHITE+1);
//            drawRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, BLACK);
//            drawString(this.btnText, btnTextX + 2, btnTextY + 2, TEXT_SMALL,FONT_ARIEL_REGULAR, BLACK);
//        }
//
//        private void pressDraw(){
//            fillRoundRectangle(btnX, btnY, btnWidth, btnHeight, 2, BLACK);
//            //drawRoundRectangle(btnX,btnY,btnWidth,btnHeight,2,WHITE+1);
//            drawString(this.btnText, btnTextX + 2, btnTextY + 2, TEXT_SMALL,FONT_ARIEL_REGULAR, WHITE+1);
//        }
//
//    }

    // fonts
    private class font{
        protected int font_width=0,font_height=0,first_char = 0,char_Count=0;
        protected int[] chars_width= new int[]{};
        protected int[] font = new int[]{};

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

        public int[] getChars_width() {
            return chars_width;
        }

        public int[] getFont() {
            return font;
        }
    }

    public class ArielRegular extends font{

        public ArielRegular(){
            font_width=10;
            font_height=12;
            first_char = 32;
            char_Count=95;
            chars_width = new int[]{
                    0x00, 0x01, 0x03, 0x07, 0x05, 0x09, 0x07, 0x01, 0x03, 0x03,
                    0x05, 0x05, 0x01, 0x03, 0x01, 0x03, 0x05, 0x03, 0x05, 0x05,
                    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x01, 0x01, 0x05, 0x06,
                    0x05, 0x05, 0x0B, 0x07, 0x06, 0x07, 0x07, 0x06, 0x05, 0x07,
                    0x07, 0x01, 0x05, 0x07, 0x06, 0x07, 0x07, 0x07, 0x06, 0x07,
                    0x07, 0x06, 0x07, 0x07, 0x07, 0x0B, 0x07, 0x07, 0x07, 0x02,
                    0x03, 0x02, 0x05, 0x07, 0x02, 0x05, 0x05, 0x04, 0x05, 0x05,
                    0x03, 0x05, 0x05, 0x01, 0x02, 0x05, 0x01, 0x09, 0x05, 0x05,
                    0x05, 0x05, 0x03, 0x05, 0x03, 0x05, 0x05, 0x09, 0x05, 0x05,
                    0x05, 0x03, 0x01, 0x03, 0x06, 0x06,
            };
            font = new int[]{
                    0x7F, 0x10, // 33
                    0x07, 0x00, 0x07, 0x00, 0x00, 0x00, // 34
                    0x24, 0xE4, 0x3C, 0xE7, 0x3C, 0x27, 0x24, 0x00, 0x10, 0x00, 0x10, 0x00, 0x00, 0x00, // 35
                    0xCE, 0x11, 0xFF, 0x11, 0xE2, 0x00, 0x10, 0x30, 0x10, 0x00, // 36
                    0x0E, 0x11, 0x11, 0xCE, 0x38, 0xE6, 0x11, 0x10, 0xE0, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x10, 0x10, 0x00, // 37
                    0xE0, 0x1E, 0x11, 0x29, 0xC6, 0xA0, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x00, 0x10, // 38
                    0x07, 0x00, // 39
                    0xF8, 0x06, 0x01, 0x00, 0x30, 0x40, // 40
                    0x01, 0x06, 0xF8, 0x40, 0x30, 0x00, // 41
                    0x02, 0x0A, 0x07, 0x0A, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, // 42
                    0x10, 0x10, 0x7C, 0x10, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, // 43
                    0x00, 0x70, // 44
                    0x20, 0x20, 0x20, 0x00, 0x00, 0x00, // 45
                    0x00, 0x10, // 46
                    0x80, 0x7C, 0x03, 0x10, 0x00, 0x00, // 47
                    0xFE, 0x01, 0x01, 0x01, 0xFE, 0x00, 0x10, 0x10, 0x10, 0x00, // 48
                    0x04, 0x02, 0xFF, 0x00, 0x00, 0x10, // 49
                    0x02, 0x81, 0x41, 0x31, 0x0E, 0x10, 0x10, 0x10, 0x10, 0x10, // 50
                    0x82, 0x01, 0x11, 0x11, 0xEE, 0x00, 0x10, 0x10, 0x10, 0x00, // 51
                    0x60, 0x58, 0x46, 0xFF, 0x40, 0x00, 0x00, 0x00, 0x10, 0x00, // 52
                    0x9C, 0x0B, 0x09, 0x09, 0xF1, 0x00, 0x10, 0x10, 0x10, 0x00, // 53
                    0xFE, 0x11, 0x09, 0x09, 0xF2, 0x00, 0x10, 0x10, 0x10, 0x00, // 54
                    0x01, 0xC1, 0x39, 0x05, 0x03, 0x00, 0x10, 0x00, 0x00, 0x00, // 55
                    0xEE, 0x11, 0x11, 0x11, 0xEE, 0x00, 0x10, 0x10, 0x10, 0x00, // 56
                    0x9E, 0x21, 0x21, 0x11, 0xFE, 0x00, 0x10, 0x10, 0x10, 0x00, // 57
                    0x04, 0x10, // 58
                    0x04, 0x70, // 59
                    0x10, 0x28, 0x28, 0x28, 0x44, 0x00, 0x00, 0x00, 0x00, 0x00, // 60
                    0x48, 0x48, 0x48, 0x48, 0x48, 0x48, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 61
                    0x44, 0x28, 0x28, 0x28, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, // 62
                    0x06, 0x01, 0x61, 0x11, 0x0E, 0x00, 0x00, 0x10, 0x00, 0x00, // 63
                    0xF0, 0x0C, 0xE2, 0x12, 0x09, 0x09, 0xF1, 0x19, 0x02, 0x86, 0x78, 0x10, 0x20, 0x40, 0x90, 0x90, 0x90, 0x90, 0x90, 0x50, 0x40, 0x20, // 64
                    0x80, 0x70, 0x2E, 0x21, 0x2E, 0x70, 0x80, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 65
                    0xFF, 0x11, 0x11, 0x11, 0x11, 0xEE, 0x10, 0x10, 0x10, 0x10, 0x10, 0x00, // 66
                    0x7C, 0x82, 0x01, 0x01, 0x01, 0x82, 0x44, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x00, // 67
                    0xFF, 0x01, 0x01, 0x01, 0x01, 0x82, 0x7C, 0x10, 0x10, 0x10, 0x10, 0x10, 0x00, 0x00, // 68
                    0xFF, 0x11, 0x11, 0x11, 0x11, 0x11, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, // 69
                    0xFF, 0x11, 0x11, 0x11, 0x01, 0x10, 0x00, 0x00, 0x00, 0x00, // 70
                    0x7C, 0x82, 0x01, 0x01, 0x11, 0x92, 0x74, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x00, // 71
                    0xFF, 0x10, 0x10, 0x10, 0x10, 0x10, 0xFF, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 72
                    0xFF, 0x10, // 73
                    0xC0, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x10, 0x10, 0x10, 0x00, // 74
                    0xFF, 0x20, 0x10, 0x28, 0x44, 0x82, 0x01, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 75
                    0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, // 76
                    0xFF, 0x06, 0x78, 0x80, 0x78, 0x06, 0xFF, 0x10, 0x00, 0x00, 0x10, 0x00, 0x00, 0x10, // 77
                    0xFF, 0x02, 0x0C, 0x10, 0x60, 0x80, 0xFF, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 78
                    0x7C, 0x82, 0x01, 0x01, 0x01, 0x82, 0x7C, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x00, // 79
                    0xFF, 0x11, 0x11, 0x11, 0x11, 0x0E, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, // 80
                    0x7C, 0x82, 0x01, 0x41, 0x41, 0x82, 0x7C, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x10, // 81
                    0xFF, 0x11, 0x11, 0x11, 0x31, 0xD1, 0x0E, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 82
                    0xCE, 0x11, 0x11, 0x11, 0x11, 0xE6, 0x00, 0x10, 0x10, 0x10, 0x10, 0x00, // 83
                    0x01, 0x01, 0x01, 0xFF, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, // 84
                    0x7F, 0x80, 0x00, 0x00, 0x00, 0x80, 0x7F, 0x00, 0x00, 0x10, 0x10, 0x10, 0x00, 0x00, // 85
                    0x03, 0x1C, 0x60, 0x80, 0x60, 0x1C, 0x03, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, // 86
                    0x07, 0x78, 0x80, 0x70, 0x0E, 0x01, 0x0E, 0x70, 0x80, 0x7C, 0x03, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, // 87
                    0x01, 0xC6, 0x28, 0x10, 0x6C, 0x82, 0x01, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 88
                    0x01, 0x06, 0x08, 0xF0, 0x08, 0x06, 0x01, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, // 89
                    0x00, 0x81, 0x61, 0x11, 0x0D, 0x03, 0x01, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, // 90
                    0xFF, 0x01, 0x70, 0x40, // 91
                    0x03, 0x7C, 0x80, 0x00, 0x00, 0x10, // 92
                    0x01, 0xFF, 0x40, 0x70, // 93
                    0x10, 0x0E, 0x01, 0x0E, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, // 94
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, // 95
                    0x01, 0x02, 0x00, 0x00, // 96
                    0xC8, 0x24, 0x24, 0xA4, 0xF8, 0x00, 0x10, 0x10, 0x00, 0x10, // 97
                    0xFF, 0x88, 0x04, 0x04, 0xF8, 0x10, 0x00, 0x10, 0x10, 0x00, // 98
                    0xF8, 0x04, 0x04, 0x88, 0x00, 0x10, 0x10, 0x00, // 99
                    0xF8, 0x04, 0x04, 0x88, 0xFF, 0x00, 0x10, 0x10, 0x00, 0x10, // 100
                    0xF8, 0x24, 0x24, 0x24, 0xB8, 0x00, 0x10, 0x10, 0x10, 0x00, // 101
                    0x04, 0xFE, 0x05, 0x00, 0x10, 0x00, // 102
                    0xF8, 0x04, 0x04, 0x88, 0xFC, 0x40, 0x50, 0x50, 0x40, 0x30, // 103
                    0xFF, 0x08, 0x04, 0x04, 0xF8, 0x10, 0x00, 0x00, 0x00, 0x10, // 104
                    0xFD, 0x10, // 105
                    0x00, 0xFD, 0x40, 0x30, // 106
                    0xFF, 0x20, 0x30, 0xC8, 0x04, 0x10, 0x00, 0x00, 0x00, 0x10, // 107
                    0xFF, 0x10, // 108
                    0xFC, 0x08, 0x04, 0x04, 0xF8, 0x08, 0x04, 0x04, 0xF8, 0x10, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x10, // 109
                    0xFC, 0x08, 0x04, 0x04, 0xF8, 0x10, 0x00, 0x00, 0x00, 0x10, // 110
                    0xF8, 0x04, 0x04, 0x04, 0xF8, 0x00, 0x10, 0x10, 0x10, 0x00, // 111
                    0xFC, 0x88, 0x04, 0x04, 0xF8, 0x70, 0x00, 0x10, 0x10, 0x00, // 112
                    0xF8, 0x04, 0x04, 0x88, 0xFC, 0x00, 0x10, 0x10, 0x00, 0x70, // 113
                    0xFC, 0x08, 0x04, 0x10, 0x00, 0x00, // 114
                    0x98, 0x24, 0x24, 0x24, 0xC8, 0x00, 0x10, 0x10, 0x10, 0x00, // 115
                    0x04, 0xFF, 0x04, 0x00, 0x10, 0x10, // 116
                    0xFC, 0x00, 0x00, 0x80, 0xFC, 0x00, 0x10, 0x10, 0x00, 0x10, // 117
                    0x0C, 0x70, 0x80, 0x70, 0x0C, 0x00, 0x00, 0x10, 0x00, 0x00, // 118
                    0x0C, 0x70, 0x80, 0x70, 0x0C, 0x70, 0x80, 0x70, 0x0C, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, // 119
                    0x04, 0xD8, 0x60, 0x98, 0x04, 0x10, 0x00, 0x00, 0x00, 0x10, // 120
                    0x0C, 0x70, 0x80, 0x70, 0x0C, 0x00, 0x40, 0x30, 0x00, 0x00, // 121
                    0x04, 0xC4, 0x24, 0x1C, 0x04, 0x10, 0x10, 0x10, 0x10, 0x10, // 122
                    0x20, 0xDE, 0x01, 0x00, 0x30, 0x40, // 123
                    0xFF, 0x70, // 124
                    0x01, 0xDE, 0x20, 0x40, 0x30, 0x00, // 125
                    0x20, 0x10, 0x10, 0x20, 0x20, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 126
                    0xFE, 0x02, 0x02, 0x02, 0x02, 0xFE, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10 // 127
            };
        }

    }

    public class ArielBlack extends font{

        public ArielBlack(){
            font_width=10;
            font_height=12;
            first_char = 32;
            char_Count=95;
            chars_width = new int[]{
                    0x00, 0x03, 0x06, 0x07, 0x07, 0x0A, 0x09, 0x03, 0x03, 0x03,
                    0x05, 0x06, 0x03, 0x04, 0x03, 0x03, 0x07, 0x05, 0x07, 0x07,
                    0x08, 0x07, 0x07, 0x07, 0x07, 0x07, 0x03, 0x03, 0x06, 0x06,
                    0x06, 0x07, 0x09, 0x09, 0x07, 0x07, 0x07, 0x07, 0x06, 0x08,
                    0x08, 0x03, 0x07, 0x09, 0x06, 0x09, 0x08, 0x08, 0x07, 0x08,
                    0x08, 0x07, 0x09, 0x08, 0x09, 0x0C, 0x09, 0x09, 0x07, 0x04,
                    0x03, 0x04, 0x06, 0x06, 0x03, 0x07, 0x07, 0x07, 0x07, 0x07,
                    0x05, 0x07, 0x07, 0x03, 0x03, 0x08, 0x03, 0x0B, 0x07, 0x07,
                    0x07, 0x07, 0x05, 0x06, 0x04, 0x07, 0x07, 0x0B, 0x07, 0x07,
                    0x06, 0x04, 0x02, 0x04, 0x06, 0x06,
            };
            font = new int[]{
                    0xBE, 0xBE, 0xBE, 0x30, 0x30, 0x30, // 33
                    0x0E, 0x0E, 0x0E, 0x00, 0x0E, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 34
                    0xD8, 0xF8, 0xFE, 0xDE, 0xF8, 0xFE, 0xDE, 0x30, 0x30, 0x00, 0x30, 0x30, 0x00, 0x00, // 35
                    0x9C, 0xBE, 0x36, 0xFF, 0x66, 0xEE, 0xCC, 0x10, 0x30, 0x30, 0x70, 0x30, 0x30, 0x10, // 36
                    0x1C, 0x22, 0x22, 0x9C, 0x60, 0x10, 0xCC, 0x22, 0x20, 0xC0, 0x00, 0x00, 0x20, 0x10, 0x00, 0x00, 0x10, 0x20, 0x20, 0x10, // 37
                    0x80, 0xDC, 0xFE, 0x76, 0x76, 0xDE, 0xCC, 0xC0, 0x40, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, 0x10, 0x30, // 38
                    0x0E, 0x0E, 0x0E, 0x00, 0x00, 0x00, // 39
                    0xF0, 0xFC, 0x06, 0x10, 0x70, 0xC0, // 40
                    0x06, 0xFC, 0xF0, 0xC0, 0x70, 0x10, // 41
                    0x04, 0x14, 0x0E, 0x14, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, // 42
                    0x60, 0x60, 0xF8, 0xF8, 0x60, 0x60, 0x00, 0x00, 0x10, 0x10, 0x00, 0x00, // 43
                    0x80, 0x80, 0x80, 0xB0, 0xF0, 0x70, // 44
                    0xC0, 0xC0, 0xC0, 0xC0, 0x00, 0x00, 0x00, 0x00, // 45
                    0x80, 0x80, 0x80, 0x30, 0x30, 0x30, // 46
                    0x00, 0xF8, 0x06, 0x30, 0x00, 0x00, // 47
                    0xF8, 0xFC, 0xFE, 0x06, 0xFE, 0xFC, 0xF8, 0x00, 0x10, 0x30, 0x30, 0x30, 0x10, 0x00, // 48
                    0x18, 0x0C, 0xFE, 0xFE, 0xFE, 0x00, 0x00, 0x30, 0x30, 0x30, // 49
                    0x0C, 0x8E, 0xCE, 0xE6, 0x7E, 0x3E, 0x1C, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, // 50
                    0x8C, 0x8E, 0x8E, 0x26, 0xFE, 0xDE, 0xCC, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 51
                    0xE0, 0xF0, 0xF8, 0xCC, 0xFE, 0xFE, 0xFE, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, // 52
                    0xBE, 0xBE, 0xB6, 0x36, 0xF6, 0xF6, 0xE6, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 53
                    0xF8, 0xFC, 0xFE, 0x26, 0xE6, 0xE6, 0xE4, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, // 54
                    0x06, 0x06, 0xE6, 0xF6, 0x7E, 0x1E, 0x06, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, // 55
                    0xCC, 0xFE, 0xFE, 0x36, 0xFE, 0xFE, 0xCC, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 56
                    0x3C, 0x3E, 0x26, 0x26, 0xFE, 0xFC, 0xF8, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, 0x00, // 57
                    0x9C, 0x9C, 0x9C, 0x30, 0x30, 0x30, // 58
                    0x9C, 0x9C, 0x9C, 0xB0, 0xF0, 0x70, // 59
                    0x70, 0x70, 0xD8, 0xD8, 0xD8, 0x8C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, // 60
                    0xD8, 0xD8, 0xD8, 0xD8, 0xD8, 0xD8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 61
                    0x8C, 0xD8, 0xD8, 0xD8, 0x70, 0x70, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, // 62
                    0x0C, 0x0E, 0x66, 0x76, 0x7E, 0x3E, 0x1C, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, // 63
                    0xF0, 0x0C, 0xF4, 0x0A, 0x0A, 0xF2, 0x1A, 0x84, 0x78, 0x00, 0x30, 0x20, 0x50, 0x50, 0x50, 0x50, 0x20, 0x20, // 64
                    0x00, 0xF8, 0xFE, 0xFE, 0x8E, 0xFE, 0xFE, 0xF8, 0x00, 0x30, 0x30, 0x30, 0x10, 0x10, 0x10, 0x30, 0x30, 0x30, // 65
                    0xFE, 0xFE, 0xFE, 0x36, 0xFE, 0xFE, 0xCC, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 66
                    0xF8, 0xFC, 0xFE, 0x06, 0x8E, 0x8E, 0x8C, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, // 67
                    0xFE, 0xFE, 0xFE, 0x06, 0xFE, 0xFC, 0xF8, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, 0x00, // 68
                    0xFE, 0xFE, 0xFE, 0x36, 0x36, 0x36, 0x36, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, // 69
                    0xFE, 0xFE, 0xFE, 0x36, 0x36, 0x06, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, // 70
                    0xF8, 0xFC, 0xFE, 0x06, 0x66, 0xEE, 0xEE, 0xEC, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 71
                    0xFE, 0xFE, 0xFE, 0x30, 0x30, 0xFE, 0xFE, 0xFE, 0x30, 0x30, 0x30, 0x00, 0x00, 0x30, 0x30, 0x30, // 72
                    0xFE, 0xFE, 0xFE, 0x30, 0x30, 0x30, // 73
                    0x80, 0x80, 0x80, 0x00, 0xFE, 0xFE, 0xFE, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 74
                    0xFE, 0xFE, 0xFE, 0x70, 0x38, 0xFC, 0xFE, 0x86, 0x02, 0x30, 0x30, 0x30, 0x00, 0x00, 0x10, 0x30, 0x30, 0x20, // 75
                    0xFE, 0xFE, 0xFE, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, // 76
                    0xFE, 0xFE, 0x1E, 0xF8, 0x80, 0xF8, 0x1E, 0xFE, 0xFE, 0x30, 0x30, 0x00, 0x10, 0x30, 0x10, 0x00, 0x30, 0x30, // 77
                    0xFE, 0xFE, 0x1C, 0x38, 0xE0, 0xC0, 0xFE, 0xFE, 0x30, 0x30, 0x00, 0x00, 0x00, 0x10, 0x30, 0x30, // 78
                    0xF8, 0xFC, 0xFE, 0x06, 0x06, 0xFE, 0xFC, 0xF8, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, 0x00, // 79
                    0xFE, 0xFE, 0xFE, 0x66, 0x7E, 0x7E, 0x3C, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, // 80
                    0xF8, 0xFC, 0xFE, 0x06, 0x86, 0xFE, 0xFC, 0xF8, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x60, // 81
                    0xFE, 0xFE, 0xFE, 0x36, 0xFE, 0xFE, 0xDC, 0x00, 0x30, 0x30, 0x30, 0x00, 0x10, 0x30, 0x30, 0x30, // 82
                    0x9C, 0xBE, 0x3E, 0x36, 0xE6, 0xEE, 0xCC, 0x10, 0x30, 0x30, 0x30, 0x30, 0x30, 0x10, // 83
                    0x06, 0x06, 0x06, 0xFE, 0xFE, 0xFE, 0x06, 0x06, 0x06, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, // 84
                    0xFE, 0xFE, 0xFE, 0x00, 0x00, 0xFE, 0xFE, 0xFE, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, 0x00, // 85
                    0x02, 0x1E, 0xFE, 0xF8, 0x80, 0xF8, 0xFE, 0x1E, 0x02, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, // 86
                    0x06, 0xFE, 0xFE, 0xC0, 0xF0, 0xFE, 0x0E, 0xFE, 0xF0, 0xC0, 0xFE, 0xFE, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, // 87
                    0x02, 0x86, 0xDE, 0xFC, 0x70, 0xFC, 0xDE, 0x86, 0x02, 0x20, 0x30, 0x30, 0x10, 0x00, 0x10, 0x30, 0x30, 0x20, // 88
                    0x02, 0x0E, 0x1E, 0xFC, 0xF0, 0xFC, 0x1E, 0x0E, 0x02, 0x00, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, // 89
                    0x06, 0xC6, 0xE6, 0x76, 0x3E, 0x1E, 0x06, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, // 90
                    0xFE, 0xFE, 0xFE, 0x06, 0xF0, 0xF0, 0xF0, 0xC0, // 91
                    0x06, 0xF8, 0x00, 0x00, 0x00, 0x30, // 92
                    0x06, 0xFE, 0xFE, 0xFE, 0xC0, 0xF0, 0xF0, 0xF0, // 93
                    0x20, 0x38, 0x0E, 0x0E, 0x38, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 94
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, // 95
                    0x02, 0x06, 0x04, 0x00, 0x00, 0x00, // 96
                    0xB0, 0xB8, 0xD8, 0x58, 0xF8, 0xF8, 0xF0, 0x10, 0x30, 0x30, 0x20, 0x30, 0x30, 0x30, // 97
                    0xFE, 0xFE, 0xFE, 0x10, 0x10, 0xF0, 0xF0, 0x30, 0x30, 0x30, 0x10, 0x10, 0x10, 0x10, // 98
                    0xE0, 0xF0, 0xF8, 0x18, 0x18, 0xB0, 0xA0, 0x00, 0x10, 0x30, 0x30, 0x30, 0x10, 0x00, // 99
                    0xF0, 0xF8, 0xF8, 0x10, 0xFE, 0xFE, 0xFE, 0x10, 0x30, 0x30, 0x10, 0x30, 0x30, 0x30, // 100
                    0xE0, 0xF0, 0xF8, 0x48, 0x78, 0x78, 0x70, 0x00, 0x10, 0x30, 0x30, 0x30, 0x30, 0x10, // 101
                    0xFC, 0xFE, 0xFE, 0x1A, 0x02, 0x30, 0x30, 0x30, 0x00, 0x00, // 102
                    0xF0, 0xF8, 0xF8, 0x10, 0xF8, 0xF8, 0xF8, 0x90, 0x90, 0x90, 0x90, 0xF0, 0xF0, 0x70, // 103
                    0xFE, 0xFE, 0xFE, 0x10, 0xF0, 0xF0, 0xF0, 0x30, 0x30, 0x30, 0x00, 0x30, 0x30, 0x30, // 104
                    0xFA, 0xFA, 0xFA, 0x30, 0x30, 0x30, // 105
                    0x1A, 0xFA, 0xFA, 0xC0, 0xF0, 0x70, // 106
                    0xFE, 0xFE, 0xFE, 0xE0, 0xF0, 0xF8, 0x98, 0x08, 0x30, 0x30, 0x30, 0x00, 0x10, 0x30, 0x30, 0x20, // 107
                    0xFE, 0xFE, 0xFE, 0x30, 0x30, 0x30, // 108
                    0xF8, 0xF8, 0xF8, 0x10, 0xF8, 0xF8, 0xF0, 0x18, 0xF8, 0xF8, 0xF0, 0x30, 0x30, 0x30, 0x00, 0x30, 0x30, 0x30, 0x00, 0x30, 0x30, 0x30, // 109
                    0xF8, 0xF8, 0xF8, 0x10, 0xF8, 0xF8, 0xF0, 0x30, 0x30, 0x30, 0x00, 0x30, 0x30, 0x30, // 110
                    0xE0, 0xF0, 0xF8, 0x18, 0xF8, 0xF0, 0xE0, 0x00, 0x10, 0x30, 0x30, 0x30, 0x10, 0x00, // 111
                    0xF8, 0xF8, 0xF8, 0x10, 0xF8, 0xF8, 0xF0, 0xF0, 0xF0, 0xF0, 0x10, 0x30, 0x30, 0x10, // 112
                    0xF0, 0xF8, 0xF8, 0x10, 0xF8, 0xF8, 0xF8, 0x10, 0x30, 0x30, 0x10, 0xF0, 0xF0, 0xF0, // 113
                    0xF8, 0xF8, 0xF8, 0x10, 0x18, 0x30, 0x30, 0x30, 0x00, 0x00, // 114
                    0x30, 0x78, 0x78, 0xC8, 0xD8, 0x90, 0x10, 0x30, 0x20, 0x30, 0x30, 0x10, // 115
                    0xFC, 0xFE, 0xFE, 0x18, 0x10, 0x30, 0x30, 0x30, // 116
                    0xF8, 0xF8, 0xF8, 0x00, 0xF8, 0xF8, 0xF8, 0x10, 0x30, 0x30, 0x10, 0x30, 0x30, 0x30, // 117
                    0x08, 0x78, 0xF8, 0x80, 0xF8, 0x78, 0x08, 0x00, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, // 118
                    0x18, 0xF8, 0xF8, 0xE0, 0xF8, 0x38, 0xF8, 0xE0, 0xF8, 0xF8, 0x18, 0x00, 0x00, 0x30, 0x30, 0x00, 0x00, 0x00, 0x30, 0x30, 0x00, 0x00, // 119
                    0x08, 0xB8, 0xF8, 0xE0, 0xF8, 0xB8, 0x08, 0x20, 0x30, 0x30, 0x00, 0x30, 0x30, 0x20, // 120
                    0x08, 0x78, 0xF8, 0x00, 0xF8, 0xF8, 0x18, 0xC0, 0xC0, 0xF0, 0xF0, 0x70, 0x00, 0x00, // 121
                    0x18, 0x98, 0xD8, 0x78, 0x38, 0x18, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, // 122
                    0x60, 0xFC, 0x9E, 0x06, 0x00, 0x70, 0xF0, 0xC0, // 123
                    0xFE, 0xFE, 0xF0, 0xF0, // 124
                    0x06, 0x9E, 0xFC, 0x60, 0xC0, 0xF0, 0x70, 0x00, // 125
                    0x60, 0x30, 0x30, 0x60, 0x60, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 126
                    0xFC, 0x04, 0x04, 0x04, 0x04, 0xFC, 0x30, 0x20, 0x20, 0x20, 0x20, 0x30 // 127
            };
        }

    }

    public class ArielItalic extends font{

        public ArielItalic(){
            font_width=10;
            font_height=13;
            first_char = 32;
            char_Count=95;
            chars_width = new int[]{
                    0x00, 0x03, 0x01, 0x06, 0x07, 0x09, 0x07, 0x00, 0x03, 0x03,
                    0x04, 0x05, 0x02, 0x04, 0x01, 0x03, 0x06, 0x04, 0x06, 0x06,
                    0x06, 0x06, 0x06, 0x05, 0x06, 0x06, 0x02, 0x03, 0x06, 0x06,
                    0x06, 0x05, 0x0B, 0x07, 0x07, 0x08, 0x08, 0x07, 0x06, 0x08,
                    0x08, 0x03, 0x05, 0x07, 0x06, 0x09, 0x08, 0x08, 0x07, 0x08,
                    0x08, 0x07, 0x06, 0x08, 0x06, 0x09, 0x08, 0x06, 0x07, 0x04,
                    0x03, 0x04, 0x05, 0x06, 0x02, 0x06, 0x07, 0x05, 0x06, 0x06,
                    0x05, 0x07, 0x06, 0x02, 0x03, 0x06, 0x02, 0x0A, 0x06, 0x06,
                    0x07, 0x06, 0x04, 0x05, 0x03, 0x06, 0x05, 0x07, 0x06, 0x06,
                    0x06, 0x03, 0x01, 0x04, 0x06, 0x06,
            };
            font = new int[]{
                    0x00, 0xF0, 0x0E, 0x10, 0x00, 0x00, // 33
                    0x0E, 0x00, // 34
                    0x48, 0xF8, 0x4E, 0xC8, 0x7E, 0x48, 0x00, 0x18, 0x00, 0x18, 0x00, 0x00, // 35
                    0x80, 0x0C, 0x12, 0xFA, 0x27, 0xC2, 0x04, 0x08, 0x10, 0x38, 0x10, 0x10, 0x08, 0x00, // 36
                    0x3C, 0x22, 0x22, 0xDE, 0x20, 0xD8, 0x24, 0x22, 0xE0, 0x00, 0x10, 0x08, 0x00, 0x00, 0x18, 0x10, 0x10, 0x08, // 37
                    0xC0, 0x20, 0x3C, 0x72, 0x92, 0x8C, 0x40, 0x08, 0x10, 0x10, 0x10, 0x08, 0x18, 0x10, // 38
                    0xE0, 0x18, 0x04, 0x38, 0x40, 0x00, // 40
                    0x00, 0x02, 0xFC, 0x20, 0x18, 0x00, // 41
                    0x04, 0x14, 0x0E, 0x14, 0x00, 0x00, 0x00, 0x00, // 42
                    0x20, 0x20, 0xF8, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, // 43
                    0x00, 0x00, 0x40, 0x30, // 44
                    0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, // 45
                    0x00, 0x10, // 46
                    0x80, 0x60, 0x30, 0x08, 0x00, 0x00, // 47
                    0xF0, 0x0C, 0x02, 0x02, 0x82, 0x7C, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 48
                    0x10, 0x88, 0x7C, 0x02, 0x00, 0x18, 0x00, 0x00, // 49
                    0x00, 0x0C, 0x82, 0x42, 0x22, 0x1C, 0x10, 0x18, 0x10, 0x10, 0x10, 0x00, // 50
                    0x00, 0x0C, 0x02, 0x22, 0x22, 0xDC, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 51
                    0xC0, 0xA0, 0x90, 0x88, 0xFC, 0x86, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, // 52
                    0x90, 0x0E, 0x0A, 0x0A, 0x0A, 0xF2, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 53
                    0xF8, 0x24, 0x12, 0x12, 0x12, 0xE4, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 54
                    0x02, 0xC2, 0x32, 0x0A, 0x06, 0x18, 0x00, 0x00, 0x00, 0x00, // 55
                    0xC0, 0x2C, 0x12, 0x12, 0x12, 0xEC, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 56
                    0x38, 0x44, 0x42, 0x42, 0xA2, 0x7C, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 57
                    0x00, 0x08, 0x10, 0x00, // 58
                    0x00, 0x00, 0x08, 0x40, 0x30, 0x00, // 59
                    0x20, 0x50, 0x50, 0x50, 0x88, 0x88, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 60
                    0x90, 0x90, 0x90, 0x90, 0x90, 0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 61
                    0x88, 0x88, 0x50, 0x50, 0x50, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 62
                    0x04, 0xC2, 0x22, 0x12, 0x0C, 0x00, 0x10, 0x00, 0x00, 0x00, // 63
                    0xE0, 0x18, 0xE4, 0x16, 0x0A, 0x0A, 0xF2, 0x3A, 0x04, 0x8C, 0xF0, 0x18, 0x20, 0x48, 0x90, 0x90, 0x88, 0x98, 0x90, 0x50, 0x48, 0x20, // 64
                    0x00, 0x80, 0xC0, 0xB0, 0x8C, 0x82, 0xFC, 0x10, 0x08, 0x00, 0x00, 0x00, 0x00, 0x18, // 65
                    0x00, 0xF0, 0x2E, 0x22, 0x22, 0x22, 0xDC, 0x18, 0x10, 0x10, 0x10, 0x10, 0x10, 0x08, // 66
                    0xF0, 0x0C, 0x04, 0x02, 0x02, 0x02, 0x02, 0x8C, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 67
                    0x00, 0xF8, 0x06, 0x02, 0x02, 0x02, 0x82, 0x7C, 0x18, 0x10, 0x10, 0x10, 0x10, 0x08, 0x08, 0x00, // 68
                    0x00, 0xF8, 0x26, 0x22, 0x22, 0x22, 0x02, 0x18, 0x10, 0x10, 0x10, 0x10, 0x10, 0x00, // 69
                    0x00, 0xF8, 0x26, 0x22, 0x22, 0x22, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, // 70
                    0xF0, 0x0C, 0x04, 0x02, 0x22, 0x22, 0xA2, 0x6C, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 71
                    0x00, 0xF8, 0x26, 0x20, 0x20, 0x20, 0xF0, 0x0E, 0x18, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, // 72
                    0x00, 0xF8, 0x06, 0x18, 0x00, 0x00, // 73
                    0x80, 0x00, 0x00, 0xE0, 0x1E, 0x08, 0x10, 0x08, 0x00, 0x00, // 74
                    0x80, 0x78, 0x26, 0x30, 0xC8, 0x04, 0x02, 0x18, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, // 75
                    0x00, 0xF8, 0x06, 0x00, 0x00, 0x00, 0x18, 0x10, 0x10, 0x10, 0x10, 0x10, // 76
                    0x00, 0xF8, 0x06, 0xFE, 0x00, 0x80, 0x70, 0x8C, 0x7E, 0x18, 0x00, 0x00, 0x00, 0x18, 0x08, 0x00, 0x18, 0x00, // 77
                    0x00, 0xF8, 0x06, 0x1C, 0xE0, 0x00, 0xF0, 0x0E, 0x18, 0x00, 0x00, 0x00, 0x08, 0x18, 0x00, 0x00, // 78
                    0xF0, 0x0C, 0x04, 0x02, 0x02, 0x02, 0x84, 0x78, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 79
                    0x00, 0xF8, 0x26, 0x22, 0x22, 0x22, 0x3E, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 80
                    0xF0, 0x0C, 0x04, 0x02, 0x82, 0x02, 0x84, 0x78, 0x00, 0x08, 0x10, 0x10, 0x10, 0x18, 0x28, 0x00, // 81
                    0x00, 0xF8, 0x26, 0x22, 0x22, 0xE2, 0x22, 0x1C, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, // 82
                    0x80, 0x1C, 0x12, 0x22, 0x22, 0xC2, 0x0C, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 83
                    0x02, 0x02, 0xF2, 0x0E, 0x02, 0x02, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, // 84
                    0xF0, 0x0E, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x0E, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, 0x00, // 85
                    0xFE, 0x00, 0x80, 0x60, 0x18, 0x06, 0x08, 0x10, 0x08, 0x00, 0x00, 0x00, // 86
                    0xFE, 0x80, 0x40, 0x30, 0x0C, 0xFE, 0x80, 0x60, 0x1E, 0x18, 0x08, 0x00, 0x00, 0x00, 0x18, 0x08, 0x00, 0x00, // 87
                    0x00, 0x00, 0x86, 0x78, 0x60, 0x90, 0x08, 0x04, 0x10, 0x08, 0x00, 0x00, 0x00, 0x08, 0x10, 0x00, // 88
                    0x06, 0x38, 0xC0, 0x30, 0x08, 0x04, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, // 89
                    0x00, 0x82, 0x42, 0x22, 0x12, 0x0A, 0x06, 0x18, 0x10, 0x10, 0x10, 0x10, 0x10, 0x00, // 90
                    0x00, 0xF0, 0x0E, 0x02, 0x70, 0x48, 0x00, 0x00, // 91
                    0x06, 0xF8, 0x00, 0x00, 0x00, 0x18, // 92
                    0x00, 0x00, 0xF2, 0x0C, 0x40, 0x78, 0x00, 0x00, // 93
                    0x20, 0x1C, 0x02, 0x1C, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, // 94
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, // 95
                    0x02, 0x04, 0x00, 0x00, // 96
                    0x80, 0x50, 0x48, 0x48, 0xC8, 0x70, 0x08, 0x10, 0x10, 0x08, 0x18, 0x00, // 97
                    0x00, 0xF8, 0x16, 0x08, 0x08, 0x08, 0xF0, 0x18, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 98
                    0xE0, 0x10, 0x08, 0x08, 0x10, 0x08, 0x10, 0x10, 0x08, 0x00, // 99
                    0xE0, 0x10, 0x08, 0x08, 0x10, 0xFF, 0x08, 0x10, 0x10, 0x08, 0x18, 0x00, // 100
                    0xE0, 0x50, 0x48, 0x48, 0x48, 0x70, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 101
                    0x00, 0xF0, 0x1C, 0x14, 0x14, 0x38, 0x00, 0x00, 0x00, 0x00, // 102
                    0x00, 0xE0, 0x10, 0x08, 0x08, 0x90, 0x78, 0x20, 0x48, 0x50, 0x50, 0x50, 0x38, 0x00, // 103
                    0x00, 0xF8, 0x16, 0x08, 0x08, 0xF0, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, // 104
                    0x00, 0xE8, 0x18, 0x00, // 105
                    0x00, 0x80, 0x68, 0x40, 0x38, 0x00, // 106
                    0x80, 0x78, 0x46, 0xE0, 0x10, 0x08, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, // 107
                    0x00, 0xF8, 0x18, 0x00, // 108
                    0x80, 0x78, 0x10, 0x08, 0x08, 0xF0, 0x10, 0x08, 0x08, 0xF0, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, // 109
                    0x00, 0xF8, 0x10, 0x08, 0x08, 0xF0, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, // 110
                    0xE0, 0x10, 0x08, 0x08, 0x08, 0xF0, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 111
                    0x00, 0xC0, 0x38, 0x10, 0x08, 0x08, 0xF0, 0x60, 0x18, 0x08, 0x10, 0x10, 0x08, 0x00, // 112
                    0xE0, 0x10, 0x08, 0x08, 0x08, 0xF0, 0x08, 0x10, 0x10, 0x10, 0x68, 0x18, // 113
                    0x80, 0x7C, 0x10, 0x10, 0x18, 0x00, 0x00, 0x00, // 114
                    0x80, 0x30, 0x28, 0x48, 0xD0, 0x08, 0x10, 0x10, 0x10, 0x08, // 115
                    0xC8, 0x3E, 0x08, 0x18, 0x10, 0x10, // 116
                    0x80, 0x78, 0x00, 0x00, 0x80, 0x78, 0x08, 0x10, 0x10, 0x08, 0x18, 0x00, // 117
                    0x38, 0xC0, 0x00, 0xC0, 0x30, 0x00, 0x18, 0x08, 0x00, 0x00, // 118
                    0x78, 0x80, 0xE0, 0x10, 0xF8, 0x80, 0x78, 0x00, 0x18, 0x00, 0x00, 0x18, 0x08, 0x00, // 119
                    0x00, 0x08, 0xB0, 0x40, 0xA0, 0x10, 0x10, 0x08, 0x00, 0x00, 0x08, 0x10, // 120
                    0x00, 0xF8, 0x00, 0x80, 0x40, 0x30, 0x40, 0x40, 0x38, 0x08, 0x00, 0x00, // 121
                    0x00, 0x08, 0x88, 0x68, 0x18, 0x08, 0x10, 0x18, 0x10, 0x10, 0x10, 0x00, // 122
                    0x40, 0xA0, 0x3E, 0x00, 0x78, 0x40, // 123
                    0xFE, 0x78, // 124
                    0x00, 0x82, 0xBE, 0x40, 0x40, 0x78, 0x00, 0x00, // 125
                    0x60, 0x20, 0x20, 0x40, 0x40, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 126
                    0xFC, 0x04, 0x04, 0x04, 0x04, 0xFC, 0x18, 0x10, 0x10, 0x10, 0x10, 0x18 // 127
            };
        }

    }

    public class SerifRegular extends font{

        public SerifRegular(){
            font_width=10;
            font_height=13;
            first_char = 32;
            char_Count=95;
            chars_width = new int[]{
                    0x00, 0x01, 0x03, 0x06, 0x05, 0x09, 0x09, 0x01, 0x03, 0x03,
                    0x05, 0x07, 0x02, 0x03, 0x01, 0x03, 0x05, 0x03, 0x05, 0x05,
                    0x06, 0x05, 0x05, 0x05, 0x05, 0x05, 0x01, 0x02, 0x06, 0x06,
                    0x06, 0x04, 0x0A, 0x08, 0x07, 0x07, 0x08, 0x07, 0x06, 0x08,
                    0x08, 0x03, 0x04, 0x08, 0x07, 0x0B, 0x08, 0x08, 0x06, 0x08,
                    0x08, 0x05, 0x07, 0x08, 0x07, 0x0B, 0x08, 0x08, 0x07, 0x03,
                    0x03, 0x02, 0x06, 0x06, 0x02, 0x05, 0x05, 0x04, 0x06, 0x04,
                    0x04, 0x05, 0x06, 0x03, 0x02, 0x06, 0x03, 0x09, 0x06, 0x05,
                    0x06, 0x06, 0x04, 0x04, 0x03, 0x06, 0x06, 0x09, 0x05, 0x07,
                    0x05, 0x03, 0x01, 0x03, 0x05, 0x00,
            };
            font = new int[]{
                    0xFE, 0x10, // 33
                    0x0E, 0x00, 0x0E, 0x00, 0x00, 0x00, // 34
                    0xD0, 0x78, 0x56, 0x50, 0xF8, 0x56, 0x18, 0x00, 0x00, 0x18, 0x00, 0x00, // 35
                    0x1C, 0x12, 0xFF, 0x22, 0xC4, 0x08, 0x10, 0x38, 0x10, 0x08, // 36
                    0x0C, 0x12, 0x8C, 0x40, 0x30, 0x08, 0x86, 0x40, 0x80, 0x00, 0x10, 0x08, 0x00, 0x00, 0x00, 0x08, 0x10, 0x08, // 37
                    0xC0, 0x20, 0x3C, 0xD2, 0x8A, 0x46, 0x30, 0x10, 0x00, 0x08, 0x10, 0x10, 0x10, 0x08, 0x10, 0x10, 0x10, 0x08, // 38
                    0x0E, 0x00, // 39
                    0xF0, 0x0C, 0x02, 0x08, 0x30, 0x40, // 40
                    0x02, 0x0C, 0xF0, 0x40, 0x30, 0x08, // 41
                    0x14, 0x08, 0x3E, 0x08, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, // 42
                    0x20, 0x20, 0x20, 0xFC, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, // 43
                    0x00, 0x00, 0x50, 0x30, // 44
                    0x80, 0x80, 0x80, 0x00, 0x00, 0x00, // 45
                    0x00, 0x10, // 46
                    0x00, 0xF8, 0x06, 0x18, 0x00, 0x00, // 47
                    0xFC, 0x02, 0x02, 0x02, 0xFC, 0x08, 0x10, 0x10, 0x10, 0x08, // 48
                    0x04, 0xFE, 0x00, 0x10, 0x18, 0x10, // 49
                    0x04, 0x02, 0x82, 0x42, 0x3C, 0x10, 0x18, 0x10, 0x10, 0x18, // 50
                    0x04, 0x02, 0x22, 0x32, 0xCC, 0x10, 0x10, 0x10, 0x10, 0x08, // 51
                    0xC0, 0xA0, 0x98, 0x84, 0xFE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, // 52
                    0x00, 0x0C, 0x0A, 0x12, 0xE2, 0x10, 0x10, 0x10, 0x08, 0x00, // 53
                    0xF0, 0x28, 0x14, 0x12, 0xE2, 0x08, 0x10, 0x10, 0x10, 0x08, // 54
                    0x04, 0x02, 0x02, 0xF2, 0x0E, 0x00, 0x00, 0x18, 0x00, 0x00, // 55
                    0x8C, 0x52, 0x22, 0x52, 0x8C, 0x08, 0x10, 0x10, 0x10, 0x08, // 56
                    0x3C, 0x42, 0x42, 0xC2, 0x7C, 0x10, 0x10, 0x08, 0x00, 0x00, // 57
                    0x10, 0x10, // 58
                    0x10, 0x00, 0x50, 0x30, // 59
                    0x20, 0x50, 0x50, 0x50, 0x88, 0x88, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 60
                    0x50, 0x50, 0x50, 0x50, 0x50, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 61
                    0x88, 0x88, 0x50, 0x50, 0x50, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 62
                    0x0C, 0xC2, 0x22, 0x1C, 0x00, 0x10, 0x00, 0x00, // 63
                    0xF0, 0x08, 0xE4, 0x12, 0x0A, 0x0A, 0x92, 0x7A, 0x04, 0xF8, 0x18, 0x20, 0x48, 0x90, 0x90, 0x88, 0x88, 0x90, 0x48, 0x20, // 64
                    0x00, 0x80, 0x78, 0x46, 0x58, 0x60, 0x80, 0x00, 0x10, 0x18, 0x10, 0x00, 0x00, 0x10, 0x18, 0x10, // 65
                    0x02, 0xFE, 0x22, 0x22, 0x22, 0x3C, 0xC0, 0x10, 0x18, 0x10, 0x10, 0x10, 0x10, 0x08, // 66
                    0xF8, 0x04, 0x02, 0x02, 0x02, 0x04, 0x8E, 0x00, 0x08, 0x10, 0x10, 0x10, 0x08, 0x00, // 67
                    0x02, 0xFE, 0x02, 0x02, 0x02, 0x02, 0x04, 0xF8, 0x10, 0x18, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 68
                    0x02, 0xFE, 0x22, 0x22, 0x72, 0x06, 0x00, 0x10, 0x18, 0x10, 0x10, 0x10, 0x10, 0x08, // 69
                    0x02, 0xFE, 0x22, 0x22, 0x72, 0x06, 0x10, 0x18, 0x10, 0x00, 0x00, 0x00, // 70
                    0xF8, 0x04, 0x02, 0x02, 0x02, 0x24, 0xEE, 0x20, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 71
                    0x02, 0xFE, 0x22, 0x20, 0x20, 0x22, 0xFE, 0x02, 0x10, 0x18, 0x10, 0x00, 0x00, 0x10, 0x18, 0x10, // 72
                    0x02, 0xFE, 0x02, 0x10, 0x18, 0x10, // 73
                    0x00, 0x02, 0xFE, 0x02, 0x18, 0x10, 0x08, 0x00, // 74
                    0x02, 0xFE, 0x22, 0x50, 0x88, 0x06, 0x02, 0x02, 0x10, 0x18, 0x10, 0x00, 0x00, 0x18, 0x10, 0x10, // 75
                    0x02, 0xFE, 0x02, 0x00, 0x00, 0x00, 0x00, 0x10, 0x18, 0x10, 0x10, 0x10, 0x10, 0x08, // 76
                    0x02, 0xFE, 0x06, 0x18, 0xE0, 0x00, 0xE0, 0x18, 0x06, 0xFE, 0x02, 0x10, 0x18, 0x10, 0x00, 0x00, 0x18, 0x00, 0x00, 0x10, 0x18, 0x10, // 77
                    0x02, 0xFE, 0x08, 0x10, 0x60, 0x82, 0xFE, 0x02, 0x10, 0x18, 0x10, 0x00, 0x00, 0x00, 0x18, 0x00, // 78
                    0xF8, 0x04, 0x02, 0x02, 0x02, 0x02, 0x04, 0xF8, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 79
                    0x02, 0xFE, 0x22, 0x22, 0x22, 0x1C, 0x10, 0x18, 0x10, 0x00, 0x00, 0x00, // 80
                    0xF8, 0x04, 0x02, 0x02, 0x02, 0x02, 0x04, 0xF8, 0x00, 0x08, 0x10, 0x10, 0x30, 0x50, 0x48, 0x40, // 81
                    0x02, 0xFE, 0x22, 0x22, 0xE2, 0x1C, 0x00, 0x00, 0x10, 0x18, 0x10, 0x00, 0x00, 0x08, 0x10, 0x10, // 82
                    0x0C, 0x12, 0x22, 0x22, 0xC6, 0x18, 0x10, 0x10, 0x10, 0x08, // 83
                    0x06, 0x02, 0x02, 0xFE, 0x02, 0x02, 0x06, 0x00, 0x00, 0x10, 0x18, 0x10, 0x00, 0x00, // 84
                    0x02, 0xFE, 0x02, 0x00, 0x00, 0x02, 0xFE, 0x02, 0x00, 0x08, 0x10, 0x10, 0x10, 0x10, 0x08, 0x00, // 85
                    0x02, 0x1E, 0xE2, 0x00, 0xE2, 0x1E, 0x02, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, // 86
                    0x02, 0x1E, 0xE2, 0x80, 0x62, 0x1E, 0xE2, 0x80, 0x72, 0x0E, 0x02, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, // 87
                    0x02, 0x06, 0x8A, 0x70, 0x70, 0x8A, 0x06, 0x02, 0x10, 0x18, 0x10, 0x00, 0x00, 0x10, 0x18, 0x10, // 88
                    0x02, 0x06, 0x3A, 0xC0, 0x30, 0x0A, 0x06, 0x02, 0x00, 0x00, 0x10, 0x18, 0x10, 0x00, 0x00, 0x00, // 89
                    0x00, 0x86, 0x42, 0x32, 0x0A, 0x06, 0x82, 0x10, 0x18, 0x10, 0x10, 0x10, 0x10, 0x18, // 90
                    0xFE, 0x02, 0x02, 0x78, 0x40, 0x40, // 91
                    0x06, 0xF8, 0x00, 0x00, 0x00, 0x18, // 92
                    0x02, 0xFE, 0x40, 0x78, // 93
                    0x10, 0x0C, 0x02, 0x02, 0x0C, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 94
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, // 95
                    0x04, 0x08, 0x00, 0x00, // 96
                    0xA0, 0x50, 0x50, 0xE0, 0x00, 0x08, 0x10, 0x10, 0x18, 0x10, // 97
                    0xFE, 0x20, 0x10, 0x10, 0xE0, 0x08, 0x10, 0x10, 0x10, 0x08, // 98
                    0xE0, 0x10, 0x10, 0x30, 0x08, 0x10, 0x10, 0x08, // 99
                    0xE0, 0x10, 0x10, 0x22, 0xFE, 0x00, 0x08, 0x10, 0x10, 0x08, 0x18, 0x10, // 100
                    0xE0, 0x50, 0x50, 0x60, 0x08, 0x10, 0x10, 0x08, // 101
                    0x10, 0xFC, 0x12, 0x02, 0x10, 0x18, 0x10, 0x00, // 102
                    0x60, 0x90, 0x90, 0x70, 0x10, 0x68, 0x90, 0x90, 0x90, 0x60, // 103
                    0x02, 0xFE, 0x20, 0x10, 0xE0, 0x00, 0x10, 0x18, 0x10, 0x00, 0x18, 0x10, // 104
                    0x10, 0xF2, 0x00, 0x10, 0x18, 0x10, // 105
                    0x10, 0xF2, 0x80, 0x78, // 106
                    0x02, 0xFE, 0x40, 0xB0, 0x10, 0x00, 0x10, 0x18, 0x10, 0x00, 0x18, 0x10, // 107
                    0x02, 0xFE, 0x00, 0x10, 0x18, 0x10, // 108
                    0x10, 0xF0, 0x20, 0x10, 0xE0, 0x20, 0x10, 0xE0, 0x00, 0x10, 0x18, 0x10, 0x00, 0x18, 0x10, 0x00, 0x18, 0x10, // 109
                    0x10, 0xF0, 0x20, 0x10, 0xE0, 0x00, 0x10, 0x18, 0x10, 0x00, 0x18, 0x10, // 110
                    0xE0, 0x10, 0x10, 0x10, 0xE0, 0x08, 0x10, 0x10, 0x10, 0x08, // 111
                    0x10, 0xF0, 0x20, 0x10, 0x10, 0xE0, 0x80, 0xF8, 0x88, 0x10, 0x10, 0x08, // 112
                    0xE0, 0x10, 0x10, 0x20, 0xF0, 0x00, 0x08, 0x10, 0x10, 0x88, 0xF8, 0x80, // 113
                    0x10, 0xF0, 0x20, 0x10, 0x10, 0x18, 0x10, 0x00, // 114
                    0x60, 0x50, 0x90, 0xB0, 0x18, 0x10, 0x10, 0x08, // 115
                    0x10, 0xFC, 0x10, 0x00, 0x18, 0x10, // 116
                    0x10, 0xF0, 0x00, 0x10, 0xF0, 0x00, 0x00, 0x08, 0x10, 0x10, 0x18, 0x10, // 117
                    0x30, 0xD0, 0x00, 0xC0, 0x30, 0x10, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, // 118
                    0x10, 0xF0, 0x00, 0x90, 0xF0, 0x10, 0xC0, 0x30, 0x10, 0x00, 0x00, 0x18, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, // 119
                    0x10, 0x30, 0xC0, 0x30, 0x10, 0x10, 0x18, 0x00, 0x18, 0x10, // 120
                    0x10, 0x70, 0x90, 0x00, 0xD0, 0x30, 0x10, 0x80, 0x80, 0x48, 0x30, 0x08, 0x00, 0x00, // 121
                    0x30, 0x10, 0xD0, 0x30, 0x10, 0x10, 0x18, 0x10, 0x10, 0x18, // 122
                    0x40, 0xBC, 0x02, 0x00, 0x38, 0x40, // 123
                    0xFE, 0x78, // 124
                    0x02, 0xBC, 0x40, 0x40, 0x38, 0x00, // 125
                    0x80, 0x40, 0xC0, 0x80, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, // 126
            };
        }

    }

    public class ComicSans extends font{

        public ComicSans(){
            font_width=10;
            font_height=14;
            first_char = 32;
            char_Count=95;
            chars_width = new int[]{
                    0x00, 0x01, 0x03, 0x09, 0x06, 0x08, 0x06, 0x01, 0x03, 0x03,
                    0x05, 0x05, 0x02, 0x04, 0x01, 0x05, 0x05, 0x03, 0x05, 0x05,
                    0x06, 0x05, 0x05, 0x07, 0x05, 0x05, 0x01, 0x02, 0x04, 0x05,
                    0x03, 0x05, 0x0A, 0x07, 0x05, 0x06, 0x07, 0x06, 0x06, 0x07,
                    0x07, 0x05, 0x07, 0x06, 0x05, 0x09, 0x08, 0x08, 0x04, 0x08,
                    0x05, 0x07, 0x07, 0x07, 0x07, 0x0B, 0x08, 0x07, 0x08, 0x03,
                    0x05, 0x03, 0x05, 0x08, 0x03, 0x06, 0x05, 0x05, 0x05, 0x05,
                    0x05, 0x05, 0x05, 0x01, 0x03, 0x05, 0x01, 0x07, 0x05, 0x05,
                    0x05, 0x05, 0x04, 0x05, 0x05, 0x05, 0x05, 0x07, 0x06, 0x06,
                    0x04, 0x04, 0x01, 0x04, 0x06, 0x05,
            };
            font = new int[]{
                    0xFF, 0x08, // 33
                    0x1E, 0x00, 0x1E, 0x00, 0x00, 0x00, // 34
                    0x80, 0x88, 0xF8, 0x8E, 0x88, 0x88, 0xF8, 0x8E, 0x08, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, // 35
                    0x1C, 0x22, 0xFF, 0x22, 0x22, 0xC2, 0x08, 0x08, 0x3C, 0x08, 0x08, 0x04, // 36
                    0x0C, 0x12, 0xD2, 0x3C, 0x8C, 0x43, 0x40, 0x80, 0x00, 0x0C, 0x04, 0x00, 0x04, 0x08, 0x08, 0x04, // 37
                    0xC0, 0x20, 0x1C, 0x7A, 0x8E, 0xE0, 0x04, 0x08, 0x08, 0x08, 0x04, 0x08, // 38
                    0x1E, 0x00, // 39
                    0xF8, 0x06, 0x01, 0x0C, 0x30, 0x40, // 40
                    0x01, 0x06, 0xF8, 0x40, 0x30, 0x0C, // 41
                    0x04, 0x14, 0x0E, 0x0C, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, // 42
                    0x40, 0x40, 0xF0, 0x40, 0x40, 0x00, 0x00, 0x04, 0x00, 0x00, // 43
                    0x00, 0x00, 0x10, 0x08, // 44
                    0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, // 45
                    0x00, 0x0C, // 46
                    0x00, 0x80, 0x60, 0x18, 0x06, 0x18, 0x04, 0x00, 0x00, 0x00, // 47
                    0xFC, 0x02, 0x02, 0x02, 0xFC, 0x04, 0x08, 0x08, 0x08, 0x04, // 48
                    0x04, 0xFE, 0x00, 0x08, 0x0C, 0x08, // 49
                    0x84, 0x42, 0x22, 0x12, 0x0C, 0x0C, 0x08, 0x08, 0x08, 0x08, // 50
                    0x04, 0x22, 0x22, 0x22, 0xDC, 0x04, 0x08, 0x08, 0x08, 0x04, // 51
                    0x60, 0x50, 0x48, 0x44, 0xFE, 0x40, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x00, // 52
                    0x7E, 0x22, 0x12, 0x12, 0xE2, 0x04, 0x08, 0x08, 0x08, 0x04, // 53
                    0xF0, 0x18, 0x14, 0x12, 0xE0, 0x04, 0x08, 0x08, 0x08, 0x04, // 54
                    0x02, 0x02, 0x82, 0x62, 0x1A, 0x06, 0x02, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00, // 55
                    0xDC, 0x22, 0x22, 0x22, 0xDC, 0x04, 0x08, 0x08, 0x08, 0x04, // 56
                    0x3C, 0x42, 0x42, 0xC2, 0x7C, 0x00, 0x08, 0x04, 0x00, 0x00, // 57
                    0xB0, 0x04, // 58
                    0x00, 0x30, 0x10, 0x08, // 59
                    0x40, 0xE0, 0x20, 0x10, 0x00, 0x00, 0x04, 0x00, // 60
                    0x90, 0x90, 0x90, 0x90, 0x90, 0x00, 0x00, 0x00, 0x00, 0x00, // 61
                    0x18, 0xB0, 0x60, 0x04, 0x00, 0x00, // 62
                    0x02, 0x42, 0x42, 0x22, 0x1C, 0x00, 0x08, 0x00, 0x00, 0x00, // 63
                    0x78, 0x86, 0x32, 0x49, 0x4D, 0x65, 0x31, 0x42, 0x42, 0x3C, 0x00, 0x00, 0x04, 0x08, 0x08, 0x08, 0x08, 0x08, 0x04, 0x00, // 64
                    0x00, 0x80, 0x60, 0x58, 0x46, 0x78, 0x80, 0x08, 0x04, 0x00, 0x00, 0x00, 0x00, 0x0C, // 65
                    0xFE, 0x22, 0x22, 0x62, 0x9C, 0x0C, 0x08, 0x08, 0x08, 0x04, // 66
                    0xE0, 0x18, 0x04, 0x02, 0x02, 0x06, 0x04, 0x08, 0x08, 0x08, 0x08, 0x04, // 67
                    0xFE, 0x02, 0x04, 0x04, 0x08, 0x10, 0xE0, 0x04, 0x08, 0x08, 0x08, 0x08, 0x04, 0x00, // 68
                    0xFE, 0x22, 0x22, 0x22, 0x22, 0x22, 0x04, 0x08, 0x08, 0x08, 0x08, 0x08, // 69
                    0xFE, 0x22, 0x22, 0x22, 0x22, 0x02, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, // 70
                    0xF0, 0x0C, 0x22, 0x22, 0x22, 0x24, 0xE0, 0x04, 0x08, 0x08, 0x08, 0x08, 0x04, 0x00, // 71
                    0xFE, 0x20, 0x20, 0x20, 0x20, 0x20, 0xFE, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C, // 72
                    0x02, 0x02, 0xFE, 0x02, 0x02, 0x08, 0x08, 0x0C, 0x08, 0x08, // 73
                    0x80, 0x00, 0x02, 0x02, 0xFE, 0x02, 0x02, 0x04, 0x08, 0x08, 0x08, 0x0C, 0x00, 0x00, // 74
                    0xFE, 0x70, 0x90, 0x08, 0x04, 0x02, 0x0C, 0x00, 0x00, 0x04, 0x08, 0x08, // 75
                    0xFE, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x08, 0x08, 0x08, 0x08, // 76
                    0x00, 0xF0, 0x0E, 0xF0, 0x00, 0xF0, 0x1E, 0xE0, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x0C, // 77
                    0xFE, 0x04, 0x08, 0x30, 0x40, 0x80, 0x00, 0xFE, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x0C, // 78
                    0xF0, 0x08, 0x04, 0x02, 0x02, 0x02, 0x84, 0x78, 0x00, 0x04, 0x08, 0x08, 0x08, 0x08, 0x04, 0x00, // 79
                    0xFE, 0x42, 0x42, 0x3C, 0x0C, 0x00, 0x00, 0x00, // 80
                    0xF8, 0x04, 0x02, 0x02, 0x82, 0x02, 0x04, 0xF8, 0x00, 0x04, 0x08, 0x08, 0x08, 0x0C, 0x1C, 0x34, // 81
                    0xFE, 0x42, 0xC2, 0x44, 0x38, 0x0C, 0x00, 0x00, 0x04, 0x08, // 82
                    0x00, 0x18, 0x24, 0x22, 0x22, 0x22, 0xC2, 0x04, 0x08, 0x08, 0x08, 0x08, 0x04, 0x00, // 83
                    0x02, 0x02, 0x02, 0xFE, 0x02, 0x02, 0x02, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, // 84
                    0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, 0x00, 0x04, 0x08, 0x08, 0x08, 0x04, 0x00, // 85
                    0x0E, 0x30, 0xC0, 0x00, 0xC0, 0x38, 0x06, 0x00, 0x00, 0x04, 0x0C, 0x04, 0x00, 0x00, // 86
                    0x06, 0xF8, 0x00, 0xC0, 0x30, 0x0E, 0xF0, 0x00, 0xC0, 0x38, 0x06, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x04, 0x08, 0x04, 0x00, 0x00, // 87
                    0x02, 0x04, 0x88, 0x70, 0x70, 0x88, 0x04, 0x02, 0x08, 0x04, 0x00, 0x00, 0x00, 0x00, 0x04, 0x08, // 88
                    0x02, 0x0C, 0x30, 0xC0, 0x60, 0x1C, 0x02, 0x00, 0x00, 0x08, 0x04, 0x00, 0x00, 0x00, // 89
                    0x02, 0x82, 0x42, 0x22, 0x12, 0x0A, 0x06, 0x02, 0x0C, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, // 90
                    0xFF, 0x01, 0x01, 0x7C, 0x40, 0x40, // 91
                    0x06, 0x0C, 0x70, 0x80, 0x00, 0x00, 0x00, 0x00, 0x04, 0x18, // 92
                    0x01, 0x01, 0xFF, 0x40, 0x40, 0x7C, // 93
                    0x04, 0x02, 0x03, 0x02, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, // 94
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, // 95
                    0x01, 0x03, 0x04, 0x00, 0x00, 0x00, // 96
                    0xC0, 0x20, 0x10, 0x10, 0xF0, 0x00, 0x04, 0x08, 0x08, 0x08, 0x04, 0x08, // 97
                    0xFF, 0x10, 0x10, 0x10, 0xE0, 0x0C, 0x08, 0x08, 0x08, 0x04, // 98
                    0xC0, 0x20, 0x10, 0x10, 0x20, 0x04, 0x08, 0x08, 0x08, 0x04, // 99
                    0xE0, 0x10, 0x10, 0x10, 0xFF, 0x04, 0x08, 0x08, 0x08, 0x0C, // 100
                    0xE0, 0x90, 0x90, 0x50, 0x20, 0x04, 0x08, 0x08, 0x08, 0x04, // 101
                    0x10, 0x10, 0xFE, 0x11, 0x11, 0x00, 0x00, 0x0C, 0x00, 0x00, // 102
                    0xC0, 0x20, 0x10, 0x10, 0xE0, 0x44, 0x48, 0x48, 0x48, 0x3C, // 103
                    0xFF, 0x20, 0x10, 0x10, 0xE0, 0x0C, 0x00, 0x00, 0x00, 0x0C, // 104
                    0xF4, 0x0C, // 105
                    0x00, 0x00, 0xF4, 0x80, 0x80, 0x7C, // 106
                    0xFF, 0xC0, 0x60, 0x90, 0x00, 0x0C, 0x00, 0x00, 0x04, 0x08, // 107
                    0xFF, 0x0C, // 108
                    0xF0, 0x20, 0x10, 0xE0, 0x20, 0x10, 0xE0, 0x0C, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x0C, // 109
                    0xF0, 0x20, 0x10, 0x10, 0xE0, 0x0C, 0x00, 0x00, 0x00, 0x0C, // 110
                    0xE0, 0x10, 0x10, 0x10, 0xE0, 0x04, 0x08, 0x08, 0x08, 0x04, // 111
                    0xF0, 0x10, 0x10, 0x10, 0xE0, 0x7C, 0x08, 0x08, 0x08, 0x04, // 112
                    0xC0, 0x20, 0x10, 0x10, 0xF0, 0x04, 0x08, 0x08, 0x08, 0x7C, // 113
                    0xF0, 0x20, 0x10, 0x70, 0x0C, 0x00, 0x00, 0x00, // 114
                    0x20, 0x50, 0x50, 0x90, 0x30, 0x08, 0x08, 0x08, 0x08, 0x04, // 115
                    0x10, 0x10, 0xFC, 0x10, 0x10, 0x00, 0x00, 0x0C, 0x00, 0x00, // 116
                    0xF0, 0x00, 0x00, 0x00, 0xF0, 0x04, 0x08, 0x08, 0x08, 0x0C, // 117
                    0x30, 0xC0, 0x00, 0xC0, 0x30, 0x00, 0x00, 0x0C, 0x00, 0x00, // 118
                    0xF0, 0x00, 0xC0, 0xF0, 0x00, 0xC0, 0x30, 0x00, 0x0C, 0x00, 0x00, 0x0C, 0x04, 0x00, // 119
                    0x10, 0x20, 0xC0, 0xC0, 0x20, 0x10, 0x08, 0x04, 0x00, 0x00, 0x04, 0x08, // 120
                    0x10, 0x60, 0x80, 0x80, 0x60, 0x10, 0x00, 0x60, 0x1C, 0x04, 0x00, 0x00, // 121
                    0x10, 0x90, 0x50, 0x30, 0x0C, 0x08, 0x08, 0x08, // 122
                    0xC0, 0xFE, 0x01, 0x01, 0x00, 0x3C, 0x40, 0x40, // 123
                    0xFF, 0x3C, // 124
                    0x01, 0x01, 0xFE, 0xC0, 0x40, 0x40, 0x3C, 0x00, // 125
                    0x60, 0x30, 0x30, 0x40, 0x40, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 126
                    0xFF, 0x01, 0x01, 0x01, 0xFF, 0x0C, 0x08, 0x08, 0x08, 0x0C // 127
            };
        }

    }
}
