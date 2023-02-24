package com.chinatsp.ifly.framesurfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final int DEFAULT_FRAME_DURATION_MILLISECOND = 50;

    private HandlerThread handlerThread;
    private SurfaceViewHandler handler;
    protected int frameDuration = DEFAULT_FRAME_DURATION_MILLISECOND;
    private Canvas canvas;
    private boolean isAlive;
    private DrawRunnable mDrawRunnable;

    public BaseSurfaceView(Context context) {
        super(context);
        init();
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected int getFrameDuration() {
        return frameDuration;
    }

    protected void setFrameDuration(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    protected void init() {
        getHolder().addCallback(this);
        setBackgroundTransparent();
    }

    private void setBackgroundTransparent() {
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isAlive = true;
        startDrawThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
        isAlive = false;
    }

    private void stopDrawThread() {
        handlerThread.quit();
        handler = null;
    }

    private void startDrawThread() {
        handlerThread = new HandlerThread("SurfaceViewThread");
        handlerThread.start();
        handler = new SurfaceViewHandler(handlerThread.getLooper());
        mDrawRunnable = new DrawRunnable("origin");
//        handler.post(new DrawRunnable());
        handler.post(mDrawRunnable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int originWidth = getMeasuredWidth();
        int originHeight = getMeasuredHeight();
        int width = widthMode == MeasureSpec.AT_MOST ? getDefaultWidth() : originWidth;
        int height = heightMode == MeasureSpec.AT_MOST ? getDefaultHeight() : originHeight;
        setMeasuredDimension(width, height);
    }

    /**
     * the width is used when wrap_content is set to layout_width
     * the child knows how big it should be
     *
     * @return
     */
    protected abstract int getDefaultWidth();

    /**
     * the height is used when wrap_content is set to layout_height
     * the child knows how big it should be
     *
     * @return
     */
    protected abstract int getDefaultHeight();


    private class SurfaceViewHandler extends Handler {

        public SurfaceViewHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private class DrawRunnable implements Runnable {

        private boolean error;
        private String name;
        public DrawRunnable(String name){
            this.name = name;
        }
        @Override
        public void run() {
            synchronized (BaseSurfaceView.class){
                if (!isAlive) {
                    return;
                }
            }

            try {
                canvas = getHolder().lockCanvas();
                error = onFrameDraw(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(!isAlive) return;
                try {
                    getHolder().unlockCanvasAndPost(canvas); //防止 surfaceview release 之后，还在调用
                    onFrameDrawFinish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!isAlive) {
                return;
            }
            if(error)
                handler.postDelayed(this, frameDuration);
        }
    }

    /**
     * it is will be invoked after one frame is drawn
     */
    protected abstract void onFrameDrawFinish();

    /**
     * draw one frame to the surface by canvas
     *
     * @param canvas
     */
    protected abstract boolean onFrameDraw(Canvas canvas);


}
