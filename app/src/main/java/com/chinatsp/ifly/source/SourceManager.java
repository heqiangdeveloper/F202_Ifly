package com.chinatsp.ifly.source;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MyToast;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.bean.CallContact;
import com.example.mediasdk.MediaManager;
import com.example.mediasdk.OperateBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SourceManager {

    private static final String TAG = SourceManager.class.getSimpleName();
    public static final int PLAY_STATE_PAUSE = 0;
    public static final int PLAY_STATE_PLAYING = 1;
    private static SourceManager instance;
    private Context mContext;
    private static boolean isBTUseable;
    private static boolean isUSBUseable;
    private static boolean isHDDUseable;
    private SourceContract.SourceID mCurrentSource = SourceContract.SourceID.NULL;

    private List<SourceContract.SourceID> mDatas = new ArrayList<>();

    public static SourceManager  getInstance(Context c){
        if(instance==null){
            synchronized (SourceManager.class){
                if(instance==null)
                    instance = new SourceManager(c);
            }
        }
        return instance;
    }

    public void notifySrcChanged(){

        if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
            LogUtils.d(TAG, "Call is Active, ignore");
            return;
        }


        if(!CarUtils.getInstance(mContext).isVoiceBroadcastOpen()){
            Log.d(TAG, "notifySrcChanged() called::"+CarUtils.getInstance(mContext).isVoiceBroadcastOpen());
            return;
        }
        if(!mContext.getPackageName().equals(AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg())) {
            AudioFocusUtils.getInstance(mContext).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
        }
        isHDDUseable = hasHDDMedia();
        String source = "在线音乐";
        switch (mCurrentSource){
            case NULL:
                mCurrentSource = SourceContract.SourceID.OL_MUSIC;
                source = "在线音乐";
                break;
            case OL_MUSIC:
                if(isUSBUseable){
                    mCurrentSource = SourceContract.SourceID.USB;
                    source = "USB音乐";
                } else if(isHDDUseable){
                    mCurrentSource = SourceContract.SourceID.HDD;
                    source = "本机音乐";
                } else if(isBTUseable){
                    mCurrentSource = SourceContract.SourceID.BTAUDIO;
                    source = "蓝牙音乐";
                }else {
                    mCurrentSource = SourceContract.SourceID.TUNER;
                    source = "广播电台";
                }

                break;
            case USB:
                if(isHDDUseable){
                    mCurrentSource = SourceContract.SourceID.HDD;
                    source = "本机音乐";
                } else if(isBTUseable){
                    mCurrentSource = SourceContract.SourceID.BTAUDIO;
                    source = "蓝牙音乐";
                }else {
                    mCurrentSource = SourceContract.SourceID.TUNER;
                    source = "广播电台";
                }
                break;
            case HDD:
                if(isBTUseable){
                    mCurrentSource = SourceContract.SourceID.BTAUDIO;
                    source = "蓝牙音乐";
                }else {
                    mCurrentSource = SourceContract.SourceID.TUNER;
                    source = "广播电台";
                }
                break;
            case BTAUDIO:
                mCurrentSource = SourceContract.SourceID.TUNER;
                source = "广播电台";
                break;
            case TUNER:
                mCurrentSource = SourceContract.SourceID.OL_TUNER;
                source = "欧尚电台";
                break;
            case OL_TUNER:
                mCurrentSource = SourceContract.SourceID.OL_MUSIC;
                source = "在线音乐";
                break;
        }
        Log.d(TAG, "notifySrcChanged() called:::"+mCurrentSource.name());
        tts(source);
    }


    private void tts(String source){
        String defTts = mContext.getString(R.string.btnC1);
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC1CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                if(tts==null||"".equals(tts))
                    tts = defTts;
                tts = tts.replace("#SOURCE#",source);
                Utils.startTTS(tts, AppConstant.KeyGuidePriority, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }
        });
    }




    private SourceManager(Context context) {

        mContext = context;
        initBlueTooth();
        registerUsbReceiver();
        registBlueToothStateReceiver();
        getSource();
        registerMediaStatus();
    }


    private void registerMediaStatus(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.STATUS_BAR_PLAY_INFO_ACTION);
        mContext.registerReceiver(receiver, intentFilter);
    }


    private void getSource() {

        mDatas.clear();
        mDatas.add(SourceContract.SourceID.OL_MUSIC);

        if (isUSBUseable) {
            mDatas.add(SourceContract.SourceID.USB);
        }
        if (isHDDUseable) {
            mDatas.add(SourceContract.SourceID.HDD);
        }
        if (isBTUseable) {
            mDatas.add(SourceContract.SourceID.BTAUDIO);
        }
        mDatas.add(SourceContract.SourceID.TUNER);
        mDatas.add(SourceContract.SourceID.OL_TUNER);

    }

    private boolean hasHDDMedia() {
        boolean has = false;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        JSONObject jo = new JSONObject();
        try {
            jo.put("path", path);
            String result = MediaManager.getInstance().opCustom(new OperateBean(10012, jo));
            Log.i(TAG, "hasHDDMedia: result 1有 0无: " + result);
            has = "1".equals(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return has;
    }





    private void initBlueTooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if(isEnabled){
            int status = bluetoothAdapter.getProfileConnectionState(16);
            Log.d(TAG, "initBlueTooth status:"+status);
            isBTUseable = BluetoothProfile.STATE_CONNECTED == status;
        }
    }

    private void registBlueToothStateReceiver(){
        IntentFilter fileFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        fileFilter.addAction(ACTION_HFP_STATUS_CHANEG);
        mContext.registerReceiver(blueToothReceiver, fileFilter);
    }

    private BluetoothDevice mHeadsetDevice;
    private static final String ACTION_HFP_STATUS_CHANEG = "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED";
    BroadcastReceiver blueToothReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "blueToothReceiver --> " + action);
            BluetoothDevice device = null;
            int state = 0;
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                state = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            } else if(action.equals(ACTION_HFP_STATUS_CHANEG)){
                state = intent.getExtras().getInt(BluetoothProfile.EXTRA_STATE);
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);/*Modify by zhanghao for [BUG#10155] of 3Y1_dayun  Date: 20190325*/

            }
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "bluetoothstate:"+"STATE_ON");
                    setBluetoothConnectedStatus(false);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "bluetoothstate:"+"STATE_OFF");
                    setBluetoothConnectedStatus(false);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "bluetoothstate:"+"STATE_CONNECTED");
                    mHeadsetDevice = device;
                    setBluetoothConnectedStatus(true);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "bluetoothstate:"+"STATE_DISCONNECTED");
                    /*Modify by zhanghao for [BUG#10155] of 3Y1_dayun  Date: 20190325 START*/
                    if(isProfileDevice(device)){
                        mHeadsetDevice = null;
                        setBluetoothConnectedStatus(false);
                    }
                    /*Modify by zhanghao for [BUG#10155] of 3Y1_dayun  Date: 20190325 END*/
                    break;
                default:
                    break;
            }
        }
    };


    private void setBluetoothConnectedStatus(boolean useable) {
        isBTUseable = useable;
        getSource();
    }

    /*Modify by zhanghao for [BUG#10155] of 3Y1_dayun  Date: 20190325 START*/
    private boolean isProfileDevice(BluetoothDevice device) {
        String receiveDeviceMac = null;
        String saveDeviceMac = null;
        if (device != null) {
            receiveDeviceMac = device.getAddress();
        }
        if (mHeadsetDevice != null) {
            saveDeviceMac = mHeadsetDevice.getAddress();
        }
        Log.w(TAG, "isProfileDevice: mHeadsetDevice = " + mHeadsetDevice + ", device = " + device);
        return mHeadsetDevice == null || device == null || equals(saveDeviceMac, receiveDeviceMac);
    }

    /**
     * 描述: 判断两个对象相等
     *
     * @return boolean
     * @throws throws [违例类型] [违例说明]
     */
    private static <A, B> boolean equals(A a, B b) {
        return (a == b) || (null != a && a.equals(b));
    }

    private void registerUsbReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        mContext.registerReceiver(mUsbReceiver, filter);
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "mUsbReceiver --> " + action);
            String path = intent.getData().getPath();
            String realPath = getRealPath(intent);
            String exraPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.i(TAG, "path --> " + path + "  realPath " + realPath + "  exraPath --> " + exraPath);

            if (exraPath.startsWith(realPath)) {
//				Toast.makeText(mContext, "exraPath.startsWith(realPath)", Toast.LENGTH_LONG).show();
                Log.i(TAG, "exraPath.startsWith(realPath) return ");
                return;
            }
            switch (action) {
                case Intent.ACTION_MEDIA_MOUNTED:
                    isUSBUseable = true;
                    getSource();
                    break;
                case Intent.ACTION_MEDIA_EJECT:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    isUSBUseable = false;
                    getSource();
                    break;
                default:
                    break;
            }
        }
    };

    private String getRealPath(Intent intent) {
        String result = null;
        Uri usbData = intent.getData();
        if (usbData != null) {
            String realPath = usbData.getSchemeSpecificPart();
            if (!TextUtils.isEmpty(realPath)) {
                result = realPath.replace("///", "/");
            }
        }
        return result;
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String source = intent.getStringExtra("source");
            int playStatus = intent.getIntExtra("play_state",PLAY_STATE_PAUSE);
            Log.d(TAG, "onReceive() called with: source = [" + source + "], playStatus = [" + playStatus + "]");
            //USB,USB音乐 TUNER,广播电台 HDD,本机音乐 BTAUDIO,蓝牙音乐 OTHERS, KAOLA,欧尚电台 OL_MUSIC在线音乐
            if("USB".equals(source)){
                mCurrentSource = SourceContract.SourceID.USB;
            }else if("TUNER".equals(source)){
                mCurrentSource = SourceContract.SourceID.TUNER;
            }else if("HDD".equals(source)){
                mCurrentSource = SourceContract.SourceID.HDD;
            }else if("BTAUDIO".equals(source)){
                mCurrentSource = SourceContract.SourceID.BTAUDIO;
            }else if("KAOLA".equals(source)){
                mCurrentSource = SourceContract.SourceID.OL_TUNER;
            }else if("OL_MUSIC".equals(source)){
                mCurrentSource = SourceContract.SourceID.OL_MUSIC;
            }
        }
    };


    /**
     * 自定义方控切源语义
     * @return
     */
    public NlpVoiceModel changeSourceVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "chinatsp";
        nlpVoiceModel.operation = "INSTRUCTION";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"SOURCE\"}}";
        return nlpVoiceModel;
    }

}
