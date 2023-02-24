//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.ifly.voiceadapter;
import android.os.RemoteException;
import com.chinatsp.ifly.ISpeechControlListener;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
public class SpeechAgentListener extends ISpeechControlListener.Stub {
    private ISpeechClientListener client;

    public SpeechAgentListener(ISpeechClientListener client) {
        this.client = client;
    }

    @Override
    public void onSrAction(NlpVoiceModel nlpVoiceModel) throws RemoteException {
        this.client.onSrAction(nlpVoiceModel);
    }

    @Override
    public void onMvwAction(CmdVoiceModel cmdVoiceModel) throws RemoteException {
        this.client.onMvwAction(cmdVoiceModel);
    }

    @Override
    public void onStksAction(CmdVoiceModel cmdVoiceModel) throws RemoteException {
        this.client.onStksAction(cmdVoiceModel);
    }

    @Override
    public void onSearchWeChatContact(String name) throws RemoteException {
        this.client.onSearchWeChatContact(name);
    }
    @Override
    public void onMutualAction(MutualVoiceModel mutualVoiceModel)throws RemoteException {
        this.client.onMutualAction(mutualVoiceModel);
    }

}
