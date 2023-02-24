package com.chinatsp.ifly.module.me.recommend.view;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;


import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.LongDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.VideoPlayActivty;
import com.chinatsp.ifly.module.me.recommend.Utils.Constants;
import com.chinatsp.ifly.module.me.recommend.adapter.VideoAdapter;
import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.video.VideoModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/17
 */

public class VoiceRecommendFragment extends BaseFragment implements View.OnClickListener,HuVoiceAsssitContentModel.onCallbackInterface {
    private final static String TAG = "VoiceRecommendFragment";
    private SettingsActivity activity;
    private Context appContext;
    private RecyclerView videoList;
    private RecyclerView.LayoutManager mLayoutManager;
    private ImageView testImageView;

    private VideoAdapter videoAdapter;
    private Object videoData;
    private ContentResolver contentResolver;
    private VideoDialog videoDialog;
    private boolean isHidden =true;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG,"hidden ="+hidden);
        isHidden = hidden;
        if (!isHidden){
            updateVideoData();
            DatastatManager.getInstance().update_video_state(appContext,VideoListUtil.getInstance(appContext).getWholeList());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
        appContext = context.getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
//        videoAdapter.setData(getVideoData());
        if (!isHidden){
            updateVideoData();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        Log.d(TAG,"initData");
        HuVoiceAsssitContentModel.getInstance().setOnCallbackListener(this);
        contentResolver = activity.getContentResolver();
//        initVideoData();
        VideoListUtil.getInstance(getContext()).initVideoData();
        videoAdapter = new VideoAdapter(getVideoData(), VoiceRecommendFragment.this.appContext);
        videoList.setAdapter(videoAdapter);
        Log.d(TAG,"getWholeList ="+ VideoListUtil.getInstance(appContext).getWholeList().size());
        initContentResolver(appContext);
    }

    private ContentObserver mContentObserver;
    private void initContentResolver(Context context) {
        if (mContentObserver == null) {
            mContentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange);
                    if (uri.toString().equalsIgnoreCase("content://com.chinatsp.ifly.videodata/VideoData")){
                        videoAdapter.setData(getVideoData());
                    }
                }
            };
        }
        appContext.getContentResolver().registerContentObserver(videoUri, false, mContentObserver);
    }

//    private List<String> voideoPathList = new ArrayList<>();

    private List<VideoDataBean> getVideoData(){
        List<VideoDataBean> videoDataBeanList = VideoListUtil.getInstance(appContext).getWholeList();
        Log.d(TAG,"videoDataBeanList ="+videoDataBeanList.size());
        return  videoDataBeanList;
    }
    private String AUTHORITY ="com.chinatsp.ifly.videodata";
    private Uri videoUri = Uri.parse("content://" + AUTHORITY + "/VideoData");
    private void initVideoData(){
        int videoId=0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = format.format(new Date());
        getVideoCoverList();
        getVideoContentList();
        for (int i=0;i<videoCoverList.size();i++){
            String picPath = videoCoverList.get(i);
//            String videoPath = videoContentList.get(i);
            String videoPath = picPath.replace("videocover","videocontent").replace("png","mp4");
            if(VideoListUtil.getInstance(activity).isVideoExist(videoNameList.get(i))){
                Log.e(TAG, "isVideoExist:"+videoPath);
                continue;
            }
            if (videoNameList.get(i).contains("DMS驾驶员关怀系统")){
                videoId = 32;
            }else if(videoNameList.get(i).contains("常用免唤醒词")){
                videoId =33;
            }else if (videoNameList.get(i).contains("太空舱座椅休息模式")){
                videoId =34;
            }
            ContentValues values = new ContentValues();
            values.put("_id",videoId);
            values.put("read", "false");
            values.put("time", date);
            values.put("pic_path", picPath);
            values.put("path", videoPath);
            values.put("name", videoNameList.get(i));
            values.put("parent_path", Constants.video_content_path);
            contentResolver.insert(videoUri, values);
        }
    }

    private List<String> videoNameList = new ArrayList<>();
    private List<String> videoCoverList = new ArrayList<>();
    private List<String> videoContentList = new ArrayList<>();
    private List<String> getVideoCoverList(){
        File scannerDirectory = new File(Constants.video_content_path);
        Log.d(TAG,"getPicTest ="+scannerDirectory);
        if (scannerDirectory.isDirectory()) {
            for (File file : scannerDirectory.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
                    Log.d(TAG,"path ="+path);
                    String name = file.getName().replace(".png","");
                    if (name.equalsIgnoreCase("caresystem")){
                        name = activity.getResources().getString(R.string.care_system);
                    }else if (name.equalsIgnoreCase("mvwwords")){
                        name = activity.getResources().getString(R.string.mvw_words);
                    }else if (name.equalsIgnoreCase("sleepmode")){
                        name = activity.getResources().getString(R.string.sleep_mode);
                    }
                    videoNameList.add(name);
                    videoCoverList.add(path);
                }
            }
        }
        return videoCoverList;
    }

    private List<String> getVideoContentList(){
        File scannerDirectory = new File(Constants.video_content_path);
        Log.d(TAG,"getVideoTest ="+scannerDirectory);
        if (scannerDirectory.isDirectory()) {
            for (File file : scannerDirectory.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".mp4")) {
                    Log.d(TAG,"path ="+path);
                    videoContentList.add(path);
                }
            }
        }
        return videoContentList;
    }

    private void getVideoBitmap(String path){
        Observable.just(path)
                 .map(new Function<String, Bitmap>() {
                     @Override
                     public Bitmap apply(String s) throws Exception {
                         Log.d(TAG,"s ="+s);
                         Bitmap bitmap = null;
                         MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                         int kind = MediaStore.Video.Thumbnails.MINI_KIND;
                         if (Build.VERSION.SDK_INT >= 14) {
                             retriever.setDataSource(path, new HashMap<String, String>());
                         } else {
                             retriever.setDataSource(path);
                         }
                         bitmap = retriever.getFrameAtTime();
                         if (bitmap== null){

                         }
                         return bitmap;
                     }
                 }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        Log.d(TAG,"bitmap ="+bitmap.getWidth());
                        testImageView.setImageBitmap(bitmap);
                    }
                });
    }


    @Override
    protected void initListener() {
        testImageView.setOnClickListener(this);
    }

    @Override
    protected void initView(View view) {
        videoList = (RecyclerView)view.findViewById(R.id.video_list);
        videoList.setHasFixedSize(true);
        if (mLayoutManager == null) {
            mLayoutManager = new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL,
                    false);
        }
//        mLayoutManager = new LinearLayoutManager(appContext,
//                LinearLayoutManager.HORIZONTAL, false) {
//            @Override
//            public boolean canScrollVertically() {
//                return false;
//            }
//        };
        videoList.setLayoutManager(mLayoutManager);
        videoList.setItemAnimator(null);
        testImageView = (ImageView) view.findViewById(R.id.testImageView);

    }



    @Override
    protected int getContentView() {
        return R.layout.fragment_voice_recommend;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.testImageView:
                FloatViewManager.getInstance(activity).hide();
                Intent intent = new Intent(activity, VideoPlayActivty.class);
                intent.putExtra("path","/storage/emulated/0/TEMPFILE/test.mp3");
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onFailure(int code, String errorMessage, int position) {
        Log.d(TAG,":onProgress:position"+position);
    }

    @Override
    public void onProgress(float progress, long total, int position) {

    }

    @Override
    public void onResponse(File response, int position) {
        Log.d(TAG,":onResponse:position"+position);
        Log.d(TAG,"response ="+response.getPath());
        if (position ==0){
            updateVideoData();
            Log.d(TAG,"getWholeList ="+ VideoListUtil.getInstance(appContext).getWholeList());
        }
    }

    private void updateVideoData(){
        Log.d(TAG,"getBoolean ="+SharedPreferencesUtils.getBoolean(activity,"isRemind",true)+",isShowRecommand=");
        if (!SharedPreferencesUtils.getBoolean(activity,"isRemind",true)){
            return;
        }
        if((getVideoData().size()!=0)&&getVideoData().get(0).getRead().equalsIgnoreCase("false")){
            if (videoDialog==null){
                videoDialog = new VideoDialog(activity);
            }
            if(videoDialog.isShowing()){
                return;
            }
            videoDialog.initData(getVideoData().get(0).getPicPath(),getVideoData().get(0).getVideoName());
            videoDialog.setClickListener(new VideoDialog.ClickListener() {
                @Override
                public void onPlay() {
                    startVideoActivity(getVideoData().get(0).getVideoName(),getVideoData().get(0).getVideoPath());
                }

                @Override
                public void onCancel() {
                    SharedPreferencesUtils.saveBoolean(appContext,"isRemind",false);
                }
            });
        }
    }

    public void startVideoActivity(String videoName,String videoPath){
        Intent intent = new Intent("com.coagent.intent.action.video");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ps", 1);
        intent.putExtra("name", videoName);
        intent.putExtra("data", "VideoData");
        intent.putExtra("dataType", 8);
        intent.putExtra("extra_vedio_from", "videoInIfly");
        intent.putExtra("id",VideoListUtil.getInstance(activity).getIdByPath(videoPath)+"");
        activity.startActivity(intent);
    }

    public void getVideoData(String path) {
        Observable.just(path)
                .map(new Function<String, List<VideoModel>>() {
                    @Override
                    public List apply(String s) throws Exception {
//                        return VideoDataImpl.getInstance().getCurrentDirVideo(appContext, path);
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List>() {
                    @Override
                    public void accept(List list) throws Exception {
                        Log.d(TAG,"accept :list="+list.size());
                    }
                });
    }
}
