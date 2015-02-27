package edu.cs4730.mywatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;

/*
 *  this is the most striped down version of the watchfaceservice.
 *  since it doesn't even show the time, it only updates on the default.
 *  it deals with everything necessary, such as ambient and timezone changes.
 */

public class beerWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "BeerFaceService";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }


    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {

        boolean mLowBitAmbient, mBurnInProt;
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mtextPaint;

        //we need a broadcast receiver to deal with a timezone change.
        final BroadcastReceiver mTimeZoneReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Even in a new timezone, it's Still BEER TIME!  so do nothing.
                //mTime.clear(intent.getStringExtra("time-zone"));
                //mTime.setToNow();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            /* initialize your watch face */

            setWatchFaceStyle(new WatchFaceStyle.Builder(beerWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)  //we are NOT showing the actual time
                    .setStatusBarGravity(Gravity.TOP | Gravity.RIGHT) //where the battery and connect icons shows.
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)  //where 'OK google' shows
                    .build());
            //setup initial Paint colors.
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setAntiAlias(true);

            mtextPaint = new Paint();
            mtextPaint.setColor(Color.YELLOW);
            mtextPaint.setAntiAlias(true);
            mtextPaint.setTypeface(BOLD_TYPEFACE);  //which font.
            //mtextPaint.setTextSize(resources.getDimension(R.dimen.digital_text_size));
            mtextPaint.setTextSize(45);

        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProt = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
            }

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /* the time changed by 1 minute which is the default*/
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
            invalidate();  //but honesty, no necessary it doesn't change...
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
            //so
            mLowBitAmbient = inAmbientMode ? true: false;

            if (mLowBitAmbient) {
                //we are in ambient mode, so switch from yellow to gray and aliasing.
                mtextPaint.setColor(Color.GRAY);
                mtextPaint.setAntiAlias(false);
                mtextPaint.setTypeface(NORMAL_TYPEFACE);
                //mBackgroundPaint.setColor(Color.BLACK);

            } else {
               //none ambient mode, so back to Yellow!
                mtextPaint.setColor(Color.YELLOW);
                mtextPaint.setAntiAlias(true);
                mtextPaint.setTypeface(BOLD_TYPEFACE);
                //mBackgroundPaint.setColor(Color.BLACK);
            }
            invalidate();  //redraw now!
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /* draw your watch face */
            String Beer = "Beer Time!";

            float x = 0, y = 0;
            x = bounds.width()/2 - mtextPaint.measureText(Beer)/2;
            y = bounds.height()/2;
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            canvas.drawText("Beer Time", x,y, mtextPaint);
        }



        //basically required code for the timezone change, which in this case, we don't care.
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }

            if (visible) {
                registerReceiver();

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
            beerWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            beerWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }
    }
}
