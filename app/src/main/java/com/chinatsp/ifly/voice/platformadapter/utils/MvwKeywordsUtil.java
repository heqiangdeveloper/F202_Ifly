package com.chinatsp.ifly.voice.platformadapter.utils;

import android.content.Context;
import android.media.JetPlayer;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.service.InitializeService;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.entity.KeyWordsEntity;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MvwKeywordsUtil {

    private static final String TAG = "MvwKeywordsUtil";

    /**
     * 更改主唤醒词
     *
     * @param mContext
     * @param defineName1
     * @param defineName2
     * @return
     */
    public static String getChangeKeywordJson(Context mContext, String defineName1, String defineName2) {
        try {
            //如果是新手引导，暂时将“打开空调”添加为唤醒词
            String fileName = "";
            boolean isFirstUse = SharedPreferencesUtils.getBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
            if(isFirstUse){
                fileName = "mvw_global_novice_guide.json";
                Log.d(TAG,"add resource: mvw_global_novice_guide.json");
            }else {
                fileName = "mvw_global.json";
                Log.d(TAG,"add resource: mvw_global.json");
            }
            String globalStr = Utils.getFromAssets(mContext, fileName);
            JSONObject globalObj = new JSONObject(globalStr);
            JSONArray keywordStr = globalObj.getJSONArray("Keywords");
            if (keywordStr.length() > 0) {
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_GLOBAL);
                if (!TextUtils.isEmpty(defineName1)) {
                    keywordStr.getJSONObject(0).put("KeyWord", "你好" + defineName1);
                    keywordStr.getJSONObject(1).put("KeyWord", defineName1 + "你好");
                    SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_CURRENT_NAME_1, defineName1);
                } else {
                    keywordStr.getJSONObject(0).put("KeyWord", "你好小欧");
                    keywordStr.getJSONObject(1).put("KeyWord", "小欧你好");
                    SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_CURRENT_NAME_1, "小欧");
                }
                if (!TextUtils.isEmpty(defineName2)) {
                    keywordStr.getJSONObject(2).put("KeyWord", "你好" + defineName2);
                    keywordStr.getJSONObject(3).put("KeyWord", defineName2 + "你好");
                    SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_CURRENT_NAME_2, defineName2);
                } else {
                    keywordStr.getJSONObject(2).put("KeyWord", "你好欧尚");
                    keywordStr.getJSONObject(3).put("KeyWord", "欧尚你好");
                    SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_CURRENT_NAME_2, "欧尚");
                }
                JSONObject objRoot = new JSONObject();
                objRoot.put("Keywords", keywordStr);
                String globalStr1 = objRoot.toString();
                LogUtils.d(TAG, "globalStr1:\n" + globalStr);
                return globalStr1;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String addMvwKeywordJson(Context mContext,String word){
        if(word!=null&&word.isEmpty()){
            return "";
        }
        try {
            SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_CURRENT_NAME_3, word);
            int index = -1;
            String fileName = "mvw_custom.json";

            String globalStr = Utils.getFromAssets(mContext, fileName);
            JSONObject globalObj = new JSONObject(globalStr);
            JSONArray keywordStr = globalObj.getJSONArray("Keywords");
            Log.d(TAG, "addMvwKeywordJson() called with: mContext = ["  + "], word = [" + keywordStr.length() + "]");

            JSONObject obj1 = new JSONObject();
            obj1.put("DefaultThreshold40",0);
            obj1.put("KeyWord","你好"+word);
            obj1.put("KeyWordId",0);

            JSONObject obj2 = new JSONObject();
            obj2.put("DefaultThreshold40",0);
            obj2.put("KeyWord",word+"你好");
            obj2.put("KeyWordId",1);

            keywordStr.put(obj1);
            keywordStr.put(obj2);

          /*  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_CHANGBA){
                JSONObject obj3 = new JSONObject();
                obj3.put("DefaultThreshold40",0);
                obj3.put("KeyWord","打开原唱");
                obj3.put("KeyWordId",keywordStr.length());

                JSONObject obj4 = new JSONObject();
                obj4.put("DefaultThreshold40",0);
                obj4.put("KeyWord","打开伴唱");
                obj4.put("KeyWordId",keywordStr.length()+1);

                JSONObject obj5 = new JSONObject();
                obj5.put("DefaultThreshold40",0);
                obj5.put("KeyWord","切歌");
                obj5.put("KeyWordId",keywordStr.length()+2);

                keywordStr.put(obj3);
                keywordStr.put(obj4);
                keywordStr.put(obj5);
            }else  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_VIDEO){
                JSONObject obj3 = new JSONObject();
                obj3.put("DefaultThreshold40",0);
                obj3.put("KeyWord","全屏播放");
                obj3.put("KeyWordId",keywordStr.length());

                JSONObject obj4 = new JSONObject();
                obj4.put("DefaultThreshold40",0);
                obj4.put("KeyWord","退出全屏");
                obj4.put("KeyWordId",keywordStr.length()+1);


                keywordStr.put(obj3);
                keywordStr.put(obj4);
            }else  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DVR){


                JSONObject obj3 = new JSONObject();
                obj3.put("DefaultThreshold40",0);
                obj3.put("KeyWord","看后面");
                obj3.put("KeyWordId",keywordStr.length()+2);

                JSONObject obj4 = new JSONObject();
                obj4.put("DefaultThreshold40",0);
                obj4.put("KeyWord","看后边");
                obj4.put("KeyWordId",keywordStr.length()+3);

                JSONObject obj5 = new JSONObject();
                obj5.put("DefaultThreshold40",0);
                obj5.put("KeyWord","看左面");
                obj5.put("KeyWordId",keywordStr.length()+4);

                JSONObject obj6 = new JSONObject();
                obj6.put("DefaultThreshold40",0);
                obj6.put("KeyWord","看左边");
                obj6.put("KeyWordId",keywordStr.length()+5);

                JSONObject obj7 = new JSONObject();
                obj7.put("DefaultThreshold40",0);
                obj7.put("KeyWord","看右面");
                obj7.put("KeyWordId",keywordStr.length()+6);

                JSONObject obj8= new JSONObject();
                obj8.put("DefaultThreshold40",0);
                obj8.put("KeyWord","看右边");
                obj8.put("KeyWordId",keywordStr.length()+7);

                JSONObject obj9= new JSONObject();
                obj9.put("DefaultThreshold40",0);
                obj9.put("KeyWord","看左右");
                obj9.put("KeyWordId",keywordStr.length()+8);

                JSONObject obj10 = new JSONObject();
                obj10.put("DefaultThreshold40",0);
                obj10.put("KeyWord","看前面");
                obj10.put("KeyWordId",keywordStr.length());

                JSONObject obj11 = new JSONObject();
                obj11.put("DefaultThreshold40",0);
                obj11.put("KeyWord","看前边");
                obj11.put("KeyWordId",keywordStr.length()+1);

                keywordStr.put(obj3);
                keywordStr.put(obj4);
                keywordStr.put(obj5);
                keywordStr.put(obj6);
                keywordStr.put(obj7);
                keywordStr.put(obj8);
                keywordStr.put(obj9);
                keywordStr.put(obj10);
                keywordStr.put(obj11);
            }*/
            globalObj.put("Keywords",keywordStr);

            return globalObj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String addChangbaMvwKeywordJson(Context mContext){
        try {
            int index = -1;
            String fileName = "mvw_custom.json";

            String globalStr = Utils.getFromAssets(mContext, fileName);
            JSONObject globalObj = new JSONObject(globalStr);
            JSONArray keywordStr = globalObj.getJSONArray("Keywords");
            Log.d(TAG, "addChangbaMvwKeywordJson: "+keywordStr.length());

            JSONObject obj1 = new JSONObject();
            obj1.put("DefaultThreshold40",0);
            obj1.put("KeyWord","打开原唱");
            obj1.put("KeyWordId",keywordStr.length());

            JSONObject obj2 = new JSONObject();
            obj2.put("DefaultThreshold40",0);
            obj2.put("KeyWord","打开伴唱");
            obj2.put("KeyWordId",keywordStr.length()+1);

            JSONObject obj3 = new JSONObject();
            obj3.put("DefaultThreshold40",0);
            obj3.put("KeyWord","切歌");
            obj3.put("KeyWordId",keywordStr.length()+2);

            keywordStr.put(obj1);
            keywordStr.put(obj2);
            keywordStr.put(obj3);
            globalObj.put("Keywords",keywordStr);
            return globalObj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String addVideoMvwKeywordJson(Context mContext){
        try {
            int index = -1;
            String fileName = "mvw_custom.json";

            String globalStr = Utils.getFromAssets(mContext, fileName);
            JSONObject globalObj = new JSONObject(globalStr);
            JSONArray keywordStr = globalObj.getJSONArray("Keywords");
            Log.d(TAG, "addChangbaMvwKeywordJson: "+keywordStr.length());

            JSONObject obj1 = new JSONObject();
            obj1.put("DefaultThreshold40",0);
            obj1.put("KeyWord","全屏播放");
            obj1.put("KeyWordId",keywordStr.length());

            JSONObject obj2 = new JSONObject();
            obj2.put("DefaultThreshold40",0);
            obj2.put("KeyWord","退出全屏");
            obj2.put("KeyWordId",keywordStr.length()+1);


            keywordStr.put(obj1);
            keywordStr.put(obj2);
            globalObj.put("Keywords",keywordStr);
            return globalObj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String addDvrMvwKeywordJson(Context mContext){
        try {
            int index = -1;
            String fileName = "mvw_custom.json";

            String globalStr = Utils.getFromAssets(mContext, fileName);
            JSONObject globalObj = new JSONObject(globalStr);
            JSONArray keywordStr = globalObj.getJSONArray("Keywords");
            Log.d(TAG, "addChangbaMvwKeywordJson: "+keywordStr.length());

            JSONObject obj1 = new JSONObject();
            obj1.put("DefaultThreshold40",0);
            obj1.put("KeyWord","看前面");
            obj1.put("KeyWordId",keywordStr.length());

            JSONObject obj2 = new JSONObject();
            obj2.put("DefaultThreshold40",0);
            obj2.put("KeyWord","看前边");
            obj2.put("KeyWordId",keywordStr.length()+1);

            JSONObject obj3 = new JSONObject();
            obj3.put("DefaultThreshold40",0);
            obj3.put("KeyWord","看后面");
            obj3.put("KeyWordId",keywordStr.length()+2);

            JSONObject obj4 = new JSONObject();
            obj4.put("DefaultThreshold40",0);
            obj4.put("KeyWord","看后边");
            obj4.put("KeyWordId",keywordStr.length()+3);

            JSONObject obj5 = new JSONObject();
            obj5.put("DefaultThreshold40",0);
            obj5.put("KeyWord","看左面");
            obj5.put("KeyWordId",keywordStr.length()+4);

            JSONObject obj6 = new JSONObject();
            obj6.put("DefaultThreshold40",0);
            obj6.put("KeyWord","看左边");
            obj6.put("KeyWordId",keywordStr.length()+5);

            JSONObject obj7 = new JSONObject();
            obj7.put("DefaultThreshold40",0);
            obj7.put("KeyWord","看右面");
            obj7.put("KeyWordId",keywordStr.length()+6);

            JSONObject obj8= new JSONObject();
            obj8.put("DefaultThreshold40",0);
            obj8.put("KeyWord","看右边");
            obj8.put("KeyWordId",keywordStr.length()+7);

            JSONObject obj9= new JSONObject();
            obj9.put("DefaultThreshold40",0);
            obj9.put("KeyWord","看左右");
            obj9.put("KeyWordId",keywordStr.length()+8);

            keywordStr.put(obj1);
            keywordStr.put(obj2);
            keywordStr.put(obj3);
            keywordStr.put(obj4);
            keywordStr.put(obj5);
            keywordStr.put(obj6);
            keywordStr.put(obj7);
            keywordStr.put(obj8);
            keywordStr.put(obj9);
            globalObj.put("Keywords",keywordStr);
            return globalObj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }



    //删除除了自定义唤醒词以外其他的唤醒词
    public static void removeCunstomMVwWords(Context context){
        MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CUSTOME);
        //增加自定义唤醒词
        String defineName1 = SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_3, "");
        if(defineName1!=null&&!defineName1.isEmpty()){
            String otherStr = MvwKeywordsUtil.addMvwKeywordJson(context,defineName1);
            MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, otherStr);
        }
    }

    public static String getCurrentName(Context context) {
        int whichName = SharedPreferencesUtils.getInt(context, AppConstant.KEY_WHICH_NAME, 0);
        if (whichName == 0) {
            return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_1, "小欧");
        } else if((whichName == 1)){
            return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_2, "欧尚");
        }else if((whichName == 2))
            return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_3, "小欧");
        else
            return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_1, "小欧");
    }

    public static String getKeyName1(Context context) {
        return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_1, "小欧");
    }

    public static String getKeyName2(Context context) {
        return SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_NAME_2, "欧尚");
    }

    public static String getKeyWordByKeyWordIdScene(Context context, int sceneId, int keywordId) {
        String keyWord = "";
        List<KeyWordsEntity> keyWordsEntityList = new ArrayList<>();
        if (context == null) {
            LogUtils.i(TAG, "context == null");
            return keyWord;
        }
        if (sceneId == TspSceneAdapter.TSP_SCENE_GLOBAL) {
            keyWordsEntityList.addAll(GsonUtil.stringToList(Utils.getFromAssets(context, "mvw_global.json"), KeyWordsEntity.class));
        }
        for (int i = 0; i < keyWordsEntityList.size(); i++) {
            if (keyWordsEntityList.get(i).KeyWordId == keywordId) {
                keyWord = keyWordsEntityList.get(i).KeyWord;
                break;
            }
        }
        return keyWord;
    }

    /**
     * 获取音乐的唤醒词
     */
    public static String getMusicKeyWords() {
        String[] strWords = {"开始", "暂停","取消" ,"上一首", "下一首", "单曲循环", "随机播放", "全部循环" };

        JSONObject objRoot = new JSONObject();
        JSONArray objArr = new JSONArray();
        for (int nIndex = 0; nIndex != strWords.length; nIndex++) {
            JSONObject objWord = new JSONObject();
            try {
                objWord.put("KeyWordId", nIndex);
                objWord.put("KeyWord", strWords[nIndex]);
                objWord.put("DefaultThreshold40", 0);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            objArr.put(objWord);
        }
        try {
            objRoot.put("Keywords", objArr);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return objRoot.toString();
    }
}
