package com.integreight.onesheeld.shields.fragments.sub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdHelperView extends View{

    private Bitmap bitmap;

    public GlcdHelperView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //if (bitmap != null)
        //    canvas.drawBitmap(bitmap,0,0,new Paint());
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);
        canvas.drawRect(5,5,55,55,paint);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
