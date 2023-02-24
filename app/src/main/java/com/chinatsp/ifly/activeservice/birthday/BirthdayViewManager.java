package com.chinatsp.ifly.activeservice.birthday;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Created by zhengxb on 2019/5/21.
 */

public class BirthdayViewManager {

    private static BirthdayViewManager birthdayViewManager;

    private Context mContext;
    private WindowManager winManager;
    private WindowManager.LayoutParams params;

    private MediaPlayer mediaPlayer;
    private String birthday_blessing;

    private FloatSmallView floatSmallView;
    private static final int ACTION_PLAY = 1;
    private static boolean vedio_onclick = true;

    private BirthdayViewManager(Context context) {
        this.mContext = context.getApplicationContext();
        winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static synchronized BirthdayViewManager getInstance(Context context) {
        if (birthdayViewManager == null) {
            birthdayViewManager = new BirthdayViewManager(context);
        }
        return birthdayViewManager;
    }

    public void show(String blessing) {
        String path;
        File file = null;
        try {
            path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/iflytek/video/birthday/birthday.mp4";
            file = new File(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.exists()) {
            birthday_blessing = blessing;
            // AudioFocusUtils.getInstance(mContext).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
            floatSmallView = getFloatSmallView();
            if (floatSmallView != null) {
                if (floatSmallView.getParent() == null) {
                    winManager.addView(floatSmallView, params);
                }
            }
        }
    }

    private FloatSmallView getFloatSmallView() {
        if (floatSmallView == null) {
            floatSmallView = new FloatSmallView(mContext);
        }
        if (params == null) {
            params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.x = 0;
            params.y = 0;
        }
        return floatSmallView;
    }

    class FloatSmallView extends LinearLayout {
        private ImageView birthday_letter;
        private VideoView birthday_video_view;
        private ImageView birthday_light;
        private AnimationDrawable frameAnim;
        private ImageView birthday_video_view_prev;


        public FloatSmallView(final Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.active_birthday_layout, this);

            try {
                mediaPlayer = new MediaPlayer();
                String path_toyou = Environment.getExternalStorageDirectory().getCanonicalPath() + "/iflytek/video/birthday/birthday_coming.wav";
                File file = new File(path_toyou);
                FileInputStream fis = new FileInputStream(file);
                mediaPlayer.setDataSource(fis.getFD());

//                mediaPlayer.setDataSource(path_toyou);
                mediaPlayer.prepare();
//                mediaPlayer.prepareAsync();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


            birthday_letter = view.findViewById(R.id.birthday_letter);
            birthday_video_view = view.findViewById(R.id.birthday_video_view);
            birthday_light = view.findViewById(R.id.birthday_light);
            birthday_video_view_prev = view.findViewById(R.id.birthday_video_view_prev);


            birthday_letter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // birthday_letter.setImageDrawable(getResources().getDrawable(R.drawable.active_birthday__openletter));
                    mHandler.sendEmptyMessageDelayed(ACTION_PLAY, 1 * 1200);
                    frameAnim = (AnimationDrawable) getResources().getDrawable(R.drawable.active_birthday_anmtion);
                    //把AnimationDrawable设置为ImageView的背景
                    birthday_letter.setBackgroundDrawable(frameAnim);
                    frameAnim.start();
                }
            });
            birthday_video_view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    vedio_onclick = false;
                    winManager.removeView(floatSmallView);
                    mediaPlayer.stop();
                    birthdayViewManager = null;
                    floatSmallView = null;
                }
            });

            RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(2000);//设置动画持续周期
            rotate.setRepeatCount(-1);//设置重复次数
            rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            rotate.setStartOffset(0);//执行前的等待时间
            birthday_light.setAnimation(rotate);

        }

        public void startCarVideo() {
            frameAnim.stop();
            birthday_light.clearAnimation();
            birthday_video_view.setVisibility(View.VISIBLE);
            birthday_light.setVisibility(View.GONE);
            birthday_letter.setVisibility(View.GONE);
            try {
                String path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/iflytek/video/birthday/birthday.mp4";
                birthday_video_view.setVideoPath(path);
                mediaPlayer = new MediaPlayer();
                String path_toyou = Environment.getExternalStorageDirectory().getCanonicalPath() + "/iflytek/video/birthday/happy_birthday_to_u.mp3";
                mediaPlayer.setDataSource(path_toyou);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            birthday_video_view.start();
            birthday_video_view_prev.setVisibility(View.VISIBLE);
            birthday_video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            /* The player just pushed the very first video frame for rendering.
                            * 视频第一帧开始渲染,视频真正开始播放.
                            * @see android.media.MediaPlayer.OnInfoListener
                            */
                            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //隐藏预览图片,这里延时100ms消失是防止页面过渡时闪屏
                                        birthday_video_view_prev.setVisibility(View.INVISIBLE);
                                    }
                                }, 100);
                                return true;
                            }
                            return false;
                        }
                    });
                }
            });
            mediaPlayer.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //requestVoiceAudioFocus();
                    birthday_video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            winManager.removeView(floatSmallView);
                            birthdayViewManager = null;
                            floatSmallView = null;
                        }
                    });


                }
            }, 17000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mediaPlayer = new MediaPlayer();
//                    String path_toyou = null;
//                    try {
//                        path_toyou = Environment.getExternalStorageDirectory().getCanonicalPath()+"/chinatsp/Video/dear_wangziming1.mp3";
//                        mediaPlayer.setDataSource(path_toyou);
//                        mediaPlayer.prepare();
//                        mediaPlayer.start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if (vedio_onclick) {
                        if(Utils.getCurrentPriority(mContext)<=PriorityControler.PRIORITY_ONE)
                           TTSController.getInstance(mContext).startTTS(birthday_blessing, PriorityControler.PRIORITY_ONE);
                    }

                }
            }, 15000);
        }

        @SuppressLint("HandlerLeak")
        private android.os.Handler mHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ACTION_PLAY:
                        startCarVideo();
                        break;
                }
            }
        };
    }

}
