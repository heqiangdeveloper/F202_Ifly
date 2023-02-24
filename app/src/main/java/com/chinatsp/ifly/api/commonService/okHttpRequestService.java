package com.chinatsp.ifly.api.commonService;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.activeservice.AudioSynthesis;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.TtsEntity;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.OkHttpUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * author:liuhong
 * 网络请求公共类
 */
public class okHttpRequestService {
    private static final String TAG = "okHttpRequestService";
    private static int i = 0;

    public static okHttpRequestService getInstance() {
        return okHttp.model;
    }

    private static class okHttp {
        private final static okHttpRequestService model = new okHttpRequestService();
    }

    /**
     *
     */
    public void getHuTtsData(final Context context, String token, String projectId, String projectVersion) {
        Map<String, String> values = new HashMap<>();
        values.put("access_token", token);
        values.put("timestamp", System.currentTimeMillis() + "");
        values.put("projectid", projectId);
        if (!TextUtils.isEmpty(projectVersion)) {
            values.put("projectversion", projectVersion);
        }
        OkHttpUtils.postFormData(AppConstant.HU_TTS_DATA_URL, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                ThreadPoolUtils.executeSingle(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "lh:response-" + response);
                        //保存更新时间
                        SharedPreferencesUtils.saveLong(context, AppConstant.LAST_UPDATE_TTS_TIME, System.currentTimeMillis());

                        TtsEntity ttsEntity = null;
                        TtsEntity.ttsVersionInfo ttsVersionInfo = null;
                        try {
                             ttsEntity = GsonUtil.stringToObject(response, TtsEntity.class);
                             ttsVersionInfo = ttsEntity.data;
                             Log.d(TAG, "lh:response-ttsEntity-" + ttsEntity == null ? null : ttsEntity.toString());


                        //返回成功，0表示有待更新的数据,-1 表示失败
                        if ("0".equals(ttsEntity.code) && (ttsVersionInfo != null)) {
                            Log.d(TAG, "lh:response-need update-" + ",-Thread-" + Thread.currentThread());
                            SharedPreferencesUtils.saveString(context, AppConstant.KEY_TTS_PROJECT_VERSION, ttsVersionInfo.newversion);
                            SharedPreferencesUtils.saveString(context, AppConstant.KEY_TTS_PROJECT_ID, ttsEntity.projectid);
                            //删除表中所有数据
                            TtsInfoDbDao.getInstance(context).deleteAllData();
                            //插入待更新数据
                            TtsInfoDbDao.getInstance(context).updateTtsDb(changeBean(ttsVersionInfo.newversiondata));
                            //检查更新后更新合成的音频
                            AudioSynthesis.getInstance(context).GetAudio();

                            TtsUtils.getInstance(context).getTXZConfigeration();//获取同行者配置

                            TtsUtils.getInstance(context).getTTsMarks();

                            TtsUtils.getInstance(context).getOnlineConfigureSpeed();

                            TtsUtils.getInstance(context).getDbTryGuideTts();

                            TtsUtils.getInstance(context).getFfrTts();

                        }else {
                            if (("203501").equals(ttsEntity.errCode)){ //token 过期,重新请求数据
                                i++;
                                if (i < 3){
//                                    AppConfig.INSTANCE.RequestUpdateAccessToken();
                                    ActiveViewService.getTtsMessage();
                                }
                                Log.e(TAG,"lh:response-need update--------------token过期重新请求");
                            }else if("-1".equals(ttsEntity.code)){
                                TtsUtils.getInstance(context).getTXZConfigeration();
                                TtsUtils.getInstance(context).getTTsMarks();
                                TtsUtils.getInstance(context).getOnlineConfigureSpeed();
                                TtsUtils.getInstance(context).getDbTryGuideTts();
                                TtsUtils.getInstance(context).getFfrTts();
                            }
                        }
                        }catch (Exception e){
                            Log.d(TAG, " okHttpRequestService 服务器数据发生错误");
                            return;
                        }
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "lh:response-fail:" + e.toString() + "," + e.getMessage());
            }
        }, values);
    }

    private List<TtsInfo> changeBean(List<TtsEntity.skillTtsInfo> newversiondata) {
        List<TtsInfo> ttsInfoList = new ArrayList<>();
        if (newversiondata != null && newversiondata.size() > 0) {
            for (TtsEntity.skillTtsInfo skillTtsInfo : newversiondata) {
                List<TtsEntity.conditionTtsInfo> conditionTtsInfoList = skillTtsInfo.data;
                if (conditionTtsInfoList != null && conditionTtsInfoList.size() > 0) {
                    for (TtsEntity.conditionTtsInfo conditionTtsInfo : conditionTtsInfoList) {
                        List<TtsEntity.ttsTextInfo> ttsTextInfoList = conditionTtsInfo.data;
                        if (ttsTextInfoList != null && ttsTextInfoList.size() > 0) {
                            for (TtsEntity.ttsTextInfo ttsTextInfo : ttsTextInfoList) {
                                TtsInfo ttsInfo = new TtsInfo();
                                ttsInfo.setSkillId(skillTtsInfo.skillid);
                                ttsInfo.setSkillVersion(skillTtsInfo.skillversion);
                                ttsInfo.setConditionId(conditionTtsInfo.conditionid);
                                ttsInfo.setTtsId(ttsTextInfo.ttsid);
                                ttsInfo.setTtsText(ttsTextInfo.ttsText);
                                ttsInfo.setBaseResponse(ttsTextInfo.baseResponse);
                                ttsInfo.setValid_starttime(ttsTextInfo.valid_starttime);
                                ttsInfo.setVelid_endtime(ttsTextInfo.velid_endtime);
                                ttsInfo.setOffline_broadcast(ttsTextInfo.offline_broadcast);
                                ttsInfo.setIsTtsAvailable(conditionTtsInfo.isTtsAvailable);
                                ttsInfoList.add(ttsInfo);
                            }
                        }
                    }
                }
            }
        }
        return ttsInfoList;
    }

}
