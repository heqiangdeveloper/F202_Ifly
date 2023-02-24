package com.chinatsp.ifly.module.me.recommend.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TableRow;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.CommandDbDao;
import com.chinatsp.ifly.db.CommandProvider;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.CommandEvent;
import com.chinatsp.ifly.module.me.recommend.Utils.Constants;
import com.chinatsp.ifly.module.me.recommend.Utils.FileUtils;
import com.chinatsp.ifly.module.me.recommend.bean.HuVoiceAssitContentBean;
import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.module.me.recommend.urlhttp.CallBackUtil;
import com.chinatsp.ifly.module.me.recommend.urlhttp.UrlHttpUtil;
import com.chinatsp.ifly.module.me.recommend.view.ManageFloatWindow;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.proxy.IVehicleNetworkRequestCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/15
 */

public class HuVoiceAsssitContentModel{
    private final static String TAG ="HuVoiceAsssitContentModel";
    private static HuVoiceAsssitContentModel huVoiceAsssitContentModel;
    private onCallbackInterface onCallbackInterface;
    private onCommandCallbackInterface monCommandCallbackInterface;
    public static synchronized HuVoiceAsssitContentModel getInstance(){
        if (huVoiceAsssitContentModel==null){
            huVoiceAsssitContentModel = new HuVoiceAsssitContentModel();
        }
        return huVoiceAsssitContentModel;
    }
    public Boolean isNetwork=true;
    private Context mContext;
    private int i=0;
    //是否是最新版本
    public Boolean isNewContentVision;
    private HuVoiceAssitContentBean  huVoiceAssitContentBean;


    public interface onCallbackInterface{
        void onFailure(int code, String errorMessage,int position);
        void onProgress(float progress, long total,int position);
        void onResponse(File response,int position);
    }

    public void setOnCallbackListener(onCallbackInterface l) {
        onCallbackInterface = l;
    }

    public interface onCommandCallbackInterface{
        void onCommandDownloadStatus(boolean finish);
    }

    public void setOnCommandCallbackListener(onCommandCallbackInterface l) {
        monCommandCallbackInterface = l;
    }



    public void getHuVoiceAsssitContent(Context context,String token, String projectId, String projectVersion){
        getVideoNameList();
        Log.d(TAG,"token="+AppConfig.INSTANCE.token);
        Log.d(TAG,"projectVersion ="+projectVersion);
        if (token==null){
            isNetwork =false;
        }
        mContext = context;
        if (AppConfig.INSTANCE.token==null|| TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            return;
        }
        String mill=  System.currentTimeMillis()+"";
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken",token);
        params.put("timestamp",mill);
        params.put("projectId", projectId);
        params.put("contentVersion",projectVersion);
        String Url = AppConstant.BACK_DATA_URL;
        Log.d(TAG,"getHuVoiceAsssitContent_RUL"+ Url);
        Log.d(TAG,""+Thread.currentThread());
        VehicleNetworkManager.getInstance().requestNet(Url,"POST",params, new IVehicleNetworkRequestCallback(){

            @Override
            public void onSuccess(String s) {
                LogUtils.debugLarge(TAG,s);
                try {
                    huVoiceAssitContentBean = new Gson().fromJson(s,HuVoiceAssitContentBean.class);
                } catch (Exception e) {
                    Log.d(TAG, " 服务器数据出错: ");
                }
                if (huVoiceAssitContentBean.getErrCode() == 208105){
                    AppConstant.DOWNLOAD_FINISEH = true;
                    if(monCommandCallbackInterface!=null)
                        monCommandCallbackInterface.onCommandDownloadStatus(true);
//                    EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.COMMAND,null,null));
                    return;
                }else {
                    try {
                        EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.JUMP,huVoiceAssitContentBean.getData().getNewContentVersionData().getXiaoouText(),huVoiceAssitContentBean.getData().getNewContentVersionData().getJumpModule()));
                        SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_TTS_CONTENT_VERSION, huVoiceAssitContentBean.getData().getNewContentVersion());
                        List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.VideosBean> videos = huVoiceAssitContentBean.getData().getNewContentVersionData().getVideos();
                        for (i=0;i<videos.size();i++){
                            if (FileUtils.getInstance(mContext).isUsableSpace()){
                                getUrlQuest(videos.get(i).getVideoContentUrl(),videos.get(i).getVideoTitle()+".mp4","videocontent",i+1,videos.get(i).getVideoId());
                            }else {
                                FileUtils.getInstance(mContext).deleteSpaceIsUsable(new FileUtils.ResultCallback() {
                                    @Override
                                    public void isOnSuccess(boolean isUsable) {
                                        if (isUsable){
                                            getUrlQuest(videos.get(i).getVideoContentUrl(),videos.get(i).getVideoTitle()+".mp4","videocontent",i+1,videos.get(i).getVideoId());
                                        }
                                    }
                                });
                            }
//                        String video = "video_seekbar"+i;
//                        Log.d(TAG,"video_seekbar ="+video);
//                        SharedPreferencesUtils.saveBoolean(mContext,video,false);
                        }
                        Log.d(TAG, "onSuccess: "+monCommandCallbackInterface);
                        if(monCommandCallbackInterface!=null)
                            monCommandCallbackInterface.onCommandDownloadStatus(false);
                        CommandDbDao.getInstance(context).updateCommandDb(huVoiceAssitContentBean.getData().getNewContentVersionData().getInstructs(), new onCommandCallbackInterface() {
                            @Override
                            public void onCommandDownloadStatus(boolean finish) {
                                if(monCommandCallbackInterface!=null)
                                    monCommandCallbackInterface.onCommandDownloadStatus(finish);
                                if(finish)
                                    EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.COMMAND,null,null));
                            }
                        });
//                    handler.sendEmptyMessage(SHOW_TEXT);
                        String xiaoText =huVoiceAssitContentBean.getData().getNewContentVersionData().getXiaoouText();
                        String jumpModule = huVoiceAssitContentBean.getData().getNewContentVersionData().getJumpModule();
                        updateToMap(xiaoText,jumpModule);
                    } catch (Exception e) {
                        Log.d(TAG, " 解析出错: ");
                    }
                }
                isNetwork = true;
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG,"onError -S ="+s.toString());
                if (s.equals("network error")){
                    Log.d(TAG,"isNetwork false");
                    isNetwork =false;
                }
            }

            @Override
            public void onProgress(float v) {

            }
        });

    }


    private void updateToMap(String xiaoouText,String jumpModule){
        Log.d(TAG,"xiaoouText ="+xiaoouText);
        Log.d(TAG,"jumpModule ="+jumpModule);
        Intent intent = new Intent();
        intent.putExtra("xiaoouText",xiaoouText);
        intent.putExtra("jumpModule",jumpModule);
        intent.setPackage("com.tencent.wecarnavi");
        intent.setAction("com.chinatsp.ifly.JUMP_DATA");
        mContext.sendBroadcast(intent);
    }

    public synchronized void getUrlQuest(String path,String fileName,String dirName,int position,int id){
        Log.d(TAG,"fileName ="+fileName+",id="+id);
        List<TtsInfo> ttsInfoList= TtsInfoDbDao.getInstance(mContext).queryTtsInfo("mainC_blacklist_video");
        Log.d(TAG,"ttsInfoList ="+ttsInfoList);
        if (ttsInfoList.size()>0){
            for (TtsInfo TtsInfo:ttsInfoList){
                if (TtsInfo.getTtsText().contains(id+"")){
                    return;
                }
            }
         }
       if (VideoListUtil.getInstance(mContext).isVideoExistById(id)){
             return;
       }

        UrlHttpUtil.downloadFile(path,position,id, new CallBackUtil.CallBackFile(""+ FileUtils.getRootFile(dirName),fileName) {
            @Override
            public void onFailure(int code, String errorMessage,int position,int id) {
                Log.d("qlf =","onFailure:position"+position);
                if (onCallbackInterface!=null){
                    onCallbackInterface.onFailure(code,errorMessage,position);
                }
            }

            @Override
            public void onProgress(float progress, long total,int position,int id) {
                super.onProgress(progress, total,position,id);
//                Log.d("qlf",":onProgress:position"+position);
//                Log.d("qlf","progress ="+progress+",total="+total);
                if (onCallbackInterface!=null){
                    onCallbackInterface.onProgress(progress,total,position);
                }
            }

            @Override
            public void onResponse(File response,int position,int id) {
                Log.d(TAG,":onResponse:position"+position+",id="+id);
//                String video = "video_seekbar"+position;
//                SharedPreferencesUtils.saveBoolean(mContext,video,true);
                Log.d(TAG,"onResponse="+Thread.currentThread().getName());
                if (position!=0){
                    loadPictureFile(position,id);
                }else {
                    Log.d(TAG,"response.getPath ="+response.getPath());
                }
                //视频下载完 再下载视频封面并显示视频封面
                if (onCallbackInterface!=null && position==0){
                    savaVideoData(response.getPath(),response.getName(),id);
                    onCallbackInterface.onResponse(response,position);
                }
            }
        });
    }


    private void loadPictureFile(int position,int id){
        List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.VideosBean> videos = huVoiceAssitContentBean.getData().getNewContentVersionData().getVideos();
        getUrlQuest(videos.get(position-1).getVideoCoverUrl(),videos.get(position-1).getVideoTitle()+".png","videocover",0,id);
    }

    
    private void savaVideoData(String picPath,String videoName,int id){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());
        Log.d(TAG,"picPath ="+picPath+",videoName ="+videoName);
        String mVideoName = videoName.replace("png","mp4");
        Log.d(TAG,"mVideoName ="+mVideoName);
        String videoPath = picPath.replace("png","mp4").replace("videocover","videocontent");
        Log.d(TAG,"videoPath ="+videoPath);
//        int videoListSize=VideoListUtil.getInstance(mContext).getWholeList().size();
//        Log.d(TAG,"videoListSize ="+videoListSize);
//        int id;
//        if (videoListSize==0){
//            id =0;
//        }else {
//            id= videoListSize;
//        }
        //暂时测试用这个路径，后续路径得修改
//        String parent_path ="/storage/emulated/0/iflytek/videocontent";
//        VideoDataBean videoDataBean = new VideoDataBean(id,"false",date,picPath,videoPath,videoName,parent_path);
//        VideoListUtil.getInstance(mContext).insert(videoDataBean);
        ContentResolver contentResolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put("read", "false");
        values.put("time", date);
        values.put("pic_path", picPath);
        values.put("path", videoPath);
        values.put("name", mVideoName);
        values.put("parent_path", Constants.video_content_path);
        contentResolver.insert(Constants.VIDEODATA_URL, values);
    }

    private List<String> videoNameList = new ArrayList<>();
    private List<String> getVideoNameList(){
        File scannerDirectory = new File(Constants.video_cover_content_path2);
        if (scannerDirectory.isDirectory()) {
            for (File file : scannerDirectory.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
                    String name = file.getName().replace(".png","");
                    videoNameList.add(name);
                }
            }
        }
        return videoNameList;
    }
}
