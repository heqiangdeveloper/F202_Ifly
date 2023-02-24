package com.chinatsp.ifly.module.me.recommend.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/31
 */

public class CustomVideoView extends VideoView {
    private final static String TAG = "CustomVideoView";
    private int width,height;
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setMeasure(int width, int height) {
        this.width = width;
        this.height = height;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG,"onMeasure");
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG,"width="+width+",height="+height);
        if (this.width>0&&this.height>0){
            widthSpecSize =this.width;
            heightSpecSize = this.height;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }
}
