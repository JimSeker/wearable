package edu.cs4730.mywatchface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * <p>
 * remember for emulators, first turn debugging on in the emulator and then
 * adb -d forward tcp:5601 tcp:5601   and then the real phone can connect to an emulated device.
 */
public class BatmanWatchFaceService extends WatchFaceService {

    private static final Typeface BOLD_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    Paint mBackgroundPaint;
    Paint mTextPaint_time, mTextPaint_date;

    boolean mAmbient;

    //Time mTime;
    private Calendar mCalendar;

    Bitmap bm_c, bm_bw;
    float mXOffset;
    float mYOffset;
    int date_height, date_width, time_height, time_width, time_height_amb, time_width_amb;


    @Nullable
    @Override
    protected WatchFace createWatchFace(@NonNull SurfaceHolder surfaceHolder,
                                        @NonNull WatchState watchState,
                                        @NonNull ComplicationSlotsManager complicationSlotsManager,
                                        @NonNull CurrentUserStyleRepository currentUserStyleRepository,
                                        @NonNull Continuation<? super WatchFace> continuation) {


        return new WatchFace(
            WatchFaceType.DIGITAL,
            new BatmanWatchFaceService.batmanCanvasRender(getApplicationContext(), surfaceHolder, watchState, complicationSlotsManager, currentUserStyleRepository, CanvasType.HARDWARE)
        );
    }

    class batmanCanvasRender extends Renderer.CanvasRenderer2<batmanCanvasRender.batmanShareAssets> {
        static private final long FRAME_PERIOD_MS_DEFAULT = 16L;
        final boolean clearWithBackgroundTintBeforeRenderingHighlightLayer = false;

        Context context;
        SurfaceHolder surfaceHolder;
        WatchState watchState;
        ComplicationSlotsManager complicationSlotsManager;
        CurrentUserStyleRepository currentUserStyleRepository;
        int canvasType;
        Paint mBackgroundPaint;
        Paint mtextPaint;

        @SuppressLint("DefaultLocale")
        @Override
        public void render(@NonNull Canvas canvas, @NonNull Rect bounds, @NonNull ZonedDateTime zonedDateTime, @NonNull batmanShareAssets batmanShareAssets) {

            RenderParameters renderParameters = getRenderParameters();
            mCalendar = Calendar.getInstance();
            mAmbient = renderParameters.getDrawMode() == DrawMode.AMBIENT;

            //            /* draw your watch face */
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
            canvas.drawText(text, center_x - (time_width / 2f), center_y - 55, mTextPaint_time);
            //draw picture, change if ambient to black and white version.
            if (mAmbient) {
                canvas.drawBitmap(bm_bw, center_x - 84, center_y - 50, mBackgroundPaint);
                //canvas.drawText(text, center_x -(time_width_amb/2), center_y + 55+ time_height_amb, mTextPaint_time);
            } else {
                canvas.drawBitmap(bm_c, center_x - 84, center_y - 50, mBackgroundPaint);
                //canvas.drawText(text, center_x -(time_width/2), center_y + 55+ time_height, mTextPaint_time);
            }
            //canvas.drawText(text, center_x -(time_width/2), center_y + 55+ time_height, mTextPaint_time);
            canvas.drawText(Date, center_x - (date_width / 2f), center_y + 55 + date_height, mTextPaint_date);
        }

        @Override
        public void renderHighlightLayer(@NonNull Canvas canvas, @NonNull Rect rect, @NonNull ZonedDateTime zonedDateTime, @NonNull batmanShareAssets batmanShareAssets) {

        }


        class batmanShareAssets implements SharedAssets {
            @Override
            public void onDestroy() {

            }

        }


        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public batmanShareAssets createSharedAssets(@NonNull Continuation completion) {
            return new batmanCanvasRender.batmanShareAssets();
        }

        public batmanCanvasRender(@NonNull Context mcontext, @NonNull SurfaceHolder msurfaceHolder, @NonNull WatchState mwatchState, @NonNull ComplicationSlotsManager mcomplicationSlotsManager, @NonNull final CurrentUserStyleRepository mcurrentUserStyleRepository, int mcanvasType) {
            super(msurfaceHolder, mcurrentUserStyleRepository, mwatchState, mcanvasType, FRAME_PERIOD_MS_DEFAULT, false);

            context = mcontext;
            surfaceHolder = msurfaceHolder;
            watchState = mwatchState;
            complicationSlotsManager = mcomplicationSlotsManager;
            currentUserStyleRepository = mcurrentUserStyleRepository;
            canvasType = mcanvasType;

            //setup initial Paint colors.
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


            boolean isRound = true;  //how to check for round???
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

    }
}
