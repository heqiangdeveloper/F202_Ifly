package com.chinatsp.ifly;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.welcome.GuideWelcomeFragment;
import com.chinatsp.ifly.service.InitializeService;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

import butterknife.ButterKnife;

public class GuideMainActivity extends AppCompatActivity {
    public static Context mContext;
    private BaseFragment mFragment;
    public static final String STEP = "step";
    public static final int STEP1 = 1;
    public static final int STEP2 = 2;
    public static final int STEP3 = 3;
    public static final int STEP4 = 4;
    public static final String TAG = "GuideMainActivity";
    public static final int LOOP1 = 10;
    public static final int LOOP2 = 11;
    public static final int FAIL = 12;
    public static final int SUCCESS = 13;
    public static final String SELECTCOLOR = "#00a1ff";
    public static AnimationDrawable animationDrawable1;//新手引导--红色烟花
    public static AnimationDrawable animationDrawable2;//新手引导--橙色烟花
    public static AnimationDrawable animationDrawable3;//新手引导--绿色烟花
    public static AnimationDrawable animationDrawable4;//新手引导--金色烟花
    public static final int TIAOZIDURATION = 50;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_main);
        ButterKnife.bind(this);
        //mContext = GuideMainActivity.this;
        mContext = getApplicationContext();
        mContext.sendBroadcast(new Intent("close.mainactivity"));
//        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, Utils.getFromAssets(mContext, "mvw_custom_guide.json"));
        initFragment();
        initFireworks();
    }

    public void initFireworks(){
        animationDrawable1 = new AnimationDrawable();
        animationDrawable2 = new AnimationDrawable();
        animationDrawable3 = new AnimationDrawable();
        animationDrawable4 = new AnimationDrawable();
        //animationDrawable1
        for (int i = 1; i <= 34; i += 1) {
            int resId1 = Utils.getId(mContext, "redfire_" + String.format("%04d", i));
            animationDrawable1.addFrame(mContext.getResources().getDrawable(resId1), 100);
        }
        //animationDrawable2
        for (int i = 1; i <= 35; i += 1) {
            int resId2 = Utils.getId(mContext, "trailwork_" + String.format("%04d", i));
            animationDrawable2.addFrame(mContext.getResources().getDrawable(resId2), 100);
        }
        //animationDrawable3
        for (int i = 37; i <= 71; i += 1) {
            int resId3 = Utils.getId(mContext, "greenfire_" + String.format("%04d", i));
            animationDrawable3.addFrame(mContext.getResources().getDrawable(resId3), 100);
        }
        //animationDrawable4
        for (int i = 1; i <= 33; i += 1) {
            int resId4 = Utils.getId(mContext, "goldenfire_" + String.format("%04d", i));
            animationDrawable4.addFrame(mContext.getResources().getDrawable(resId4), 100);
        }

        animationDrawable1.setOneShot(false);
        animationDrawable2.setOneShot(false);
        animationDrawable3.setOneShot(false);
        animationDrawable4.setOneShot(false);
    }

    public void initFragment(){
        mFragment = GuideWelcomeFragment.newInstance("", "");
        ActivityUtils.replaceFragmentToActivity2(getSupportFragmentManager(), mFragment, R.id.framelayout_content);
    }

    private static void tryRecycleAnimationDrawable(AnimationDrawable animationDrawable) {
        if (animationDrawable != null) {
            animationDrawable.stop();
            for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
                Drawable frame = animationDrawable.getFrame(i);
                if (frame instanceof BitmapDrawable) {
                    ((BitmapDrawable) frame).getBitmap().recycle();
                }
                frame.setCallback(null);
            }
            animationDrawable.setCallback(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, Utils.getFromAssets(mContext, "mvw_custom.json"));
        SharedPreferencesUtils.saveInt(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY, 1);
        SharedPreferencesUtils.saveBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
        tryRecycleAnimationDrawable(animationDrawable1);
        tryRecycleAnimationDrawable(animationDrawable2);
        tryRecycleAnimationDrawable(animationDrawable3);
        tryRecycleAnimationDrawable(animationDrawable4);
    }

    //当Activity销毁时，不会把Fragment的相关信息保存下来，就解决了空指针问题！
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //super.onSaveInstanceState(outState, outPersistentState);
    }
}
