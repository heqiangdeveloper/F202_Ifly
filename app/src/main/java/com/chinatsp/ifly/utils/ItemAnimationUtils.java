package com.chinatsp.ifly.utils;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.view.ItemRotateAnimation;

/**
 * Created by ytkj on 2019/5/27.
 */

public final class ItemAnimationUtils {


    public  static final String TAG=AllDimensionUtils.class.getName();

    private ItemAnimationUtils(){}


    //item下落动画
    public static void startFallDownAnimation(Context mContext, RecyclerView mRecyclerView){
        if (mContext==null||mRecyclerView==null){
            LogUtils.d(TAG, "mContext or mRecyclerView is null");
            return;
        }

        LayoutAnimationController fallDownAnimation = AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_fall_down);
        mRecyclerView.setLayoutAnimation(fallDownAnimation);
        mRecyclerView.startLayoutAnimation();
    }

    //item翻页动画
    public static void startRotateAnimation(boolean isReverse,RecyclerView mRecyclerView) {
        if (mRecyclerView==null){
            LogUtils.d(TAG, "mRecyclerView is null");
            return;
        }

        ItemRotateAnimation animation = new ItemRotateAnimation();
        animation.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setDelay(0.1f);
        controller.setOrder(isReverse ? LayoutAnimationController.ORDER_REVERSE : LayoutAnimationController.ORDER_NORMAL);
        mRecyclerView.setLayoutAnimation(controller);
        mRecyclerView.getAdapter().notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();
    }

}
