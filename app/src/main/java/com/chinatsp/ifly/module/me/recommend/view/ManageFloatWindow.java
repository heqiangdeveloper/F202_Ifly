package com.chinatsp.ifly.module.me.recommend.view;

import android.car.hardware.CarSensorEvent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.CommandEvent;
import com.chinatsp.ifly.entity.JumpEntity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.framesurfaceview.FrameSurfaceView;
import com.chinatsp.ifly.service.DetectionService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/29
 */

public class ManageFloatWindow {

    private final static String TAG = "ManageWindow";
    private static ManageFloatWindow manageFloatWindow;
    private static final String IFLY_PACKAGE = "com.oushang.voice";
    private static final String ACTION_OPEN_GUIDE = "openguide";
    public static final String ACTION_OPEN_VIDEO = "openvideo";
    public static final String ACTION_OPEN_MODULE = "openmodule";
    public static final String ACTION_OPEN_SKILL = "openskill";
    private static final int ANIM_STATUS_NORMAL_ANIM = 1;
    private static final int ANIM_STATUS_NORMAL_QUIET = 2;
    private static final int ANIM_STATUS_MESSAGE_ANIM = 3;
    private static final int ANIM_STATUS_MESSAGE_QUIET = 4;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    private Context mContext;
    private int mShowCount = 0;//消息体显示次数，开机最大显示 3 次
    private String mJumpContent;
    private String mJumpText;
    private JumpEntity mJumpEntity;

    private FloatView floatView;
    private XiaoOuTextView xiaoOuTextView;
    private VideoView videoView;
    private List<Integer> mNormalBitmaps;
    private List<Integer> mNormalQuietBitmaps;
    private List<Integer> mMessageBitmaps;
    private List<Integer> mMessageQuietBitmaps;
    private FrameSurfaceView mFrameSurfaceView;
//    private TextView mTvMessage;


    private static final int MSG_ATTENTION = 1001;
    private static final int TIME_DELAY_SHOWING = 600;
    private static final int MSG_CONTINUE = 1002;
    private static final int TIME_DELAY_CONTINUE = 10*1000;


    private int mCurrentStatus;
    private boolean mShown = false;
    private boolean mCommandDownload = false;
    private boolean mVideoDownload = true;
    private boolean mMessageClicked = false;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_ATTENTION:
                    String tts = (String) msg.obj;
                    Utils.startTTS(tts, null);
                    break;
                case MSG_CONTINUE:
                    if(mCurrentStatus==ANIM_STATUS_NORMAL_QUIET)
                        setAnim(ANIM_STATUS_NORMAL_ANIM);
                    else  if(mCurrentStatus==ANIM_STATUS_MESSAGE_QUIET)
                        setAnim(ANIM_STATUS_MESSAGE_ANIM);
                    break;
            }

        }
    };

    public static synchronized ManageFloatWindow getInstance(Context context) {
        if (manageFloatWindow == null) {
            manageFloatWindow = new ManageFloatWindow(context);
        }
        return manageFloatWindow;
    }


    private ManageFloatWindow(Context context) {
        this.mContext = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wmParams == null) {
            wmParams = new WindowManager.LayoutParams();
        }
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.LEFT|Gravity.TOP;
        wmParams.x =0;
        wmParams.y =92;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


        initBitmapRes();
//        registerCommandEvent();

    }




    class FloatView extends LinearLayout {
        private ImageView imageView;
        private Context mContext;
        public FloatView(Context context) {
            super(context);
            mContext = context;
            initView();
        }

        private void initView() {
            inflate(getContext(), R.layout.float_ball, this);
            mFrameSurfaceView = findViewById(R.id.frame_surfaceview);
            RelativeLayout root = findViewById(R.id.root_float_view);
//            mTvMessage = findViewById(R.id.tv_float_message);
            mFrameSurfaceView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHandleClick();
                }
            });
          /*  mTvMessage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHandleClick();
                }
            });*/

        }

    }


    public void showFloatView() {
        if(mShown){
            Log.e(TAG, "showFloatView: the UI is shown");
            return;
        }

        if(!DetectionService.PACKAGE_NAVI.equals(AppConstant.mCurrentPkg)){
            Log.e(TAG, "showFloatView: the current pkg:"+AppConstant.mCurrentPkg);
            return;
        }

        /*if(!MXSdkManager.getInstance(mContext).isPageFuFragmentShow()){
            Log.e(TAG, "showFloatView: "+MXSdkManager.getInstance(mContext).currentPage);
            return;
        }*/

        if (floatView == null) {
            floatView = new FloatView(mContext);
            windowManager.addView(floatView, wmParams);
        }
        mShown = true;
        if(mCommandDownload&&mVideoDownload){
            if(mJumpText!=null&&!"".equals(mJumpText)&&!mMessageClicked){//消息体不为空 没有被点击过
                if(mShowCount<=3){
                    setAnim(ANIM_STATUS_MESSAGE_QUIET);
                    showMessage(true);
                }else{
                    setAnim(ANIM_STATUS_MESSAGE_QUIET);
                    showMessage(false);
                }
            }else
                setAnim(ANIM_STATUS_NORMAL_QUIET);
        }else
           setAnim(ANIM_STATUS_NORMAL_QUIET);
        mShowCount++;
    }

    /**
     * 在非导航页面隐藏
     * 小欧语音交互是隐藏
     */
    private void removeFloatView() {
        if (floatView == null) {
            return;
        }
        if(!mShown){
            Log.e(TAG, "removeView: the UI is hide");
            return;
        }
        windowManager.removeViewImmediate(floatView);
        mShown = false;
        floatView = null;
        removeXiaoOuTextView();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateFloatUI(CommandEvent event) {
        switch (event.type){
            case JUMP:
                mJumpText = event.text;
                mJumpContent = event.message;
                analysisJumpContent(mJumpContent);
                break;
            case COMMAND:
                mCommandDownload = true;
//                showMessageAnims();
                break;
            case RECOMMEND:
                break;
            case HIDE:
//                removeFloatView();
                break;
            case SHOW:
//                showFloatView();
                break;
            case NAVI:
//                refreshMessageUI();
                break;
            case GEAR:
//                refreshMessageUI();
                break;
        }
    }




    private void  setAnim(int status){
        mCurrentStatus = status;
        if(status == ANIM_STATUS_NORMAL_ANIM){
            mFrameSurfaceView.setLoop(false);
            mFrameSurfaceView.setBitmapIds(mNormalBitmaps, new FrameSurfaceView.OnFrameAnimationListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd() {
                    setAnim(ANIM_STATUS_NORMAL_QUIET);

                }
            });
        }else if(status == ANIM_STATUS_NORMAL_QUIET){
            mFrameSurfaceView.setLoop(false);
            mFrameSurfaceView.setBitmapIds(mNormalQuietBitmaps,null);
            mHandler.removeMessages(MSG_CONTINUE);
            mHandler.sendEmptyMessageDelayed(MSG_CONTINUE,TIME_DELAY_CONTINUE);
        }else if(status == ANIM_STATUS_MESSAGE_ANIM){
            mFrameSurfaceView.setLoop(false);
            mFrameSurfaceView.setBitmapIds(mMessageBitmaps, new FrameSurfaceView.OnFrameAnimationListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd() {
                    setAnim(ANIM_STATUS_MESSAGE_QUIET);

                }
            });
        }else if(status == ANIM_STATUS_MESSAGE_QUIET){
            mFrameSurfaceView.setLoop(false);
            mFrameSurfaceView.setBitmapIds(mMessageQuietBitmaps,null);
            mHandler.removeMessages(MSG_CONTINUE);
            mHandler.sendEmptyMessageDelayed(MSG_CONTINUE,TIME_DELAY_CONTINUE);
        }

        mFrameSurfaceView.start();
    }



    public void showXiaoOuTextView(String textContent){
       /* if (xiaoOuTextView == null) {
            xiaoOuTextView = new XiaoOuTextView(mContext,textContent);
            wmParams.x = 120;
            wmParams.y = -100;
            windowManager.addView(xiaoOuTextView, wmParams);
        }*/
    }

    public void removeXiaoOuTextView() {
       /* if (xiaoOuTextView == null) {
            return;
        }
        windowManager.removeViewImmediate(xiaoOuTextView);
        xiaoOuTextView = null;*/
    }


    public void showVideoView(String path){
      /*  if (videoView == null) {
            videoView = new VideoView(mContext,path);
            wmParams.x = 0;
            wmParams.y = 0;
            windowManager.addView(videoView, wmParams);
        }*/
    }

    public void removeVideoView() {
      /*  if (videoView == null) {
            return;
        }
        windowManager.removeViewImmediate(videoView);
        videoView = null;*/
    }

    /**
     * 监听数据更新消息
     */
    private void registerCommandEvent() {
        EventBus.getDefault().register(this);
    }

    //xiaoou://com.oushang.voice?action=openvideo&videoId=17&timeout=202005191500  跳转到视频
    //xiaoou://com.oushang.voice?action=openmodule&moduleName=导航&skillName=发起导航&timeout=202005191500 跳转到模块及对应区域
    //xiaoou://com.oushang.voice?action=openskill&moduleName=导航&skillName=发起导航&timeout=202005191500  跳转到技能详情
    //xiaoou://com.oushang.voice?action=openguide&instructTeach=请用普通话对我说：“打开赛道模式”&timeout=202005191500 指令性情
    //xiaoou://com.xx?action=XXX&p1=X&p2=X&timeout=202005191500 跳转到第三方
    private void analysisJumpContent(String message){
        Log.d(TAG, "analysisJumpContent() called with: message = [" + message + "]");
        try {
            if(message.contains("//")){
                int pkgIndex = message.indexOf("//");
                int pkgEndIndex = message.indexOf("?");
                int firstEqual = message.indexOf("=");
                int firstAnd = message.indexOf("&");
                String pkg = message.substring(pkgIndex+2,pkgEndIndex);
                String action = message.substring(firstEqual+1,firstAnd);
                if(IFLY_PACKAGE.equals(pkg)){
                    if(ACTION_OPEN_GUIDE.equals(action)&&message.contains("instructTeach=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        int instructTeachIndex = message.indexOf("instructTeach=");
                        int lastAnd = message.lastIndexOf("&");
                        String instructTeach = message.substring(instructTeachIndex+14,lastAnd);
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.instructTeach = instructTeach;
                        mJumpEntity.action = action;
                        mJumpEntity.timeout = time;
                    }else if(ACTION_OPEN_VIDEO.equals(action)&&message.contains("videoId=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        int videoIndex = message.indexOf("videoId=");
                        int lastAnd = message.lastIndexOf("&");
                        String videoId = message.substring(videoIndex+8,lastAnd);
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.videoId = videoId;
                        mJumpEntity.action = action;
                        mJumpEntity.timeout = time;
                    }else if(ACTION_OPEN_SKILL.equals(action)&&message.contains("moduleName=")&&message.contains("skillName=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        mJumpEntity.action = action;
                        int moduleIndex = message.indexOf("moduleName=");
                        int skillIndex = message.indexOf("skillName=");
                        int secondAnd = message.indexOf("&",moduleIndex);
                        int lastAnd = message.lastIndexOf("&");
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.moduleName = message.substring(moduleIndex+11,secondAnd);
                        mJumpEntity.skillName = message.substring(skillIndex+10,lastAnd);
                        mJumpEntity.timeout = time;
                        Log.d(TAG, "analysisJumpContent() called with: moduleIndex = [" + moduleIndex + "]"+"..skillIndex:"+skillIndex+".."+secondAnd+"..."+timeIndex);
                    }else if(ACTION_OPEN_MODULE.equals(action)&&message.contains("moduleName=")&&message.contains("skillName=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        mJumpEntity.action = action;
                        int moduleIndex = message.indexOf("moduleName=");
                        int skillIndex = message.indexOf("skillName=");
                        int secondAnd = message.indexOf("&",moduleIndex);
                        int lastAnd = message.lastIndexOf("&");
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.moduleName = message.substring(moduleIndex+11,secondAnd);
                        mJumpEntity.skillName = message.substring(skillIndex+10,lastAnd);
                        mJumpEntity.timeout = time;
                    }
                }
            }else return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "analysisJumpContent() called with: mJumpEntity = [" + mJumpEntity + "]");
    }

    /**
     * 处理跳转事件
     */
    private void doHandleClick(){

        if(mJumpEntity==null||mMessageClicked){//为空 或者消息体已经被点击过了，表示没有消息，直接跳转到界面
            Intent intent = new Intent(mContext, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }else {
            if(ACTION_OPEN_GUIDE.equals(mJumpEntity.action)){//语音引导
                if(mJumpEntity.instructTeach==null||"".equals(mJumpEntity.instructTeach)){ //直接启动语音
                    Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
                }else {  //显示语音引导界面
                    Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
                    broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_GUIDE);
                    broad.putExtra(AppConstant.EXTRA_SHOW_TTS,mJumpEntity.instructTeach);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);

                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_ATTENTION;
                    msg.obj = mJumpEntity.instructTeach;
                    mHandler.sendMessageDelayed(msg,TIME_DELAY_SHOWING);
                }
            }else if(ACTION_OPEN_MODULE.equals(mJumpEntity.action)){
                Intent intent = new Intent(mContext, SettingsActivity.class);
                intent.putExtra("jump",mJumpEntity);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }else if(ACTION_OPEN_SKILL.equals(mJumpEntity.action)){
                Intent intent = new Intent(mContext, SettingsActivity.class);
                intent.putExtra("jump",mJumpEntity);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }else
                Log.e(TAG, "doHandleClick: mJumpEntity::"+mJumpEntity);
        }
        mMessageClicked = true;
    }

    private void initBitmapRes() {
        mNormalBitmaps = new ArrayList<>();
        for (int i = 0; i <= 56; i += 1) {
            int resId = Utils.getId(mContext, "white_" + String.format("%05d", i));
            mNormalBitmaps.add(resId);
        }

        mNormalQuietBitmaps = new ArrayList<>();
        for (int i = 0; i <= 5; i += 1) {
            int resId = Utils.getId(mContext, "white_" + String.format("%05d", 56));
            mNormalQuietBitmaps.add(resId);
        }

        mMessageBitmaps = new ArrayList<>();
        for (int j = 0; j < 3; j++) {  //循坏加载 3 次，满足3s的动画效果
            for (int i = 0; i <= 24; i += 1) {
                int resId = Utils.getId(mContext, "breathlight_white_" + String.format("%05d", i));
                mMessageBitmaps.add(resId);
            }
        }

        mMessageQuietBitmaps = new ArrayList<>();
        for (int i = 0; i <= 5; i += 1) {
            int resId = Utils.getId(mContext, "breathlight_white_" + String.format("%05d", 24));
            mMessageQuietBitmaps.add(resId);
        }

    }

    /**
     * 显示消息体动画
     */
    private void showMessageAnims() {
        Log.d(TAG, "showMessageAnims() called：："+mCommandDownload+"..."+mVideoDownload);
        if(mCommandDownload&&mVideoDownload){ //如果所有指令下载完成 和 视频下载完成，开始显示消息动画
            setAnim(ANIM_STATUS_MESSAGE_ANIM);
            showMessage(true);
        }else{
            showMessage(false);
            setAnim(ANIM_STATUS_NORMAL_ANIM);
        }

    }

    /**
     * 显示消息体内容
     */
    private void showMessage(boolean shown) {
        if(mJumpEntity==null||mJumpEntity.timeout==null)return;
        Log.d(TAG, "showMessage() called with: shown = [" + shown + "]"+mJumpText+".."+mJumpEntity.timeout);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder builder = new StringBuilder();
        builder.append(mJumpEntity.timeout.substring(0,4));
        builder.append("-");
        builder.append(mJumpEntity.timeout.substring(4,6));
        builder.append("-");
        builder.append(mJumpEntity.timeout.substring(6,8));
        builder.append(" ");
        builder.append(mJumpEntity.timeout.substring(8,10));
        builder.append(":");
        builder.append(mJumpEntity.timeout.substring(10,12));

        Date  now=  new Date(System.currentTimeMillis());
        try {
            Date showData= sdf.parse(builder.toString());
            Log.d(TAG, "showMessage() called with: now = [" + now + "]"+showData+"..."+now.after(showData));
            if(now.after(showData)){
//                mTvMessage.setVisibility(View.GONE);
                setAnim(ANIM_STATUS_NORMAL_QUIET);
                mMessageClicked = true;   //当做消息体被点击来处理
                return;
            }
        } catch (ParseException e) {
            Log.e(TAG, "showMessage: "+e);
            e.printStackTrace();
        }

        if(shown&&!mMessageClicked&&mJumpText!=null){
//            mTvMessage.setVisibility(View.VISIBLE);
//            mTvMessage.setText(mJumpText);
        }else{
//            mTvMessage.setVisibility(View.GONE);
        }


    }

    /**
     *
     */
    private void refreshMessageUI() {
        boolean isNaving = MXSdkManager.getInstance(mContext).isNaving();
        boolean isParking = CarUtils.carGear== CarSensorEvent.GEAR_PARK;
        Log.d(TAG, "refreshMessageUI() called::"+isNaving+"...."+isParking);
        if(isNaving||!isParking){ //如果实在导航或者非 P 档时不显示消息体
            showMessage(false);
        }else {
            if(mJumpText!=null&&!"".equals(mJumpText)&&!mMessageClicked){//消息体不为空 没有被点击过
                if(mShowCount<=3){
                    showMessage(true);
                }else
                    showMessage(false);
            }
            mShowCount++;
        }
    }

}