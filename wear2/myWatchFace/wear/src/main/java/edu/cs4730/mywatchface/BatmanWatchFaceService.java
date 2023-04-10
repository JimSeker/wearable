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

package edu.cs4730.mywatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * <p>
 * remember for emulators, first turn debugging on in the emulator and then
 * adb -d forward tcp:5601 tcp:5601   and then the real phone can connect to an emulated device.
 */
public class BatmanWatchFaceService extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        /**
         * Handler to update the time periodically in interactive mode.
         */
        final Handler mUpdateTimeHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
                return true;
            }
        });

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mTextPaint_time, mTextPaint_date;

        boolean mAmbient;

        //Time mTime;
        private Calendar mCalendar;

        Bitmap bm_c, bm_bw;
        float mXOffset;
        float mYOffset;
        int date_height, date_width, time_height, time_width, time_height_amb, time_width_amb;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            //see https://developer.android.com/reference/android/support/wearable/watchface/WatchFaceStyle.Builder.html for more info on the methods use
            //in the next command
            setWatchFaceStyle(new WatchFaceStyle.Builder(BatmanWatchFaceService.this)
                .setStatusBarGravity(Gravity.TOP | Gravity.END) //where the battery and connect icons shows.
                .build());
            Resources resources = BatmanWatchFaceService.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            bm_c = BitmapFactory.decodeResource(resources, R.drawable.batman2c);
            bm_bw = BitmapFactory.decodeResource(resources, R.drawable.batman2bw);
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.digital_background));

            mTextPaint_time = new Paint();
            mTextPaint_time = createTextPaint(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));
            mTextPaint_date = new Paint();
            mTextPaint_date = createTextPaint(ContextCompat.getColor(getApplicationContext(), R.color.digital_text));

            mCalendar = Calendar.getInstance();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
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
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            BatmanWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            BatmanWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = BatmanWatchFaceService.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            //time size
            float textSize = resources.getDimension(isRound
                //        ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
                ? R.dimen.date_text_size_round : R.dimen.date_text_size);
            //date size.
            float datetextSize = resources.getDimension(isRound
                ? R.dimen.date_text_size_round : R.dimen.date_text_size);

            mTextPaint_date.setTextSize(datetextSize);
            mTextPaint_time.setTextSize(textSize);
            //setup where everything goes.

            Rect bounds = new Rect();

            String text = "13:40:45";  //sample time
            mTextPaint_time.getTextBounds(text, 0, text.length(), bounds);
            time_height = bounds.height();

            time_width = bounds.width();

            text = "13:40";  //sample date ambient
            mTextPaint_time.getTextBounds(text, 0, text.length(), bounds);
            time_width_amb = bounds.width();
            time_height_amb = bounds.height();

            text = "02/27 Mon";  //sample date
            mTextPaint_date.getTextBounds(text, 0, text.length(), bounds);
            date_height = bounds.height();
            date_width = bounds.width();


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
                    mTextPaint_date.setAntiAlias(!inAmbientMode);
                    mTextPaint_time.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            int center_x, center_y;
            center_x = bounds.width() / 2;
            center_y = bounds.height() / 2;


            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            String text = mAmbient
                ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE))
                : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
            //to deal with the change in digits (and maybe at some point am/pm too).
            Rect mybounds = new Rect();
            mTextPaint_time.getTextBounds(text, 0, text.length(), mybounds);
            time_height = mybounds.height();
            time_width = mybounds.width();

            //canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            //http://man7.org/linux/man-pages/man3/strftime.3.html for the format command info.
            String Date = String.format("%d/%d/%d", mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), mCalendar.get(Calendar.YEAR));
            //mTime.format("%m/%d %a");
            //canvas.drawText(Date, center_x - (date_width/2), center_y - 55 , mTextPaint_date);
            canvas.drawText(text, center_x - (time_width / 2), center_y - 55, mTextPaint_time);
            //draw picture, change if ambient to black and white version.
            if (mAmbient) {
                canvas.drawBitmap(bm_bw, center_x - 84, center_y - 50, mBackgroundPaint);
                //canvas.drawText(text, center_x -(time_width_amb/2), center_y + 55+ time_height_amb, mTextPaint_time);
            } else {
                canvas.drawBitmap(bm_c, center_x - 84, center_y - 50, mBackgroundPaint);
                //canvas.drawText(text, center_x -(time_width/2), center_y + 55+ time_height, mTextPaint_time);
            }
            //canvas.drawText(text, center_x -(time_width/2), center_y + 55+ time_height, mTextPaint_time);
            canvas.drawText(Date, center_x - (date_width / 2), center_y + 55 + date_height, mTextPaint_date);

        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
