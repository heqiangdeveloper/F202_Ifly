package com.chinatsp.ifly.framesurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;


public class FrameSurfaceView extends BaseSurfaceView {
    private static final String TAG = "FrameSurfaceView";
    private boolean isLoop;

    /**
     * the resources of frame animation
     */
    private List<Integer> bitmapIds = new ArrayList<>();
    /**
     * the index of frame which is drawing
     */
    private int frameIndex = 0;

    private Paint paint = new Paint();
    private Rect srcRect;
    private Rect dstRect = new Rect();
    private int defaultWidth = 0;
    private int defaultHeight;

    private boolean isEnd = false;
    private Bitmap mBitmap;


    public FrameSurfaceView(Context context) {
        super(context);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnFrameAnimationListener mAnimationListener;

    public interface OnFrameAnimationListener {
        /**
         * 动画开始播放后调用
         */
        void onStart();

        /**
         * 动画结束播放后调用
         */
        void onEnd();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        dstRect.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected int getDefaultWidth() {
        return defaultWidth;
    }

    @Override
    protected int getDefaultHeight() {
        return defaultHeight;
    }

    @Override
    protected void onFrameDrawFinish() {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    /**
     * set the duration of frame animation
     *
     * @param duration time in milliseconds
     */
    public void setDuration(int duration) {
        int frameDuration = duration / bitmapIds.size();
        setFrameDuration(frameDuration);
    }

    public void setLoop(boolean loop){
        isLoop = loop;
    }


    public void start() {
        isEnd = false;
        frameIndex = 0;
        if (mAnimationListener != null) {
            mAnimationListener.onStart();
        }

    }


    public void setBitmapIds(List<Integer> bitmapIds,OnFrameAnimationListener listener) {
        if (bitmapIds == null || bitmapIds.size() == 0) {
            return;
        }
        mAnimationListener = listener;
        this.bitmapIds = bitmapIds;
        if(defaultWidth==0)  //避免重新绘制
           getBitmapDimension(bitmapIds.get(0));
    }


    @Override
    protected boolean onFrameDraw(Canvas canvas) {
        try {
            clearCanvas(canvas);
            mBitmap = BitmapFactory.decodeResource(getResources(),bitmapIds.get(frameIndex));
            if(mBitmap==null||mBitmap.isRecycled()) return true;
            canvas.drawBitmap(mBitmap, srcRect, dstRect, paint);
            if (frameIndex == bitmapIds.size() - 1) {
                if (!isLoop){
                    frameIndex = bitmapIds.size() - 1;
                    if (mAnimationListener != null) {
                        mAnimationListener.onEnd();
                    }
                }else
                    frameIndex = 0;
            }else
                frameIndex++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }



    private void clearCanvas(Canvas canvas) {
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void getBitmapDimension(int bitmapId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(this.getResources(), bitmapId, options);
        defaultWidth = options.outWidth;
        defaultHeight = options.outHeight;
        srcRect = new Rect(0, 0, defaultWidth, defaultHeight);
        //we have to re-measure to make defaultWidth in use in onMeasure()
        requestLayout();
    }



}
