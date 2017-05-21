/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.integreight.onesheeld.utils.customviews.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    int previewWidth = 0;
    int previewHeight = 0;
    boolean isBack = true;
    int faceRotation;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }

    public int getId() {
        return mFaceId;
    }

    public void setCameraInfo(int previewWidth, int previewHeight, boolean isBack, int faceRotation) {
        this.isBack = isBack;
        this.previewHeight = previewHeight;
        this.previewWidth = previewWidth;
        this.faceRotation = faceRotation;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float tmp = 0;
        // Draws a bounding box around the face.
        float left = 0, top = 0, right = 0, bottom = 0;
        switch (faceRotation) {
            case 0: {
                if (!isBack)
                    x = previewHeight - x;
                tmp = y;
                y = x;
                x = previewWidth - tmp;
                left = x - yOffset;
                top = y - xOffset;
                right = x + yOffset;
                bottom = y + xOffset;
                break;
            }
            case 1: {
                if (!isBack)
                    x = previewWidth - x;
                left = x - xOffset;
                top = y - yOffset;
                right = x + xOffset;
                bottom = y + yOffset;
                break;
            }
            case 2: {
                if (!isBack)
                    x = previewHeight - x;
                tmp = x;
                x = y;
                y = previewHeight - tmp;
                left = x - yOffset;
                top = y - xOffset;
                right = x + yOffset;
                bottom = y + xOffset;
                break;
            }
            case 3: {
                if (isBack)
                    x = previewWidth - x;
                y = previewHeight - y;
                left = x - xOffset;
                top = y - yOffset;
                right = x + xOffset;
                bottom = y + yOffset;
                break;
            }
        }


        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
