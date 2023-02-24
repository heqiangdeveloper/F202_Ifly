package com.chinatsp.ifly;

import android.car.hardware.mcu.CarMcuManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.db.CommandProvider;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.JumpEntity;
import com.chinatsp.ifly.module.me.helper.VoiceHelperFragment;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.module.me.recommend.view.ManageFloatWindow;
import com.chinatsp.ifly.module.me.recommend.view.VoiceRecommendFragment;
import com.chinatsp.ifly.module.me.settings.contract.LogSettingsContract;
import com.chinatsp.ifly.module.me.settings.view.fragment.ActorSettingsFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.AnswerSettingsFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.AwareSettingsFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.CommandFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.DetailMvwFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.DetailSrFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.LogSettingsFragment;
import com.chinatsp.ifly.module.me.settings.view.fragment.VoiceSettingsFragment;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.example.loginarar.LoginManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isLifeOnce = true;//一个生命周期之内只发送一次
    private static final String TAG = "SettingsActivity";
    public static final String KEY_ID = "fragment_id";
    private FragmentManager mFragmentManager;
    private VoiceSettingsFragment mSettingsFragment;
    private VoiceHelperFragment mHelperFragment;
    private AnswerSettingsFragment mAnswerFragment;
    private AwareSettingsFragment mAwareFragment;
    private ActorSettingsFragment mActorFragment;
    private LogSettingsFragment mLogFragment;
    private VoiceRecommendFragment mVoiceRecommendFragment;
    private CommandFragment mCommandFragment;
    private DetailSrFragment mDetailSrFragment;
    private DetailMvwFragment mDetailMvwFragment;
    private TextView tvCommand;
    private TextView tvSettings;
    private TextView tvRecommend;
    private ImageView iv_user_head;
    private TextView tv_video_count;
    private TextView tv_command_count;
    private String user_head;
    private static int currentFragment;
    private int fragmentId;
    private TextView tvLog;

    //点击5次
    private final int CLICK_NUM = 5;
    //点击时间间隔1秒
    private final int CLICK_INTERVER_TIME = 1000;
    //上一次的点击时间
    private long lastClickTime = 0;
    //记录点击次数
    private int clickNum = 0;

    private JumpEntity mJumpEntity;
    private int jumpName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mFragmentManager = getSupportFragmentManager();


        Intent intent = getIntent();
        if(VideoListUtil.getInstance(this).noReadVideoExist()){
            fragmentId = getIntent().getIntExtra(KEY_ID, AppConstant.VOICE_RECOMMEND_ID);
        }else {
            fragmentId = getIntent().getIntExtra(KEY_ID, AppConstant.VOICE_COMMAND_ID);
        }
        getJumpId(intent);
        Log.d(TAG, "onCreate() called with: mJumpEntity = [" + mJumpEntity + "]");
        if (savedInstanceState != null) {  // “内存重启”时调用
            mSettingsFragment = (VoiceSettingsFragment) mFragmentManager.findFragmentByTag(VoiceSettingsFragment.class.getName());
            mVoiceRecommendFragment = (VoiceRecommendFragment)mFragmentManager.findFragmentByTag(VoiceRecommendFragment.class.getName()) ;
            mCommandFragment = (CommandFragment)mFragmentManager.findFragmentByTag(CommandFragment.class.getName()) ;
            mLogFragment = (LogSettingsFragment) mFragmentManager.findFragmentByTag(LogSettingsFragment.class.getName()) ;
            mAnswerFragment = (AnswerSettingsFragment) mFragmentManager.findFragmentByTag(AnswerSettingsFragment.class.getName());
            mAwareFragment = (AwareSettingsFragment) mFragmentManager.findFragmentByTag(AwareSettingsFragment.class.getName());
            mDetailSrFragment = (DetailSrFragment) mFragmentManager.findFragmentByTag(DetailSrFragment.class.getName());
            mDetailMvwFragment = (DetailMvwFragment) mFragmentManager.findFragmentByTag(DetailMvwFragment.class.getName());
            fragmentId =currentFragment;
        } else {  // 正常时
            newAllFragment();
            addAllFragment();
        }
        initView();
        initContentResolver(this);
        currentFragment = 0;
        this.setFinishOnTouchOutside(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume:fragmentId="+fragmentId);
        boolean ttsPlaying = TTSController.getInstance(this).isTtsPlaying();
//        if (!ttsPlaying){
//            Utils.exitVoiceAssistant();
//        }
        //解决bugID1062683和1059664
        if (FloatViewManager.getInstance(this).isChangeMode){
            FloatViewManager.getInstance(this).isChangeMode =false;
        }else {
            Utils.exitVoiceAssistant();
        }
        if(mJumpEntity!=null){
            switchFragmnetByJump();
        }else
            if (fragmentId==AppConstant.VOICE_DETAIL_SR||fragmentId==AppConstant.VOICE_DETAIL_MVW){
                currentFragment = fragmentId;
                tvCommand.setSelected(true);
            }else {
                switchFragment(fragmentId);
            }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mJumpEntity = intent.getParcelableExtra("jumpModule");
        Log.d(TAG,"onNewIntent:mJumpEntity="+mJumpEntity);
        getJumpId(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.sendBroadcastForHideSoftBoard(this);
    }

    private void getJumpId(Intent intent){
        mJumpEntity = intent.getParcelableExtra("jumpModule");
        jumpName =intent.getIntExtra("jumpName",0);
        Log.d(TAG,"jumpName ="+jumpName);
        if (jumpName==(AppConstant.VOICE_SETTINGS_ID)){
            fragmentId = intent.getIntExtra(KEY_ID, AppConstant.VOICE_SETTINGS_ID);
        }
    }

    public void switchFragment(int id) {
        //关键软键盘
        Utils.hideKeyBoard(this);
        if (id == currentFragment) {
            Log.d(TAG, "switchFragment just in id =" + id + " , return!!!");
            return;
        }
        Log.d(TAG, "switchFragment  id =" + id);
        currentFragment = id;
        fragmentId = id;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(mSettingsFragment);
        transaction.hide(mCommandFragment);
        transaction.hide(mVoiceRecommendFragment);
        transaction.hide(mLogFragment);
        if (mAnswerFragment != null && mAnswerFragment.isAdded()) {
            transaction.remove(mAnswerFragment);
            mAnswerFragment = null;
        }
        if (mAwareFragment != null && mAwareFragment.isAdded()) {
            transaction.remove(mAwareFragment);
            mAwareFragment = null;
        }
        if (mActorFragment != null && mActorFragment.isAdded()) {
            transaction.remove(mActorFragment);
            mActorFragment = null;
        }
        if(mDetailSrFragment!=null&&mDetailSrFragment.isAdded()){
            transaction.remove(mDetailSrFragment);
            mDetailSrFragment = null;
        }

        if(mDetailMvwFragment!=null&&mDetailMvwFragment.isAdded()){
            transaction.remove(mDetailMvwFragment);
            mDetailMvwFragment = null;
        }
        setVideoCountVisible(true);
        switch (id) {
            case AppConstant.VOICE_SETTINGS_ID: {
                transaction.show(mSettingsFragment);
                tvSettings.setSelected(true);
                tvCommand.setSelected(false);
                tvRecommend.setSelected(false);
                break;
            }
            case AppConstant.VOICE_RECOMMEND_ID:{
                transaction.show(mVoiceRecommendFragment);
                tvRecommend.setSelected(true);
                tvCommand.setSelected(false);
                tvSettings.setSelected(false);
                setVideoCountVisible(false);
                break;
            }
            case AppConstant.VOICE_COMMAND_ID:{
                transaction.show(mCommandFragment);
                tvRecommend.setSelected(false);
                tvCommand.setSelected(true);
                tvSettings.setSelected(false);
                break;
            }
            case AppConstant.VOICE_SUBSETTINGS_ANSWER_ID: {
                if (mAnswerFragment == null) {
                    mAnswerFragment = new AnswerSettingsFragment();
                    transaction.add(R.id.fl_content, mAnswerFragment, AnswerSettingsFragment.class.getName());
                }
                transaction.show(mAnswerFragment);
                tvSettings.setSelected(true);
                tvCommand.setSelected(false);
                break;
            }
            case AppConstant.VOICE_SUBSETTINGS_AWARE_ID: {
                if (mAwareFragment == null) {
                    mAwareFragment = new AwareSettingsFragment();
                    transaction.add(R.id.fl_content, mAwareFragment, AwareSettingsFragment.class.getName());
                }
                transaction.show(mAwareFragment);
                tvSettings.setSelected(true);
                tvCommand.setSelected(false);
                break;
            }
            case AppConstant.VOICE_SUBSETTINGS_ACTOR_ID: {
                if (mActorFragment == null) {
                    mActorFragment = new ActorSettingsFragment();
                    transaction.add(R.id.fl_content, mActorFragment, ActorSettingsFragment.class.getName());
                }
                transaction.show(mActorFragment);
                tvSettings.setSelected(true);
                tvCommand.setSelected(false);
                break;
            }
            case AppConstant.VOICE_SUBSETTINGS_LOG_ID: {
                if (mLogFragment == null) {
                    mLogFragment = new LogSettingsFragment();
                    transaction.add(R.id.fl_content, mLogFragment, LogSettingsFragment.class.getName());
                }
                transaction.show(mLogFragment);
                tvSettings.setSelected(true);
                tvCommand.setSelected(false);
                break;
            }
        }
        transaction.commit();
    }

    public void switchFragment(BaseFragment fragment){
        if(fragment instanceof DetailSrFragment){
            mDetailSrFragment = (DetailSrFragment) fragment;
            currentFragment = AppConstant.VOICE_DETAIL_SR;
        }else if(fragment instanceof DetailMvwFragment){
            mDetailMvwFragment = (DetailMvwFragment) fragment;
            currentFragment = AppConstant.VOICE_DETAIL_MVW;
        }
        fragmentId = currentFragment;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(mSettingsFragment);
        transaction.hide(mCommandFragment);
        transaction.hide(mVoiceRecommendFragment);
        transaction.hide(mLogFragment);
        if (fragment.isAdded()) {
            transaction.remove(fragment);
        }
        transaction.add(R.id.fl_content, fragment, fragment.getClass().getName());
        transaction.show(fragment);
        transaction.commit();
    }

    //如果是修改唤醒词,在修改完成后同时更新主界面的唤醒词显示.
    public void updateVoiceData() {
        if (mSettingsFragment != null) {
            mSettingsFragment.updateVoiceData();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_exit:
                finish();
                break;
            case R.id.iv_home:
                backToHome();
                break;
            case R.id.tv_voice_settings:
                if(tvSettings.isSelected())
                    return;
                switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
            case R.id.tv_voice_command:
                if (tvCommand.isSelected())
                    return;
                switchFragment(AppConstant.VOICE_COMMAND_ID);
                break;
            case R.id.tv_voice_recommand:
                if(tvRecommend.isSelected())
                    return;
                switchFragment(AppConstant.VOICE_RECOMMEND_ID);
                break;
            case R.id.iv_user_head:
                Intent intent = new Intent();
                intent.setPackage("com.tencent.wecarnavi");
                intent.setAction( "open_vc_from_others");
                intent.putExtra("open_vc", "frag_uc");
                sendBroadcast(intent);
                Log.d(TAG, "lh:openCarController:" + "frag_uc");
                break;
            case R.id.tv_log://进入语音日志界面
                fiveTimeClick();
                break;
        }
    }

    private void backToHome() {
        if (isLifeOnce){
            isLifeOnce = false;
            Intent homeVirture = new Intent("chinatsp.action.virtual.tts");
            homeVirture.putExtra("extra","EXTRA_NAVI_YUN");
            sendBroadcast(homeVirture);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        Utils.exitVoiceAssistant();
    }


    public int getCurrentFragmentId() {
        return currentFragment;
    }

    public void GetImage(String uir){
        if (uir!=null&&!uir.isEmpty()){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uir)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {

            }
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();//得到图片的流
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Message msg = new Message();
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }
        });
        }
    }


    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap)msg.obj;
            iv_user_head.setImageBitmap(bitmap);//将图片的流转换成图片
        }
    };


    private void fiveTimeClick(){
        long currentClickTime = SystemClock.uptimeMillis();
        if(currentClickTime - lastClickTime <= CLICK_INTERVER_TIME || lastClickTime == 0){
            lastClickTime = currentClickTime;
            clickNum++;
        }else {//超时重新计数
            clickNum = 1;
            lastClickTime = 0;
            return;
        }
        if(clickNum == CLICK_NUM){
            clickNum = 0;
            lastClickTime = 0;
            //处理事件
            switchFragment(AppConstant.VOICE_SUBSETTINGS_LOG_ID);
        }
    }

    private void initView() {
        user_head = LoginManager.getInstance().getAvatar();
        tvSettings = findViewById(R.id.tv_voice_settings);
        tvCommand = findViewById(R.id.tv_voice_command);
        iv_user_head = findViewById(R.id.iv_user_head);
        tvRecommend = findViewById(R.id.tv_voice_recommand);
        tvLog = findViewById(R.id.tv_log);
        tv_video_count =(TextView)findViewById(R.id.tv_video_count);
        tv_command_count =(TextView)findViewById(R.id.tv_command_count);
        tvRecommend.setOnClickListener(this);
        tvSettings.setOnClickListener(this);
        tvCommand.setOnClickListener(this);
        iv_user_head.setOnClickListener(this);
        tvLog.setOnClickListener(this);
        findViewById(R.id.iv_exit).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
//        GetImage(user_head);
    }


    private void newAllFragment() {
        mSettingsFragment = new VoiceSettingsFragment();
        mVoiceRecommendFragment = new VoiceRecommendFragment();
        mCommandFragment = new CommandFragment();
        mLogFragment = new LogSettingsFragment();
    }

    private void addAllFragment() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fl_content, mSettingsFragment, VoiceSettingsFragment.class.getName());
        transaction.add(R.id.fl_content, mVoiceRecommendFragment, VoiceRecommendFragment.class.getName());
        transaction.add(R.id.fl_content, mCommandFragment, CommandFragment.class.getName());
        transaction.add(R.id.fl_content, mLogFragment, LogSettingsFragment.class.getName());
        transaction.commit();
    }

    private void switchFragmnetByJump(){
        String action = mJumpEntity.action;
        String module = mJumpEntity.moduleName;
        String skill = mJumpEntity.skillName;
        if(ManageFloatWindow.ACTION_OPEN_MODULE.equals(action)){
            if(mCommandFragment!=null)
                mCommandFragment.putExtra(mJumpEntity);
            switchFragment(AppConstant.VOICE_COMMAND_ID);
        }else if(ManageFloatWindow.ACTION_OPEN_SKILL.equals(action)){
            Observable.just("").map(new Function<String, ArrayList<CommandInfo>>() {
                @Override
                public ArrayList<CommandInfo> apply(String s) throws Exception {
                    Log.d(TAG, "apply() called with: module = [" + module + "]"+skill);
                    return CommandProvider.getInstance(SettingsActivity.this).queryModelSkillContents(module,skill);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<CommandInfo>>() {
                @Override
                public void accept(ArrayList<CommandInfo> infos) throws Exception {
                    if(infos!=null&&infos.size()>0){
                        String moduleType = infos.get(0).getModuleType();
                        if("0".equals(moduleType)){
                            DetailMvwFragment fragment= DetailMvwFragment.newInstance(infos);
                            switchFragment(fragment);
                        }else {
                            DetailSrFragment fragment= DetailSrFragment.newInstance(infos);
                            switchFragment(fragment);
                        }

                        tvRecommend.setSelected(false);
                        tvCommand.setSelected(true);
                        tvSettings.setSelected(false);

                    }else
                        switchFragment(fragmentId);
                }
            });

        }else if(ManageFloatWindow.ACTION_OPEN_VIDEO.equals(action)) {
            switchFragment(AppConstant.VOICE_RECOMMEND_ID);
        }
        else
            switchFragment(fragmentId);
    }


    private void setVideoCountVisible(boolean isRecommand){
        int unread_count = query();
        if (unread_count>0&&isRecommand){
            tv_video_count.setText(unread_count+"");
            tv_video_count.setVisibility(View.VISIBLE);
            if (unread_count>=10){
                tv_video_count.setBackground(getResources().getDrawable(R.drawable.big_unread_count));
            }else {
                tv_video_count.setBackground(getResources().getDrawable(R.drawable.unread_count));
            }
        }else {
            tv_video_count.setVisibility(View.GONE);
        }
    }
    private String AUTHORITY ="com.chinatsp.ifly.videodata";
    private Uri videoUri = Uri.parse("content://" + AUTHORITY + "/VideoData");
    private ContentObserver mContentObserver;
    private void initContentResolver(Context context) {
        if (mContentObserver == null) {
            mContentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange);
                    if (uri.toString().equalsIgnoreCase("content://com.chinatsp.ifly.videodata/VideoData")){
                        setVideoCountVisible(true);
                    }
                }
            };
        }
        this.getContentResolver().registerContentObserver(videoUri, false, mContentObserver);
    }
    //查询未读视频数量
    private int query() {
        Cursor cursor = this.getContentResolver().query(videoUri, null, null, null, null);
        int count = cursor.getCount();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String read = cursor.getString(cursor.getColumnIndex("read"));
            if (read.equalsIgnoreCase("true")){
                count =count-1;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }
}
