package com.chinatsp.ifly;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
interface ISpeechControlListener {

  void onSrAction(in NlpVoiceModel nlpVoiceModel);
  void onMvwAction(in CmdVoiceModel cmdVoiceModel);
  void onStksAction(in CmdVoiceModel cmdVoiceModel);
  void onMutualAction(in MutualVoiceModel mutualVoiceModel);
  void onSearchWeChatContact(in String keyword);

}
