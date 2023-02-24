package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

   private int spanCount; //列数
   private int spacing; //间隔
   private boolean includeEdge; //是否包含边缘
    private int top;

    public GridSpacingItemDecoration(int spanCount, int spacing,int top, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.top = top;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

       //这里是关键，需要根据你有几列来判断
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = top; // item top
            }
        }
    }
}