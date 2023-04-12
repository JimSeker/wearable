package edu.cs4730.mywatchface;


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.content.Context;
import android.view.SurfaceHolder;

import java.time.ZonedDateTime;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.wear.watchface.CanvasType;
import androidx.wear.watchface.ComplicationSlotsManager;
import androidx.wear.watchface.DrawMode;
import androidx.wear.watchface.RenderParameters;
import androidx.wear.watchface.Renderer;
import androidx.wear.watchface.WatchFace;
import androidx.wear.watchface.WatchFaceService;
import androidx.wear.watchface.WatchFaceType;
import androidx.wear.watchface.WatchState;
import androidx.wear.watchface.style.CurrentUserStyleRepository;
import kotlin.coroutines.Continuation;

import static android.graphics.Color.*;

/**
 * this code is based off google's original example, but updated to the point that it is likely
 * no longer recognizable.
 */

public class myWatchFaceService extends WatchFaceService {

    private static final String TAG = "myWatchFaceService";

    private static final Typeface BOLD_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Nullable
    @Override
    protected WatchFace createWatchFace(@NonNull SurfaceHolder surfaceHolder,
                                        @NonNull WatchState watchState,
                                        @NonNull ComplicationSlotsManager complicationSlotsManager,
                                        @NonNull CurrentUserStyleRepository currentUserStyleRepository,
                                        @NonNull Continuation<? super WatchFace> continuation) {


        return new WatchFace(
            WatchFaceType.DIGITAL,
            new myWatchFaceService.myCanvasRender(getApplicationContext(), surfaceHolder, watchState, complicationSlotsManager, currentUserStyleRepository, CanvasType.HARDWARE)
        );

    }

    class myCanvasRender extends Renderer.CanvasRenderer2<myWatchFaceService.myCanvasRender.myShareAssets> {
        static private final long FRAME_PERIOD_MS_DEFAULT = 16L;
        final boolean clearWithBackgroundTintBeforeRenderingHighlightLayer = false;

        Context context;
        SurfaceHolder surfaceHolder;
        WatchState watchState;
        ComplicationSlotsManager complicationSlotsManager;
        CurrentUserStyleRepository currentUserStyleRepository;
        int canvasType;

        Paint mBackgroundPaint;
        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mSecondPaint;
        Paint mAmPmPaint;
        Paint mColonPaint;
        float mColonWidth;

        Calendar mTime;
        boolean mShouldDrawColons;
        float mXOffset;
        float mYOffset;
        String mAmString;
        String mPmString;
        int mInteractiveBackgroundColor = Color.BLACK;//parseColor("black");
        int mInteractiveHourDigitsColor = parseColor("white");
        int mInteractiveMinuteDigitsColor = parseColor("white");
        int mInteractiveSecondDigitsColor = parseColor("gray");
        boolean mAmbient;

        static final String COLON_STRING = ":";

        @Override
        public void render(@NonNull Canvas canvas, @NonNull Rect bounds, @NonNull ZonedDateTime zonedDateTime, @NonNull myWatchFaceService.myCanvasRender.myShareAssets myShareAssets) {

            RenderParameters renderParameters = getRenderParameters();
            mAmbient = renderParameters.getDrawMode() == DrawMode.AMBIENT;
            // Calendar cal = new Calendar(getSystemTimeProvider().getSystemTimeMillis());
            mTime = Calendar.getInstance();

            /* draw your watch face */

            // Show colons for the first half of each second so the colons blink on when the time
            // updates.
            mShouldDrawColons = (System.currentTimeMillis() % 1000) < 500;

            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Draw the hours.
            float x = mXOffset;
            String hourString = String.valueOf(convertTo12Hour(mTime.get(Calendar.HOUR)));
            canvas.drawText(hourString, x, mYOffset, mHourPaint);
            x += mHourPaint.measureText(hourString);


            // In ambient and mute modes, always draw the first colon. Otherwise, draw the
            // first colon for the first half of each second.
            if (mAmbient || mShouldDrawColons) {
                canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
            }
            x += mColonWidth;

            // Draw the minutes.
            String minuteString = formatTwoDigitNumber(mTime.get(Calendar.MINUTE));
            canvas.drawText(minuteString, x, mYOffset, mMinutePaint);
            x += mMinutePaint.measureText(minuteString);

            // In ambient and mute modes, draw AM/PM. Otherwise, draw a second blinking
            // colon followed by the seconds.
            if (mAmbient) {
                x += mColonWidth;
                canvas.drawText(getAmPmString(mTime.get(Calendar.HOUR)), x, mYOffset, mAmPmPaint);
            } else {
                if (mShouldDrawColons) {
                    canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
                }
                x += mColonWidth;
                canvas.drawText(formatTwoDigitNumber(mTime.get(Calendar.SECOND)), x, mYOffset,
                    mSecondPaint);

            }
        }


        @Override
        public void renderHighlightLayer(@NonNull Canvas canvas, @NonNull Rect rect, @NonNull ZonedDateTime zonedDateTime, @NonNull myShareAssets myShareAssets) {
            ///what does this do?? no documentation as to it use.

        }

        class myShareAssets implements SharedAssets {
            @Override
            public void onDestroy() {

            }

        }

        @Nullable
        @Override
        public myWatchFaceService.myCanvasRender.myShareAssets createSharedAssets(@NonNull Continuation completion) {
            return new myWatchFaceService.myCanvasRender.myShareAssets();
        }

        private Paint createTextPaint(int defaultInteractiveColor) {
            return createTextPaint(defaultInteractiveColor, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(defaultInteractiveColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @SuppressLint("DefaultLocale")
        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private int convertTo12Hour(int hour) {
            int result = hour % 12;
            return (result == 0) ? 12 : result;
        }

        private String getAmPmString(int hour) {
            return (hour < 12) ? mAmString : mPmString;
        }

        public myCanvasRender(@NonNull Context mcontext, @NonNull SurfaceHolder msurfaceHolder, @NonNull WatchState mwatchState, @NonNull ComplicationSlotsManager mcomplicationSlotsManager, @NonNull final CurrentUserStyleRepository mcurrentUserStyleRepository, int mcanvasType) {
            super(msurfaceHolder, mcurrentUserStyleRepository, mwatchState, mcanvasType, FRAME_PERIOD_MS_DEFAULT, false);

            context = mcontext;
            surfaceHolder = msurfaceHolder;
            watchState = mwatchState;
            complicationSlotsManager = mcomplicationSlotsManager;
            currentUserStyleRepository = mcurrentUserStyleRepository;
            canvasType = mcanvasType;

            //setup initial Paint colors.
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setAntiAlias(true);

            Resources resources = myWatchFaceService.this.getResources();
            //setup variables.
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mAmString = resources.getString(R.string.digital_am);
            mPmString = resources.getString(R.string.digital_pm);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);
            mHourPaint = createTextPaint(mInteractiveHourDigitsColor, BOLD_TYPEFACE);
            mMinutePaint = createTextPaint(mInteractiveMinuteDigitsColor);
            mSecondPaint = createTextPaint(mInteractiveSecondDigitsColor);
            mAmPmPaint = createTextPaint(ContextCompat.getColor(getApplicationContext(), R.color.digital_am_pm));
            mColonPaint = createTextPaint(ContextCompat.getColor(getApplicationContext(), R.color.digital_colons));


            surfaceHolder.getSurfaceFrame();

            boolean isRound = true;  //how determine it's round or not?
            mXOffset = resources.getDimension(isRound ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float amPmSize = resources.getDimension(isRound ? R.dimen.digital_am_pm_size_round : R.dimen.digital_am_pm_size);

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mSecondPaint.setTextSize(textSize);
            mAmPmPaint.setTextSize(amPmSize);
            mColonPaint.setTextSize(textSize);

            mColonWidth = mColonPaint.measureText(COLON_STRING);

        }
    }


}
