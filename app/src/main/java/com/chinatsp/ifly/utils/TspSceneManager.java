package com.chinatsp.ifly.utils;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.sqlite.SQLiteOutOfMemoryException;
import android.util.Log;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.service.InitializeService;
import com.chinatsp.ifly.voice.platformadapter.controller.FeedBackController;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

public class TspSceneManager {

    private static String TAG = "xyj_TspSceneManager";
    private static TspSceneManager loadClass = new TspSceneManager();
    public static TspSceneManager getInstance(){
        return loadClass;
    }

    public void switchSceneToChangba(Context context){
//        String otherStr = MvwKeywordsUtil.addChangbaMvwKeywordJson(context);
//        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, otherStr);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_CHANGBA);
    }

    public void switchSceneToVideo(Context context){
//        String otherStr = MvwKeywordsUtil.addVideoMvwKeywordJson(context);
//        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, otherStr);
//        MVWAgent.getInstance().stopMVWSession();
//        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_VIDEO);
    }

    public void switchSceneToDVR(Context context){
//        String otherStr = MvwKeywordsUtil.addDvrMvwKeywordJson(context);
//        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, otherStr);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DVR);
    }

    public void resetCustomWvms(Context context){
        MvwKeywordsUtil.removeCunstomMVwWords(context);
    }


    public void resetScrene(Context context, int lastScene){
        Log.d(TAG, "-----resetMVWsession------ "  + CarUtils.getInstance(context).isAvmOpen()
                +"...:"+ActiveServiceViewManager.ActiveServiceView_Show
                +"...."+MXSdkManager.getInstance(context).isExitShowShow()
                +"...."+SearchListFragment.isShown
                +"..."+BluePhoneManager.getInstance(context).getCallStatus()
                +"..."+lastScene);

        MVWAgent.getInstance().stopMVWSession();

        //是不是在来电中
        if(BluePhoneManager.getInstance(context).getCallStatus() == CallContact.CALL_STATE_INCOMING){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_CALL);
        }

        //当前是不是全景界面 切换
        else if(CarUtils.getInstance(context).isAvmOpen()){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DVR);
        }
        //当前 微信位置倒车 是不是在前台
        else if(ActiveServiceViewManager.ActiveServiceView_Show){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_CONFIRM);
        }
        //当前在人脸识别界面，切换到confirm场景
        else if(!FloatViewManager.getInstance(context).isHide()
               && FeedBackController.getInstance(context).getShowType()!=FeedBackController.INVALID_TYPE){
            Log.d(TAG, "resetScrene() called with: context = [" +  FeedBackController.getInstance(context).getShowType() + "], lastScene = [" + lastScene + "]");
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_FEEDBACK);
        }

        //当前 退出导航界面是不是在前台

        else  if(MXSdkManager.getInstance(context).isExitShowShow()){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DIALOG);
        }
        //s说明当前是选择列表界面
        else if(SearchListFragment.isShown){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
        } else if (MXSdkManager.getInstance(context).isForeground() || MXSdkManager.getInstance(context).isNaving()) {
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        }/***********欧尚修改开始*****************/
        else if(GDSdkManager.getInstance(context).isGDForeground()){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        }
        /***********欧尚修改结束*****************/
        else if (lastScene != -1)
            MVWAgent.getInstance().startMVWSession(lastScene);
        else {
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        }
    }




}
