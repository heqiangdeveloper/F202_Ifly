package com.chinatsp.ifly;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.service.FloatViewIdleService;
import com.chinatsp.ifly.source.Constant;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.example.mxextend.ExtendConstants;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.entity.SearchResultModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.mxnavi.busines.entity.PageOpreaData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NaviActivity extends AppCompatActivity {

    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.tv_3)
    TextView tv3;
    @BindView(R.id.tv_4)
    TextView tv4;
    @BindView(R.id.tv_5)
    TextView tv5;
    @BindView(R.id.tv_6)
    TextView tv6;
    @BindView(R.id.tv_7)
    TextView tv7;
    @BindView(R.id.tv_8)
    TextView tv8;
    @BindView(R.id.tv_9)
    TextView tv9;
    @BindView(R.id.tv_10)
    TextView tv10;
    @BindView(R.id.tv_11)
    TextView tv11;
    @BindView(R.id.tv_12)
    TextView tv12;
    @BindView(R.id.tv_13)
    TextView tv13;
    @BindView(R.id.tv_14)
    TextView tv14;
    @BindView(R.id.tv_15)
    TextView tv15;
    @BindView(R.id.tv_16)
    TextView tv16;
    @BindView(R.id.tv_17)
    TextView tv17;
    @BindView(R.id.tv_18)
    TextView tv18;
    private Unbinder unbinder;
    private Context context;
    private IExtendApi extendApi;
    private TTSController ttsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        context = this;
        unbinder = ButterKnife.bind(this);
        requestPermission();

        ttsController = TTSController.getInstance(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        TAExtendManager.getInstance().init(this);
        if (!ActivityManagerUtils.getInstance(this).isServiceRunning("com.chinatsp.ifly.service.FloatViewIdleService")) {
            startService(new Intent(this, FloatViewIdleService.class));
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, 1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i : grantResults) {
            if (i != PackageManager.PERMISSION_GRANTED) {
//                System.exit(1);
            }
        }
    }

    private String calcRemainTimeAndDistance(int remainTime, int remainDistance) {
        String formatTimeS = formatTimeS(remainTime);
        if (remainDistance < 1000) {
            return String.format("距离目的地还有%d%s", remainDistance, "米,") + formatTimeS;
        } else {
            return String.format("距离目的地还有%.1f%s", remainDistance / 1000.0f, "公里,") + formatTimeS;
        }

    }

    private static String formatTimeS(long seconds) {
        Calendar calendar = Calendar.getInstance();
        LogUtils.d("test", DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar.getTime()).toString());
        int oriDay = calendar.get(Calendar.DAY_OF_MONTH);
        long millis = calendar.getTimeInMillis() + seconds * 1000;
        calendar.setTimeInMillis(millis);
        LogUtils.d("test", DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar.getTime()).toString());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        StringBuffer sb = new StringBuffer();
        if (day - oriDay == 1) {
            sb.append("预计明天").append(hour).append("点").append(min).append("分到达");
        } else if (day - oriDay == 2) {
            sb.append("预计后天").append(hour).append("点").append(min).append("分到达");
        } else if (day == oriDay) {
            sb.append("预计").append(hour).append("点").append(min).append("分到达");
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_3, R.id.tv_4, R.id.tv_5, R.id.tv_6, R.id.tv_7, R.id.tv_8, R.id.tv_9, R.id.tv_10, R.id.tv_11, R.id.tv_12, R.id.tv_13, R.id.tv_14, R.id.tv_15, R.id.tv_16, R.id.tv_17, R.id.tv_18})
    public void onViewClicked(View view) {
        if (extendApi == null) {
            extendApi = TAExtendManager.getInstance().getApi();
        }
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        switch (view.getId()) {
            case R.id.tv_1: //地图缩放
                extendApi.specialPoiNavi(0, 1, new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        LogUtils.d("test", "success:" + extendBaseModel.getExtendId());
                        extendApi.getDestInfo(new IExtendCallback<LocationInfo>() {
                            @Override
                            public void success(LocationInfo locationInfo) {
                                LogUtils.d("test", "success:" + locationInfo.getName());
                            }

                            @Override
                            public void onFail(ExtendErrorModel extendErrorModel) {

                            }

                            @Override
                            public void onJSONResult(JSONObject jsonObject) {

                            }
                        });
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
                break;
            case R.id.tv_2: //路况开关
                extendApi.mapOpera(0, 0, new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        LogUtils.d("test", "success:" + extendBaseModel.getExtendId());
                        ttsController.startTTS("开启路况成功:" + extendBaseModel.getExtendId());
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorMessage());
                        ttsController.startTTS("开启路况失败:" + extendErrorModel.getErrorMessage());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {
                        LogUtils.d("test", "onJSONResult: " + jsonObject);
                    }
                });
                int remainTime = extendApi.getRemainTime();
                LogUtils.e("test", "remainTime：" + remainTime);
                int remainDistance = extendApi.getRemainDistance();
                LogUtils.e("test", "remainDistance：" + remainDistance);
                if (remainTime > 0 && remainDistance > 0) {
                    String tts = calcRemainTimeAndDistance(remainTime, remainDistance);
                    LogUtils.e("test", "tts：" + tts);
                }

                double dis = 11011.0 / 1000.0f;
                String s = String.format("%.2f", dis);
                LogUtils.d("test", "s:" + s);
                break;
            case R.id.tv_3://沿途搜索
                extendApi.searchAlongRoute("加油站", new IExtendCallback<SearchResultModel>() {
                    @Override
                    public void success(SearchResultModel searchResultModel) {
                        ArrayList<LocationInfo> resultList = searchResultModel.getResultList();
                        LogUtils.d("test", "result:" + searchResultModel.getResultList());
                        for(LocationInfo info : resultList) {
                            LogUtils.d("test", "info:" + info);
                        }
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
                break;
            case R.id.tv_4://停止导航
                extendApi.cancelNavi();
                break;
            case R.id.tv_5: //当前位置查询
                extendApi.showMyLocation(0, new IExtendCallback<LocationInfo>() {
                    @Override
                    public void success(LocationInfo locationInfo) {
                        LogUtils.d("test", "success :" + locationInfo);
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "fail:" + extendErrorModel.getErrorMessage());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {
                        LogUtils.d("test", "onJSONResult:" + jsonObject);
                    }
                });
                break;
            case R.id.tv_6: //周边搜索
                 TAExtendManager.getInstance().getApi().aroundSearch("","医院",0, 10000, new IExtendCallback<SearchResultModel>() {
                    @Override
                    public void success(SearchResultModel searchResultModel) {
                        ArrayList<LocationInfo> resultList = searchResultModel.getResultList();
                        for(LocationInfo info : resultList) {
                            LogUtils.d("test", "re:" + info);
                        }
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                        Toast.makeText(getApplicationContext(), "onFail:" + extendErrorModel.getErrorCode()
                                + " erroMsg:" + extendErrorModel.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
                break;
            case R.id.tv_7: //关键字搜索
                int callbackid = TAExtendManager.getInstance().getApi().keywordSearch("世界之窗", new IExtendCallback<SearchResultModel>() {
                    @Override
                    public void success(SearchResultModel searchResultModel) {
                        ArrayList<LocationInfo> resultList = searchResultModel.getResultList();
                        for(LocationInfo info : resultList) {
                            LogUtils.d("test", "re:" + info);
                        }
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                        Toast.makeText(getApplicationContext(), "onFail:" + extendErrorModel.getErrorCode()
                                + " erroMsg:" + extendErrorModel.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });

//                extendApi.setVolumeMute(false, new IExtendCallback() {
//                    @Override
//                    public void success(ExtendBaseModel extendBaseModel) {
//                        LogUtils.d("test", "success");
//                    }
//
//                    @Override
//                    public void onFail(ExtendErrorModel extendErrorModel) {
//                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
//                    }
//
//                    @Override
//                    public void onJSONResult(JSONObject jsonObject) {
//
//                    }
//                });
                break;
            case R.id.tv_8:
                startApp(AppConstant.PACKAGE_NAME_WECARNAVI);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TAExtendManager.getInstance().getApi().naviToPoi("世界之窗(公交站)", new IExtendCallback() {
                            @Override
                            public void success(ExtendBaseModel extendBaseModel) {
                                Toast.makeText(getApplicationContext(), "导航到poi：success", Toast.LENGTH_SHORT).show();
                                LogUtils.d("test", "success");
                            }

                            @Override
                            public void onFail(ExtendErrorModel errorModel) {
                                Toast.makeText(getApplicationContext(), "导航到poi：error==" + errorModel.getErrorCode(), Toast.LENGTH_SHORT).show();
                                LogUtils.d("test", "onFail:" + errorModel.getErrorMessage() + " errocode:" + errorModel.getErrorCode());
                            }

                            @Override
                            public void onJSONResult(JSONObject jsonObject) {

                            }
                        });
                    }
                }, 5000);

                break;
            case R.id.tv_9:
                Intent intent8 = new Intent();
                intent8.setAction(Intent.ACTION_MAIN);
                intent8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent8.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(intent8);

                //模拟启动悬浮框
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendBroadcast(new Intent(AppConstant.ACTION_AWARE_VOICE_TOUCH));
                    }
                }, 2000);
                break;
            case R.id.tv_10:
                extendApi.goSetting(new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        LogUtils.d("test", "success");
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
                break;
            case R.id.tv_11:
                PageOpreaData data = new PageOpreaData();
                data.setPageId(ExtendConstants.PageId.PAGE_SETTING);
                data.setAction(0);
                data.setRequestData("");
                extendApi.pageOprea(data, new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        LogUtils.d("test", "success");
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
                break;

        }
    }

    public void startApp(String packageName) {
        String className = Utils.getLauncherActivityNameByPackageName(BaseApplication.getInstance(), packageName);
        if (!TextUtils.isEmpty(className)) {
            Intent intent = new Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseApplication.getInstance().startActivity(intent);
        }
    }
}
