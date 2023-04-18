package edu.cs4730.mywatchface;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

import androidx.annotation.NonNull;
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


/**
 * this is the most striped down version of the watchfaceservice.
 * since it doesn't even show the time, it only updates on the default.
 * it deals with everything necessary, such as ambient and timezone changes.
 * <p>
 * Need to learn how to use the userstye part, so beertime interactive would
 * be different colors.
 */

public class beerWatchFaceService extends WatchFaceService {
    private static final String TAG = "BeerFaceService";
    private static final Typeface BOLD_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
        Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Nullable
    @Override
    protected WatchFace createWatchFace(
        @NonNull SurfaceHolder surfaceHolder,
        @NonNull WatchState watchState,
        @NonNull ComplicationSlotsManager complicationSlotsManager,
        @NonNull CurrentUserStyleRepository currentUserStyleRepository,
        @NonNull Continuation<? super WatchFace> continuation) {

        return new WatchFace(
            WatchFaceType.DIGITAL,
            new beerCanvasRender(getApplicationContext(), surfaceHolder, watchState, complicationSlotsManager, currentUserStyleRepository, CanvasType.HARDWARE)
        );
    }


    class beerCanvasRender extends Renderer.CanvasRenderer2<beerCanvasRender.beerShareAssets> {
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


        @Override
        public void render(@NonNull Canvas canvas, @NonNull Rect bounds, @NonNull ZonedDateTime zonedDateTime, @NonNull beerShareAssets beerShareAssets) {

            RenderParameters renderParameters = getRenderParameters();

            if (renderParameters.getDrawMode() == DrawMode.AMBIENT) {
                //Log.d("mode ", "ambient");
                //we are in ambient mode, so switch from yellow to gray and aliasing.
                mtextPaint.setColor(Color.GRAY);
                mtextPaint.setAntiAlias(false);
                mtextPaint.setTypeface(NORMAL_TYPEFACE);
                //mBackgroundPaint.setColor(Color.BLACK);
            } else if (renderParameters.getDrawMode() == DrawMode.INTERACTIVE) {
                //Log.d("mode ", "interact");
                //none ambient mode, so back to Yellow!
                mtextPaint.setColor(Color.YELLOW);
                mtextPaint.setAntiAlias(true);
                mtextPaint.setTypeface(BOLD_TYPEFACE);
                //mBackgroundPaint.setColor(Color.BLACK);

            } else {
                //Log.d("mode ", "something? " + renderParameters.getDrawMode());
                //we are in ambient mode, so switch from yellow to gray and aliasing.
                mtextPaint.setColor(Color.GRAY);
                mtextPaint.setAntiAlias(false);
                mtextPaint.setTypeface(NORMAL_TYPEFACE);
                //mBackgroundPaint.setColor(Color.BLACK);
            }

            //            /* draw your watch face */
            String Beer = "Beer Time!";
            float x = 0, y = 0;
            x = bounds.width() / 2 - mtextPaint.measureText(Beer) / 2;
            y = bounds.height() / 2;
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            canvas.drawText("Beer Time", x, y, mtextPaint);
        }

        @Override
        public void renderHighlightLayer(@NonNull Canvas canvas, @NonNull Rect rect, @NonNull ZonedDateTime zonedDateTime, @NonNull beerShareAssets beerShareAssets) {

        }

        class beerShareAssets implements SharedAssets {
            @Override
            public void onDestroy() {

            }

        }

        @Nullable
        @Override
        public beerShareAssets createSharedAssets(@NonNull Continuation completion) {
            return new beerCanvasRender.beerShareAssets();
        }

        public beerCanvasRender(@NonNull Context mcontext, @NonNull SurfaceHolder msurfaceHolder, @NonNull WatchState mwatchState, @NonNull ComplicationSlotsManager mcomplicationSlotsManager, @NonNull final CurrentUserStyleRepository mcurrentUserStyleRepository, int mcanvasType) {
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

            mtextPaint = new Paint();
            mtextPaint.setColor(Color.YELLOW);
            mtextPaint.setAntiAlias(true);
            mtextPaint.setTypeface(BOLD_TYPEFACE);  //which font.
            //mtextPaint.setTextSize(resources.getDimension(R.dimen.digital_text_size));
            mtextPaint.setTextSize(45);


        }

    }


}