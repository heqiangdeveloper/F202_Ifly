package com.chinatsp.ifly.voice.platformadapter.entity;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;

import org.json.JSONObject;

import java.util.List;

/**
 * AIUI语义结果intent实体类
 */
public class IntentEntity {
    public int rc;
    //对结果内容的最简化文本/图片描述，各服务自定义。
    public AnswerEntity answer;
    public String aqua_score;
    public int array_index;
    public String cid;
    public String bislocalresult;
    public DataEntity data;//语义结构化表示，各服务自定义。
    //继承后语义，在用户原始请求语义基础上继承一些历史语义的信息
    public JSONObject demand_semantic;
    public String dialog_stat;
    public double engine_time;
    //在存在多个候选结果时，用于提供更多的结果描述。
    public List<JSONObject> moreResults;
    //服务的细分操作编码，各业务服务自定义。需要根据 operation 来判断服务的返回类型。
    public String operation;
    //用户原始语义，表达用户原始请求的意图
    public JSONObject intelligentAnswer;
    public JSONObject orig_semantic;
    public boolean save_history;
    public float score;
    public JSONObject searchSemantic;
    //搜索后语义，部分标识信源搜索结果状态的字段放在其中
    public JSONObject search_semantic;
    //语义结构化表示，各服务自定义。
    public Semantic semantic;
    //业务名，标识用户意图。
    public String service;
    //唯一的识别会话
    public String sid;
    //当次会话请求完成后的跳转状态
    public JSONObject state;
    //用户的输入，可能和请求中的原始text不完全一致，因服务器可能会对text进行语言纠错。
    public String text;
    //当次会话请求完成后的跳转状态，内部使用
    public JSONObject used_state;
    //唯一的通信id
    public String uuid;

    public void setService(String service) {
        this.service = service;
    }

    public NlpVoiceModel convert2NlpVoiceModel() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = service;
        nlpVoiceModel.operation = operation;
        nlpVoiceModel.semantic = GsonUtil.objectToString(semantic);
        nlpVoiceModel.text = text;
        nlpVoiceModel.dataEntity = GsonUtil.objectToString(data);
        nlpVoiceModel.response = DatastatManager.response;
        return nlpVoiceModel;
    }

    public MutualVoiceModel convert2MutualVoiceModel(MultiSemantic multiSemantic) {
        MutualVoiceModel mutualVoiceModel = new MutualVoiceModel();
        mutualVoiceModel.service = multiSemantic.service;
        mutualVoiceModel.operation = multiSemantic.operation;
        mutualVoiceModel.text = text;
        mutualVoiceModel.response = DatastatManager.response;
        return mutualVoiceModel;
    }

}