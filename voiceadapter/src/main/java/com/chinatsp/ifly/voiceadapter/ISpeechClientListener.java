//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.ifly.voiceadapter;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
public interface ISpeechClientListener {
    void onSrAction(NlpVoiceModel nlpVoiceModel);

    void onMvwAction(CmdVoiceModel cmdVoiceModel);

    void onStksAction(CmdVoiceModel cmdVoiceModel);

    void onSearchWeChatContact(String name);

    void onMutualAction(MutualVoiceModel mutualVoiceModel);
}
