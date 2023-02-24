package com.chinatsp.ifly;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.chinatsp.ifly.activeservice.AudioSynthesis;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.commonService.okHttpRequestService;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.service.FloatViewIdleService;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.voiceadapter.ISpeechClientListener;
import com.chinatsp.ifly.voiceadapter.SpeechServiceAgent;
import com.chinatsp.proxy.IVehicleNetworkCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.listener.IExtendCallback;
import org.json.JSONObject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

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

    private Unbinder unbinder;
    private Context context;
    private final static String TAG;
    ActiveServiceViewManager activeServiceViewManager;


    static {
        TAG = "MainActivity";
    }

    AudioSynthesis audioSynthesis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        unbinder = ButterKnife.bind(this);
        requestPermission();
        TAExtendManager.getInstance().init(this);
         audioSynthesis = new AudioSynthesis(context);
        //FOR 测试
//        SpeechServiceAgent speechServiceAgent = SpeechServiceAgent.getInstance();
//        speechServiceAgent.initService(context, Business.MUSIC, mSpeechClientListener);
//        try {
//            speechServiceAgent.registerStksCommand("");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            speechServiceAgent.uploadAppStatus("");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

    }


    private ISpeechClientListener mSpeechClientListener = new ISpeechClientListener() {
        @Override
        public void onSrAction(NlpVoiceModel nlpVoiceModel) {

        }

        @Override
        public void onMvwAction(CmdVoiceModel cmdVoiceModel) {

        }

        @Override
        public void onStksAction(CmdVoiceModel cmdVoiceModel) {

        }

        @Override
        public void onSearchWeChatContact(String name) {

        }

        @Override
        public void onMutualAction(MutualVoiceModel mutualVoiceModel) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onStop() {
        super.onStop();
        SpeechServiceAgent.getInstance().releaseService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_1, R.id.tv_2, R.id.tv_3, R.id.tv_4, R.id.tv_5, R.id.tv_6, R.id.tv_7, R.id.tv_8, R.id.tv_9,R.id.tv_10})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_1: //天气
//                Intent intent = new Intent(context, FullScreenActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_WEATHER);
//                intent.putExtra(FullScreenActivity.DATA_LIST_STR, "1111111");
//                intent.putExtra(FullScreenActivity.SEMANTIC_STR, "1");
//                context.startActivity(intent);

                break;
            case R.id.tv_2:
                Intent intent2 = new Intent(context, FullScreenActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_STOCK);
                context.startActivity(intent2);
                break;
            case R.id.tv_3:
                Intent intent3 = new Intent(context, FullScreenActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent3.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE);
                context.startActivity(intent3);
                break;
            case R.id.tv_4:
                Intent intent4 = new Intent(context, FullScreenActivity.class);
                intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent4.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_TRAIN);
                context.startActivity(intent4);
                break;
            case R.id.tv_5:
                Intent intent5 = new Intent(context, FullScreenActivity.class);
                intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent5.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT);
                context.startActivity(intent5);
                break;
            case R.id.tv_6:
                Intent intent6 = new Intent(context, FullScreenActivity.class);
                intent6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent6.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI);
                context.startActivity(intent6);
                break;
            case R.id.tv_7:
                Intent intent7 = new Intent(context, SettingsActivity.class);
                intent7.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent7);
                break;
            case R.id.tv_8:
                Intent intent8 = new Intent();
                intent8.setAction(Intent.ACTION_MAIN);
                intent8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent8.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(intent8);

                TAExtendManager.getInstance().getApi().setNaviScreen(new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        Log.d("test", "success");
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        Log.d("test", "onFail:" + extendErrorModel.getErrorCode());
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });

                break;
            case R.id.tv_9:

                break;

            case R.id.tv_10:

            break;
        }
    }

    /*
     * 获取TTS播报数据
     */
    private void getTtsMessage() {
        Log.d(TAG, "lh:start get Token:" );
        VehicleNetworkManager.getInstance().getToken(new IVehicleNetworkCallback() {
            @Override
            public void onCompleted(String s) {
                Log.d(TAG, "lh:get token-" + s);
                String projectId = SharedPreferencesUtils.getString(context, AppConstant.KEY_TTS_PROJECT_ID, "F202_01");
                String projectVersion = SharedPreferencesUtils.getString(context, AppConstant.KEY_TTS_PROJECT_VERSION, "");
                okHttpRequestService.getInstance().getHuTtsData(context, s, projectId, projectVersion);
            }

            @Override
            public void onException(int i, String s) {
                Log.d(TAG, "lh:exception:" + s);
            }
        });
    }

}
