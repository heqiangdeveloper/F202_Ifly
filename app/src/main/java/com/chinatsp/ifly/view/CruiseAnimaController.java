package com.chinatsp.ifly.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/9/28
 */

public class CruiseAnimaController {
    public static CruiseAnimaController cruiseAnimaController;
    public static synchronized CruiseAnimaController getInstance(){
        if (cruiseAnimaController==null){
            cruiseAnimaController = new CruiseAnimaController();
        }
        return cruiseAnimaController;
    }

    private int repeatCount =0;
    public void starPlayAnima(View firstView, View secondView){
        AnimatorSet animatorSetsuofang = new AnimatorSet();
        AnimatorSet animatorPartial = new AnimatorSet();
        AnimatorSet animatorsuoxiao = new AnimatorSet();

        animatorSetsuofang.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                starPartialAnima(animatorPartial,secondView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorPartial.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                secondView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
               suoxiaoAnima(animatorsuoxiao,secondView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorsuoxiao.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                secondView.setVisibility(View.GONE);
                repeatCount++;
                Log.d("qlf","repeatCount ="+repeatCount);
                if (repeatCount>2){
                    repeatCount = 0;
                    return;
                }
                starPartialAnima(animatorPartial,secondView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        starAllAnima(animatorSetsuofang,firstView);
    }


    private void starAllAnima(AnimatorSet animatorSet, View view){

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f);
        ObjectAnimator TranslationY =  ObjectAnimator.ofFloat(view, "TranslationY", 0,-120);
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY).with(TranslationY);
        animatorSet.start();
    }

    private void starPartialAnima(AnimatorSet animatorSet, View view){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.5f,1f);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(scaleX,scaleY,alpha);
        animatorSet.start();
    }


    private void suoxiaoAnima(AnimatorSet animatorSet, View view){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.3f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.3f,1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f,0.5f);
        animatorSet.setDuration(500);
        animatorSet.playTogether(scaleX,scaleY,alpha);
        animatorSet.setStartDelay(300);
        animatorSet.start();
    }
}
