package com.chinatsp.ifly.voice.platformadapter.entity;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/8/4
 */

public class TtsText {
    /**
     * service : carNumber
     * url :
     * skillname : 垃圾分类查询
     * errtext : 网络不稳定，请稍后再试
     */

    private String service;
    private String url;
    private String skillname;
    private String errtext;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSkillname() {
        return skillname;
    }

    public void setSkillname(String skillname) {
        this.skillname = skillname;
    }

    public String getErrtext() {
        return errtext;
    }

    public void setErrtext(String errtext) {
        this.errtext = errtext;
    }
}
