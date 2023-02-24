package com.chinatsp.ifly.voice.platformadapter.entity;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.utils.LogUtils;

import org.json.JSONObject;

import java.util.List;

public class StkResultEntity {

    /**
     * bisMvwSTKS : 1
     * selectList : {"list":[{"dimension":[{"field":"map","spword":false,"val":"收藏"}],"id":12,"searchResult":[{"field":"text","score":1,"val":"收藏"}]}],"type":"poi"}
     * text : 收藏
     */

    public String bisMvwSTKS;
    public SelectListBean selectList;
    public String text;

    public static class SelectListBean {
        /**
         * list : [{"dimension":[{"field":"map","spword":false,"val":"收藏"}],"id":12,"searchResult":[{"field":"text","score":1,"val":"收藏"}]}]
         * type : poi
         */
        public String type;
        public List<ListBean> list;

        public static class ListBean {
            /**
             * dimension : [{"field":"map","spword":false,"val":"收藏"}]
             * id : 12
             * searchResult : [{"field":"text","score":1,"val":"收藏"}]
             */
            public int id;
            public List<JSONObject> dimension;
            public List<JSONObject> searchResult;

        }
    }


    public CmdVoiceModel convert2CmdVoiceModel() {
        CmdVoiceModel cmdVoiceModel = new CmdVoiceModel();
        if (selectList != null && selectList.list != null && selectList.list.size() > 0) {
            if(selectList.list.size() ==1){
                cmdVoiceModel.id = selectList.list.get(0).id;
                cmdVoiceModel.text = text;
            } else { // 可见即可说 广播会返回两个值，导致电台无法处理，做下规避
                if(text!=null&&text.equals("广播")){
                    cmdVoiceModel.id = 243;
                    cmdVoiceModel.text = text;
                    cmdVoiceModel.response = DatastatManager.response;
                }else if(text!=null&&text.equals("广播电台")){
                    cmdVoiceModel.id = 32;
                    cmdVoiceModel.text = text;
                    cmdVoiceModel.response = DatastatManager.response;
                }else{
                    cmdVoiceModel.id = selectList.list.get(0).id;
                    cmdVoiceModel.text = text;
                    cmdVoiceModel.response = DatastatManager.response;
                }
            }

        } else {
            LogUtils.e("CmdVoiceModel", text + "no return id");
            cmdVoiceModel.id = -1;
            cmdVoiceModel.text = text;
            cmdVoiceModel.response = DatastatManager.response;
        }
        return cmdVoiceModel;
    }
}
