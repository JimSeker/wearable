/*
 * Copyright (C) 2014 The Android Open Source Project
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

package edu.cs4730.tapeventsdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.TimeZone;

/**
 * this is a striped down example to demo Tap Events.  It will draw a rectangle with a number
 * that indicates the number of taps in/on the rectangle.
 *
 * This code does not attempt to detect if tap events are available.  just uses them if there are.
 *  see http://android-developers.blogspot.com/2015/08/interactive-watch-faces-with-latest.html
 *
 */
public class MyTapFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    String TAG = "TapDemo";
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {


        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mTextPaint;


        boolean mAmbient;

        Time mTime;

        float mXOffset;
        float mYOffset;

        //MyRec for the tap demo
        Rect myRec;
        int mTapNumber =0, strokewidth=5;
        Paint mRecPaint;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyTapFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)  //turn on the tap events.
                    .build());
            Resources resources = MyTapFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.digital_background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            //for rec painting, mostly the same as text, except for fill/stroke
            mRecPaint = new Paint();
            mRecPaint = createTextPaint(resources.getColor(R.color.digital_text));
            mRecPaint.setStyle(Paint.Style.STROKE);
            mRecPaint.setStrokeWidth(strokewidth);  //set the line size to be 5 "pixels".

            mTime = new Time();
            //initialize the rectangle for later use.
            myRec = new Rect();

        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyTapFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyTapFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyTapFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                     mRecPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Draw H:MM in all modes.
            mTime.setToNow();
            String text = String.format("%d:%02d", mTime.hour, mTime.minute);
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            //there should be a better way, then re computing this every time, but I need
            //to know where I draw the rectangle at for the tap.  It's 80 accross.
            //set left,top, right, bottom
            int left = (bounds.width()/2) -40;
            int bottom = bounds.bottom - strokewidth; //stroke width is 5!
            int top = bottom -80;
            myRec.set(left,top,left+80,bottom);
            canvas.drawRect(myRec, mRecPaint);
            //now draw the tap number.
            canvas.drawText(String.valueOf(mTapNumber), left + strokewidth, bottom-strokewidth,mTextPaint);

        }

        /*
        * the part of this example we care about here.   Tap Events!
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {


            switch(tapType) {
                case TAP_TYPE_TOUCH:
                    Log.v(TAG, "Tap Touch");
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    Log.v(TAG, "Tap Touch Cancel");
                    break;
                case TAP_TYPE_TAP:

                    if (myRec.contains(x,y)) {
                        mTapNumber++;
                        invalidate();
                        Log.v(TAG, "Tap inside");
                    } else {
                        Log.v(TAG, "Tap outside");
                    }
                    break;
            }
        }


    }
}
