package com.chinatsp.ifly.voice.platformadapter.utils;

import android.content.Context;
import android.text.TextUtils;

import com.chinatsp.ifly.utils.FileUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.entity.MultiSemantic;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;

public class MultiInterfaceUtils {
    private static final String TAG = "MultiInterfaceUtils";
    private static final String FILE_NAME = "multi_semantic.json";
    private Context mContext;
    private static MultiInterfaceUtils instance = null;

    private MultiInterfaceUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized MultiInterfaceUtils getInstance(Context context) {
        if (instance == null) {
            instance = new MultiInterfaceUtils(context);
        }
        return instance;
    }

    public MultiSemantic readSavedMultiSemantic() {
        String jsonStr = FileUtils.readFileData(mContext, FILE_NAME);
        MultiSemantic semantic = null;
        LogUtils.d(TAG, "readSavedMultiSemantic:" + jsonStr);
        try {
            if (!TextUtils.isEmpty(jsonStr)) {
                semantic = GsonUtil.stringToObject(jsonStr, MultiSemantic.class);
            }
        } catch (JsonSyntaxException e) {
            LogUtils.e(TAG, e.getMessage());
        }
        return semantic;
    }

    public void clearMultiInterfaceSemantic() {
        boolean ret = FileUtils.deleteFile(mContext, FILE_NAME);
        LogUtils.d(TAG, "clearMultiInterfaceSemantic:" + ret);
    }

    public void saveMultiInterfaceSemantic(IntentEntity intentEntity) {
        JsonObject root = new JsonObject();
        root.addProperty("service", intentEntity.service);
        root.addProperty("operation", intentEntity.operation);
        if (intentEntity.semantic != null) {
            String semanticJson = GsonUtil.objectToString(intentEntity.semantic);
            JsonObject semantic = new JsonParser()
                    .parse(semanticJson)
                    .getAsJsonObject();
            root.add("semantic", semantic);
        }
        LogUtils.d(TAG, "saveMultiInterfaceSemantic:" + root.toString());
        FileUtils.writeFileData(mContext, FILE_NAME, root.toString());
    }

    public void uploadMapUMoreTargetData(final String result, final String semantic) {

        Runnable mUploadMapUMoreTargetRunnable = new Runnable() {
            @Override
            public void run() {
                JsonObject dataList = new JsonObject();
                JsonArray resultArray = new JsonParser().parse(result).getAsJsonArray();
                dataList.add("result", resultArray);

                JsonObject semanticObject = new JsonParser().parse(semantic).getAsJsonObject();
                dataList.add("semantic", semanticObject);

                JsonObject data = new JsonObject();
                JsonObject dataInfo = new JsonParser()
                        .parse("{\"avoid_poi\": {},\"end_poi\": {},\"reference_poi\": {},\"start_poi\": {},\"via_poi\": {}}")
                        .getAsJsonObject();

                data.add("dataInfo", dataInfo);
                data.add("dataList", dataList);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", "fg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", "moreTarget");
                serviceScene.addProperty("selectStatus", "endLoc");

                JsonObject UserData = new JsonObject();
                UserData.add("mapU::poiSearch", serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);
                int errid = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadMapUMoreTargetData errId:" + errid);
            }
        };

        ThreadPoolUtils.execute(mUploadMapUMoreTargetRunnable);
    }

    public void uploadMapUPersPOISetData() {

        Runnable persPoiSetRunnable = new Runnable() {
            @Override
            public void run() {
                int errId = SRAgent.getInstance().uploadData(Utils.getFromAssets(mContext,
                        "mapU_persPOISet.json"));
                LogUtils.d(TAG, "uploadMapUPersPOISetData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(persPoiSetRunnable);
    }

    public void uploadCmdDefaultData() {

        Runnable cmdRunnable = new Runnable() {
            @Override
            public void run() {
                int errId = SRAgent.getInstance().uploadData(Utils.getFromAssets(mContext,
                        "cmd_default.json"));
                LogUtils.d(TAG, "uploadCmdDefaultData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(cmdRunnable);
    }

    public void uploadHotWrodsData() {

      /*  Runnable cmdRunnable = new Runnable() {
            @Override
            public void run() {
                int errId = SRAgent.getInstance().uploadData(Utils.getFromAssets(mContext,
                        "hot_default.json"));
                LogUtils.d(TAG, "uploadCmdDefaultData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(cmdRunnable);*/

        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_FEEDBACK);
    }

    public void uploadAppStatusData(final boolean isFg, final String service, final String sceneStatus) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                JsonObject dataInfo = new JsonObject();

                JsonObject data = new JsonObject();
                data.add("dataInfo", dataInfo);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", isFg ? "fg" : "bg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", sceneStatus);

                String service_scene = service + "::default";
                JsonObject UserData = new JsonObject();
                UserData.add(service_scene, serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);

                int errId = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadAppStatusData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(mRunnable);
    }

    public void uploadAppStatusNaviBackground() {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                JsonObject dataInfo = new JsonObject();

                JsonObject data = new JsonObject();
                data.add("dataInfo", dataInfo);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", "bg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", "navigation");

                String service_scene = PlatformConstant.Service.MAP_U + "::navi";
                JsonObject UserData = new JsonObject();
                UserData.add(service_scene, serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);

                int errId = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadAppStatusData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(mRunnable);
    }

    public void uploadMediaStatusData(boolean isFg, String service, boolean isPlaying, String song, String artist) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                JsonObject dataInfo = new JsonObject();
                if (!TextUtils.isEmpty(song) && !TextUtils.isEmpty(artist)) {
                    dataInfo.addProperty("song", song);
                    dataInfo.addProperty("artist", artist);
                }

                JsonObject data = new JsonObject();
                data.add("dataInfo", dataInfo);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", isFg ? "fg" : "bg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", isPlaying ? "playing" : "paused");

                String service_scene = service + "::default";
                JsonObject UserData = new JsonObject();
                UserData.add(service_scene, serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);

                int errId = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadMediaStatusData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(mRunnable);
    }

    public void uploadBluePhoneStatusData(final boolean isFg, final String service, final String sceneStatus) {

        Runnable bluePhoneRunnable = new Runnable() {
            @Override
            public void run() {

                JsonObject dataInfo = new JsonObject();

                JsonObject data = new JsonObject();
                data.add("dataInfo", dataInfo);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", isFg ? "fg" : "bg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", sceneStatus);

                String service_scene = service + "::default";
                JsonObject UserData = new JsonObject();
                UserData.add(service_scene, serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);

                int errId = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadBluePhoneStatusData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(bluePhoneRunnable);
    }

    public void uploadTelephoneMoreContactData(final String result, final String semantic) {

        Runnable mUploadTelephoneMoreContactRunnable = new Runnable() {
            @Override
            public void run() {
                JsonObject dataList = new JsonObject();
                JsonArray resultArray = new JsonParser().parse(result).getAsJsonArray();
                dataList.add("result", resultArray);

                JsonObject semanticObject = new JsonParser().parse(semantic).getAsJsonObject();
                dataList.add("semantic", semanticObject);

                JsonObject data = new JsonObject();
                data.add("dataList", dataList);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", "fg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", "moreContact");

                JsonObject UserData = new JsonObject();
                UserData.add("telephone::default", serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);
                int errid = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadTelephoneMoreContactData errId:" + errid);
            }
        };

        ThreadPoolUtils.execute(mUploadTelephoneMoreContactRunnable);
    }

    public void uploadChangbaStatusData(boolean isFg) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                JsonObject dataInfo = new JsonObject();

                JsonObject data = new JsonObject();
                data.add("dataInfo", dataInfo);

                JsonObject serviceScene = new JsonObject();
                serviceScene.addProperty("activeStatus", isFg ? "fg" : "bg");
                serviceScene.add("data", data);
                serviceScene.addProperty("sceneStatus", "default");

                String service_scene = "changBa" + "::guide";
                JsonObject UserData = new JsonObject();
                UserData.add(service_scene, serviceScene);

                JsonObject root = new JsonObject();
                root.add("UserData", UserData);

                LogUtils.d(TAG, "root:\n" + root);

                int errId = SRAgent.getInstance().uploadData(root.toString());
                LogUtils.d(TAG, "uploadMediaStatusData errId:" + errId);
            }
        };
        ThreadPoolUtils.execute(mRunnable);
    }
}
