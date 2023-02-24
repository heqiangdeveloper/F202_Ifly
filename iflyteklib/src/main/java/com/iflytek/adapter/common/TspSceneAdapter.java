package com.iflytek.adapter.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.iflytek.mvw.MvwSession;

public class TspSceneAdapter {
    public static final int TSP_SCENE_GLOBAL = 1;
    public static final int TSP_SCENE_CONFIRM = 2;
    public static final int TSP_SCENE_SELECT = 3;
    public static final int TSP_SCENE_ANSWER_CALL = 4;
    public static final int TSP_SCENE_NAVI = 5;
    public static final int TSP_SCENE_DIALOG = 6;
    public static final int TSP_SCENE_FEEDBACK = 7;//人脸识别二次交互
    public static final int TSP_SCENE_DRIVING_GUIDE = 8;//驾驶模式引导二次交互
    public static final int TSP_SCENE_WARN_SAVE_SLEEP = 9;//驾驶模式引导二次交互
    public static final int TSP_SCENE_CHANGBA = 10;//唱吧
    public static final int TSP_SCENE_VIDEO = 11;//电影
    public static final int TSP_SCENE_DVR = 12;//影像类
    public static final int TSP_SCENE_VEHICLE = 13;//车况
    public static final int TSP_SCENE_OIL = 14;//油量 电量提醒
    public static final int TSP_SCENE_DRIVING_CARE = 15;//驾车关怀
    public static final int TSP_SCENE_CALL = 16;//蓝牙电话接听 挂断

    /**
     * 保存场景到系统
     *
     * @param context
     * @param tspScene
     */
    public static void saveTspScene(Context context, int tspScene) {
        SharedPreferences preferences = context.getSharedPreferences("com.chinatsp.ifly_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putInt("tsp_scene", tspScene);
        editor.apply();
    }

    /**
     * 获取当前系统的场景
     *
     * @param context
     * @return
     */
    public static int getTspScene(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("com.chinatsp.ifly_preferences",
                Context.MODE_PRIVATE);
        return preferences.getInt("tsp_scene", TSP_SCENE_GLOBAL);
    }

    //在非GLOBAL场景下默认是与GLOBAL场景的和
    public static int convert2MvwScene(int tspScene) {
        if (tspScene == TspSceneAdapter.TSP_SCENE_GLOBAL) {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER;
        } else if (tspScene == TspSceneAdapter.TSP_SCENE_CONFIRM) {
            return MvwSession.ISS_MVW_SCENE_CONFIRM;
        } else if (tspScene == TspSceneAdapter.TSP_SCENE_SELECT) {
            return MvwSession.ISS_MVW_SCENE_SELECT | MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CONFIRM | MvwSession.ISS_MVW_SCENE_CUSTOME| MvwSession.ISS_MVW_SCENE_OTHER;
        } else if (tspScene == TspSceneAdapter.TSP_SCENE_ANSWER_CALL) {
            return MvwSession.ISS_MVW_SCENE_ANSWER_CALL | MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME| MvwSession.ISS_MVW_SCENE_OTHER;
        } else if(tspScene == TspSceneAdapter.TSP_SCENE_NAVI) { //导航场景复用来电接收场
            return MvwSession.ISS_MVW_SCENE_ANSWER_CALL | MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME| MvwSession.ISS_MVW_SCENE_OTHER;
        } else if(tspScene == TspSceneAdapter.TSP_SCENE_DIALOG) { //导航退出场景
            return  MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER|MvwSession.ISS_MVW_SCENE_CONFIRM;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_FEEDBACK
                ||tspScene == TspSceneAdapter.TSP_SCENE_VEHICLE
                ||tspScene == TspSceneAdapter.TSP_SCENE_OIL
                ||tspScene == TspSceneAdapter.TSP_SCENE_DRIVING_CARE) {
            return MvwSession.ISS_MVW_SCENE_CONFIRM | MvwSession.ISS_MVW_SCENE_GLOBAL;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_DRIVING_GUIDE) {
            return MvwSession.ISS_MVW_SCENE_CONFIRM | MvwSession.ISS_MVW_SCENE_GLOBAL;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP) {
            return MvwSession.ISS_MVW_SCENE_CONFIRM | MvwSession.ISS_MVW_SCENE_GLOBAL;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_CHANGBA) {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER|MvwSession.ISS_MVW_SCENE_KTV;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_VIDEO) {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER|MvwSession.ISS_MVW_SCENE_CCTV;
        }else if (tspScene == TspSceneAdapter.TSP_SCENE_DVR) {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER|MvwSession.ISS_MVW_SCENE_IMAGE;
        } else if (tspScene == TspSceneAdapter.TSP_SCENE_CALL) {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_CALL;
        } else {
            return MvwSession.ISS_MVW_SCENE_GLOBAL | MvwSession.ISS_MVW_SCENE_CUSTOME | MvwSession.ISS_MVW_SCENE_OTHER;
        }
    }
}
