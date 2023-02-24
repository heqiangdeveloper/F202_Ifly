package com.chinatsp.ifly;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chinatsp.ifly.module.me.recommend.Utils.TimeUilts;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.module.me.recommend.view.CustomVideoView;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/17
 */

public class VideoPlayActivty extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "VideoPlayActivty";
    private CustomVideoView videoView;
    private ImageView videoClose;
    private SeekBar videoSeekBar;
    private TextView tvMoveTime;
    private ImageView ivVideoPlay,ivVideoPre,ivVideoNext;
    private Display currDisplay;
    private int vWidth, vHeight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        initView();
        initListener();
        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent ="+intent);
        initData(getIntent());
    }
    String path;
    private void initData(Intent intent) {
        intent = getIntent();
        path = intent.getStringExtra("path");
        videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        videoView.setVideoPath(path);
        Log.d(TAG,"setVideoPath");
        currDisplay = this.getWindowManager().getDefaultDisplay();
//        reQuestPlay();
//        VideoListUtil.getInstance(this).getId(path);
        showTime();
    }
    //设置全屏播放
    private void fullScreen(){
        videoView.setMeasure(currDisplay.getWidth(),currDisplay.getHeight());
        videoView.requestLayout();
    }
    //取消全屏
    private void normalScreen(){
        videoView.setMeasure(vWidth,vHeight);
        videoView.requestLayout();
    }

    private void reQuestPlay(){
        videoView.start();
        requestVideoAudioFocus();
        VideoListUtil.getInstance(this).update("true",path);
    }

    private void nextOrPreVideo(String path,Boolean isNext){
        this.path =VideoListUtil.getInstance(this).rawQueryNextOrPre(path,isNext);
        Log.d(TAG,"this.path ="+this.path+",path ="+path);
        if ( !this.path.equalsIgnoreCase(path)){
            videoView.setVideoPath( this.path);
//            videoView.start();
            reQuestPlay();
        }else {
            if (isNext){
                Toast.makeText(this,this.getResources().getString(R.string.is_the_last),Toast.LENGTH_SHORT);
            }else {
                Toast.makeText(this,this.getResources().getString(R.string.is_the_first),Toast.LENGTH_SHORT);
            }
        }
    }

    private void initListener() {
        videoClose.setOnClickListener(this);
        ivVideoPlay.setOnClickListener(this);
        ivVideoPre.setOnClickListener(this);
        ivVideoNext.setOnClickListener(this);
        videoSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        videoView.setOnCompletionListener(MyPlayerOnCompletionListener);
        videoView.setOnErrorListener(onErrorListener);
        videoView.setOnPreparedListener(onPreparedListener);
    }

    private void initView() {
        videoView = (CustomVideoView) findViewById(R.id.video_view);
        videoClose =(ImageView)findViewById(R.id.video_close);
        videoSeekBar = (SeekBar)findViewById(R.id.videoSeekbar);
        tvMoveTime = (TextView)findViewById(R.id.tvMoveTime);
        ivVideoPlay = (ImageView)findViewById(R.id.ivVideoPlay);
        ivVideoPre = (ImageView)findViewById(R.id.ivVideoPre);
        ivVideoNext = (ImageView)findViewById(R.id.ivVideoNext);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_close:
                finish();
                break;
            case R.id.ivVideoPlay:
                onPlayOrPauseClick();
                break;
            case R.id.ivVideoPre:
                if (path!=null){
                    nextOrPreVideo(path,false);
                }
                break;
            case R.id.ivVideoNext:
                if (path!=null){
                    nextOrPreVideo(path,true);
                }
                break;
        }
    }

    public void onPlayOrPauseClick() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            reQuestPlay();
//            requestVideoAudioFocus();
        }
        ivVideoPlay.setImageResource(videoView.isPlaying() ? R.drawable.selector_video_pause :
                R.drawable.selector_video_play);
    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            reQuestPlay();
            vWidth=  mp.getVideoWidth();
            vHeight = mp.getVideoHeight();
            normalScreen();
        }



    };
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG,"mp"+mp+",what ="+what+",extra ="+extra);
            return false;
        }
    };


    private MediaPlayer.OnCompletionListener MyPlayerOnCompletionListener= new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG,"onCompletion"+mp.getDuration());
            if (path!=null){
                nextOrPreVideo(path,true);
            }
        }
    };


    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            Log.d(TAG,"onProgressChanged:currentPosition"+videoView.getCurrentPosition());
            int currentPosition = videoView.getCurrentPosition();
            int allTime = videoView.getDuration();
            tvMoveTime.setText(TimeUilts.timeTraToHMS(currentPosition)+"/"+ TimeUilts.timeTraToHMS(allTime));


            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            tvMoveTime.measure(spec, spec);
            int moveTimeWidth = tvMoveTime.getMeasuredWidth();
            int sbWidth = videoSeekBar.getMeasuredWidth();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tvMoveTime.getLayoutParams();
            params.leftMargin = (int) (((double) progress / videoSeekBar.getMax()) * sbWidth - (double) moveTimeWidth * progress / videoSeekBar.getMax());
            tvMoveTime.setLayoutParams(params);

            ivVideoPlay.setImageResource(videoView.isPlaying() ? R.drawable.selector_video_pause :
                    R.drawable.selector_video_play);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            videoView.seekTo(seekBar.getProgress());
            reQuestPlay();
        }
    };

    private Disposable disposable;
    private void showTime(){
        disposable = Observable.interval(1,TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        int currentPosition = videoView.getCurrentPosition();
                        int allTime = videoView.getDuration();
                        VideoPlayActivty.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMoveTime.setText(TimeUilts.timeTraToHMS(currentPosition)+"/"+ TimeUilts.timeTraToHMS(allTime));
                                videoSeekBar.setMax( allTime);
                                videoSeekBar.setProgress(currentPosition);
                            }
                        });

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable!=null){
            disposable.dispose();
        }
        if (videoView!=null){
            //停止播放，释放资源
            videoView.stopPlayback();
        }
        releaseAudioFocus();
    }

    //音频焦点调试测试，和AudioFocusUtils重复，后续需要整合。
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private int mLastFocus=0 ;
    private void requestVideoAudioFocus(){
        Log.d(TAG,"mLastFocus ="+mLastFocus);
//        if (mLastFocus ==1||mLastFocus==-1){
//            return;
//        }
        audioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
        attributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE);
        AudioFocusRequest.Builder requestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);

        AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG,"focusChange="+focusChange);
                mLastFocus = focusChange;
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:

//                       releaseAudioFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                        videoView.pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        videoView.start();
                        break;
                    default:
                }
            }
        };
        requestBuilder.setAudioAttributes(attributesBuilder.build())
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener);
        audioFocusRequest = requestBuilder.build();
        int result = audioManager.requestAudioFocus(audioFocusRequest);
//        videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_GAIN);
        Log.d(TAG,"result ="+result);
        if (result==1){
            mLastFocus = AudioManager.AUDIOFOCUS_GAIN;
        }else {
            mLastFocus = AudioManager.AUDIOFOCUS_LOSS;
        }
    }

    private void releaseAudioFocus(){
        if (audioFocusRequest != null) {
            int ret = audioManager.abandonAudioFocusRequest(audioFocusRequest);
            LogUtils.d(TAG, "releaseVoiceAudioFocus, ret=" + ret);
        }
    }
}
