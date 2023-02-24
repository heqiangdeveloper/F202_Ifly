package com.iflytek.adapter.sr;

public class SrSessionArgu {
    public String scene;
    public int mode;
    public int onlyUploadToCloud;
    public String szCmd;

    public SrSessionArgu(SrSessionArgu other) {
        this.scene = other.scene;
        this.mode = other.mode;
        this.onlyUploadToCloud = other.onlyUploadToCloud;
        this.szCmd = other.szCmd;
    }

    public SrSessionArgu(String scene, int mode, int onlyUploadToCloud) {
        this.scene = scene;
        this.mode = mode;
        this.onlyUploadToCloud = onlyUploadToCloud;
        this.szCmd = "";
    }
}
