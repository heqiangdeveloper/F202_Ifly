//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.speech.sr;

import android.util.Log;
import com.iflytek.speech.libisssr;

import java.io.File;
import java.util.Random;

public class SRSolution implements ISRService {
    private static final String tag = "SRSolution";
    private String ID = null;
    private static SRSolution instance = null;
    private ISRListener mSRListener = null;
    private static String IMEI = null;
    private static String mSerialNumber = null;
    private int miAcousLang = 0;
    private String ResDir = null;
    private boolean create_done = false;

    private SRSolution() {
    }

    public static SRSolution getInstance() {
        return instance;
    }

    public static SRSolution getInstance(String imei) {
        if (imei == null) {
            Log.e("SRSolution", "IMEI is null");
            return null;
        } else {
            if (instance == null) {
                instance = new SRSolution();
            }

            IMEI = imei;
            return instance;
        }
    }

    public int setMachineCode(String code) {
        return libisssr.setMachineCode(code);
    }

    public int getActiveKey(String resDir) {
        Log.d("SRSolution", "getActiveKey");
        if (resDir != null) {
            resDir = resDir.replaceFirst("/$", "");
        }

        int id = libisssr.activate(resDir);
        return id == 0 ? 0 : libisssr.getActiveKey(resDir);
    }

    public String create(String ResDir, ISRListener srListener) {
        Log.d("SRSolution", "sessionCreate");
        if (ResDir != null) {
            ResDir = ResDir.replaceFirst("/$", "");
        }

        if (this.create_done) {
            this.mSRListener = srListener;
            this.ResDir = ResDir;
            if (this.ID == null) {
                this.ID = this.getSessionId();
            }

            Log.d("SRSolution", "Already created, return ID:" + this.ID);
            Thread t1 = new Thread(new SRSolution.sendCreateMsgThread());
            t1.start();
            return this.ID;
        } else {
            this.ID = this.getSessionId();
            this.mSRListener = srListener;
            this.ResDir = ResDir;

            int nRet = libisssr.setMachineCode(IMEI);
            Log.d("SRSolution", "create: IMEI::"+IMEI+"............nRet---->"+nRet);
            if (mSerialNumber != null) {
                nRet = libisssr.setSerialNumber(mSerialNumber);
            }

            Log.d("SRSolution", "setSerialNumber return = " + nRet);
            int activateId = libisssr.activate(ResDir);
            if (activateId != 0 && mSerialNumber != null) {
                Log.d("SRSolution", "create session activate error,id = " + activateId);
                nRet = libisssr.getActiveKey(ResDir);
                Log.d("SRSolution", "getActiveKey return = " + nRet);
                activateId = libisssr.activate(ResDir);
            }

            if (activateId != 0) {
                Log.d("SRSolution", "create session activate error,id = " + activateId);
                return activateId + "";
            } else {
                Thread t = new Thread(new SRSolution.createsrthread());
                t.start();
                Log.d("SRSolution", "sessionCreate success, return");
                return this.ID;
            }
        }
    }

    public int sessionStart(String sid, String szScene, int iMode, String szCmd) {
        Log.d("SRSolution", "sessionStart, sid=" + sid + " szScene=" + szScene + " iMode=" + iMode);
        if (this.ID != null && sid.equals(this.ID)) {
            return libisssr.start(szScene, iMode, szCmd);
        } else {
            Log.e("SRSolution", "ISS_ERROR_NO_LICENSE, sid!=ID");
            return 20000;
        }
    }

    public int setParam(String sid, String szParam, String szParamValue) {
        Log.d("SRSolution", "setParam");
        return sid != null && sid.equals(this.ID) ? libisssr.setParam(szParam, szParamValue) : 20000;
    }

    public int uploadDict(String sid, String szList, int bOnlyUploadToCloud) {
        Log.d("SRSolution", "uploadDict");
        return sid != null && sid.equals(this.ID) ? libisssr.uploadDict(szList, bOnlyUploadToCloud) : 20000;
    }

    public int uploadData(String sid, String szList, int bOnlyUploadToCloud) {
        Log.d("SRSolution", "uploadData");
        return sid != null && sid.equals(this.ID) ? libisssr.uploadData(szList, bOnlyUploadToCloud) : 20000;
    }

    public int appendAudioData(String sid, byte[] audioBuffer, int BufferLength) {
        if (sid != null && sid.equals(this.ID)) {
            return audioBuffer != null && BufferLength != 0 ? libisssr.appendAudioData(audioBuffer, BufferLength) : 0;
        } else {
            return 20000;
        }
    }

    public int endAudioData(String sid) {
        Log.d("SRSolution", "endAudioData");
        return sid != null && sid.equals(this.ID) ? libisssr.endAudioData() : 20000;
    }

    public int sessionStop(String sid) {
        Log.d("SRSolution", "SessionStop");
        return sid != null && sid.equals(this.ID) ? libisssr.stop() : 20000;
    }

    public int resetSession(String sid) {
        Log.d("SRSolution", "resetSession");
        if (sid != null && sid.equals(this.ID)) {
            return libisssr.resetSession();
        } else {
            return 20000;
        }
    }

    public int destroy(String sid) {
        Log.d("SRSolution", "destroy");
        if (sid != null && sid.equals(this.ID)) {
            this.create_done = false;
            return libisssr.destroy();
        } else {
            return 20000;
        }
    }

    public void OnSRMsgProc(long uMsg, long wParam, String lParam) {
        try {
            if (this.mSRListener != null) {
                if (lParam == null) {
                    lParam = "";
                }

                if (uMsg != 20003L) {
                    Log.d("SRSolution", "uMsg=" + uMsg + ", wParam=" + wParam + ".");
                }

                this.mSRListener.onSRMsgProc_(uMsg, wParam, lParam);
            } else {
                Log.d("SRSolution", "listener is null");
            }
        } catch (NullPointerException var7) {
            var7.printStackTrace();
        }

    }

    private String getSessionId() {
        Log.d("SRSolution", "getSessionId");
        String id = null;
        long curTime = System.currentTimeMillis();
        Random random = new Random();
        long r = random.nextLong();
        String timeString = String.valueOf(curTime);
        String rString = String.valueOf(r);
        id = "iflytek" + timeString + rString;
        return id;
    }

    public String mspSearch(String sid, String szText, String szExternParam) {
        Log.d("SRSolution", "mspSearch");
        return sid != null && sid.equals(this.ID) ? libisssr.mspSearch(szText, szExternParam) : "";
    }

    public String localNli(String sid, String szText, String szScene) {
        Log.d("SRSolution", "localNli");
        return sid != null && sid.equals(this.ID) ? libisssr.localNli(szText, szScene) : "";
    }

    public int setSerialNumber(String strSerialNumber) {
        Log.d("SRSolution", "setSerialNumber");
        mSerialNumber = strSerialNumber;
        return 0;
    }

    public String createEx(int iAcousLang, String ResDir, ISRListener srListener) {
        Log.d("SRSolution", "sessionCreateEx");
        if (ResDir != null) {
            ResDir = ResDir.replaceFirst("/$", "");
        }

        if (this.create_done) {
            this.mSRListener = srListener;
            this.ResDir = ResDir;
            if (this.ID == null) {
                this.ID = this.getSessionId();
            }

            Log.d("SRSolution", "Already created, return ID:" + this.ID);
            Thread t1 = new Thread(new SRSolution.sendCreateMsgThread());
            t1.start();
            return this.ID;
        } else {
            this.ID = this.getSessionId();
            this.miAcousLang = iAcousLang;
            this.mSRListener = srListener;
            this.ResDir = ResDir;
            int nRet = libisssr.setMachineCode(IMEI);
            Log.d("SRSolution", "setMachineCode return = " + nRet+"........."+IMEI);
            if (mSerialNumber != null) {
                nRet = libisssr.setSerialNumber(mSerialNumber);
            }

            int activateId = libisssr.activate(ResDir);
            if (activateId != 0 && mSerialNumber != null) {
                Log.d("SRSolution", "create session activate error,id = " + activateId);
                nRet = libisssr.getActiveKey(ResDir);
                Log.d("SRSolution", "getActiveKey return = " + nRet);
                activateId = libisssr.activate(ResDir);
            }

            if (activateId != 0) {
                Log.d("SRSolution", "create session activate error,id = " + activateId);
                return activateId + "";
            } else {
                Thread t = new Thread(new SRSolution.createExsrthread());
                t.start();
                Log.d("SRSolution", "sessionCreateEx success, return");
                return this.ID;
            }
        }
    }

    private class createExsrthread implements Runnable {
        private createExsrthread() {
        }

        public void run() {
            int errid = libisssr.createEx(SRSolution.this.miAcousLang, SRSolution.this.ResDir, SRSolution.this.mSRListener);
            if (errid != 0) {
                Log.e("SRSolution", "createEx sr failed, Error ID=" + String.valueOf(errid));
                SRSolution.this.OnSRMsgProc(10102L, 0L, (String)null);

            } else {
                Log.d("SRSolution", "createEx success");
                SRSolution.this.create_done = true;
            }

        }
    }

    private class createsrthread implements Runnable {
        private createsrthread() {
        }

        public void run() {
            int machineRet = libisssr.setMachineCode(SRSolution.IMEI);
            Log.d("SRSolution", "run() called::machineRet:::"+SRSolution.IMEI+"....machineRet----->"+machineRet);
            if (machineRet != 0) {
                Log.e("SRSolution", "setMachineCode failed.");
            }

            int errid = libisssr.create(SRSolution.this.ResDir, SRSolution.this.mSRListener);
            if (errid != 0) {
                Log.e("SRSolution", "create sr failed, Error ID=" + String.valueOf(errid));
                SRSolution.this.OnSRMsgProc(10102L, 0L, (String)null);
            } else {
                Log.d("SRSolution", "create success");
                SRSolution.this.create_done = true;
            }

        }
    }

    private class sendCreateMsgThread implements Runnable {
        private sendCreateMsgThread() {
        }

        public void run() {
            SRSolution.this.OnSRMsgProc(10121L, 0L, SRSolution.this.ID);
        }
    }
}
