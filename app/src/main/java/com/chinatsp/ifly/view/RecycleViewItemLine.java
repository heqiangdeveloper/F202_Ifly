package com.chinatsp.ifly.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2016/6/22
 * 描    述：自定义recycleView的分割线【可以是分割线，也可以是分割条】
 * 修订历史：
 * ================================================
 */
public class RecycleViewItemLine extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private Drawable mDivider;
    private boolean mHeaderDividerEnabled;

    /**
     * 分割线高度，默认为1px
     */
    private int mDividerHeight = 2;
    /**
     * 列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
     */
    private int mOrientation;
    private static int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * 默认分割线：高度为2px，颜色为灰色
     * @param context     上下文
     * @param orientation 列表方向
     */
    public RecycleViewItemLine(Context context, int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * 自定义分割线
     * @param context     上下文
     * @param orientation 列表方向
     * @param drawableId  分割线图片,或者shape图形
     */
    public RecycleViewItemLine(Context context, int orientation, int drawableId) {
        this(context, orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    /**
     * 自定义分割线
     * @param context       上下文
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
     * @param dividerColor  分割线颜色
     */
    public RecycleViewItemLine(Context context, int orientation, int dividerHeight, int dividerColor) {
        this(context, orientation);
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    /**
     * 获取分割线尺寸
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
        //绘制了顶部分割线，因此需要将第一行的item下移相应的距离
        if(mHeaderDividerEnabled) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.set(0, mDivider.getIntrinsicHeight(), 0, 0);
                }
            }
        }
    }

    /**
     * 绘制分割线
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /**
     * 绘制横向 item 分割线
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        if(mHeaderDividerEnabled) {
            drawTopLine(canvas, parent);
        }
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
    private void drawTopLine(Canvas canvas, RecyclerView parent) {
        //绘制分割线
        int left = 0;
        int right = parent.getWidth();
        View child = parent.getChildAt(0);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

        //不可以直接设置top = 0；因为这样的话分隔线就不会跟着移动，因为top = 0,是绝对位置，
        //所以应该设置为子view的相对位置
        //这样才可以跟着滑动。
        //child的顶部坐标，减去设置的margin_top的值，再减去child为了给分割线腾出空间所下滑的高度，
        //这样分割线才会在顶部

        int top = child.getTop() - params.topMargin - mDivider.getIntrinsicHeight();
        int bottom;
        bottom = top + mDivider.getIntrinsicHeight();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }
    /**
     * 绘制纵向 item 分割线
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 设置是否显示顶部的divider
     * @param headerDividerEnabled
     */
    public void setHeaderDividerEnabled(boolean headerDividerEnabled) {
        this.mHeaderDividerEnabled = headerDividerEnabled;
    }
}
