package com.chinatsp.ifly.voice.platformadapter.entity;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.base.BaseApplication;

public class MvwLParamEntity {
    public int nMvwScene ;
    public int nMvwId ;
    public int nMvwScore ;
    public int wakeup ;
    public long nStartBytes ;
    public long nEndBytes ;
    public String nKeyword ;
    public double PowerValue ;

    public CmdVoiceModel convert2CmdVoiceModel() {
        CmdVoiceModel cmdVoiceModel = new CmdVoiceModel();
        cmdVoiceModel.id = nMvwId;
        cmdVoiceModel.text = nKeyword;
        cmdVoiceModel.response= DatastatManager.response;
        cmdVoiceModel.hide= FloatViewManager.getInstance(BaseApplication.getInstance().getApplicationContext()).isHide()?0:1;
        return cmdVoiceModel;
    }
}
