package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IChatController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;

import java.lang.ref.WeakReference;

public class ChatController extends BaseController implements IChatController {
    private static final String TAG = "ChatController";
    private static final int MSG_TTS = 1000;
    private Context context;
    private MyHandler myHandler = new MyHandler(this);

    public ChatController(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        Message msg = myHandler.obtainMessage(MSG_TTS, intentEntity.answer.text);
        myHandler.sendMessageDelayed(msg, 1000);

        Utils.eventTrack(context, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default);
    }

    @Override
    public void mvwAction(MvwLParamEntity lParamEntity) {

    }

    public void srAction(String text) {
        Message msg = myHandler.obtainMessage(MSG_TTS, text);
        myHandler.sendMessageDelayed(msg, 10);
    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    private static class MyHandler extends Handler {

        private final WeakReference<ChatController> chatControllerWeakReference;

        private MyHandler(ChatController chatController) {
            this.chatControllerWeakReference = new WeakReference<>(chatController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final ChatController chatController = chatControllerWeakReference.get();
            if (chatController == null) {
                LogUtils.d(TAG, "chatController == null");
                return;
            }
            switch (msg.what) {
                case MSG_TTS:
                    chatController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {

                        @Override
                        public void onPlayStopped() {
                            Utils.exitVoiceAssistant();
                        }
                    });
                    break;
            }
        }
    }
}
