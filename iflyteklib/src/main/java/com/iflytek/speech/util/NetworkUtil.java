//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.speech.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import javax.xml.parsers.FactoryConfigurationError;

public class NetworkUtil {
    private static final String tag = "NetworkUtil";
    public static final String NET_UNKNOWN = "none";
    public static final String NET_WIFI = "wifi";
    public static final String NET_CMWAP = "cmwap";
    public static final String NET_UNIWAP = "uniwap";
    public static final String NET_CTWAP = "ctwap";
    public static final String NET_CTNET = "ctnet";

    public NetworkUtil() {
    }

    public static String getNetType(NetworkInfo info) {
        if (info == null) {
            return "none";
        } else {
            try {
                if (info.getType() == 1) {
                    return "wifi";
                } else {
                    if(info.getExtraInfo() == null) {
                        return "none";
                    }
                    String extra = info.getExtraInfo().toLowerCase();
                    if (TextUtils.isEmpty(extra)) {
                        return "none";
                    } else if (!extra.startsWith("3gwap") && !extra.startsWith("uniwap")) {
                        if (extra.startsWith("cmwap")) {
                            return "cmwap";
                        } else if (extra.startsWith("ctwap")) {
                            return "ctwap";
                        } else {
                            return extra.startsWith("ctnet") ? "ctnet" : extra;
                        }
                    } else {
                        return "uniwap";
                    }
                }
            } catch (Exception var2) {
                Log.d("NetworkUtil", var2.toString());
                return "none";
            }
        }
    }

    public static String getNetSubType(NetworkInfo info) {
        if (info == null) {
            return "none";
        } else {
            try {
                if (info.getType() == 1) {
                    return "none";
                } else {
                    String subtype = "";
                    subtype = subtype + info.getSubtypeName();
                    subtype = subtype + ";" + info.getSubtype();
                    return subtype;
                }
            } catch (Exception var2) {
                Log.d("NetworkUtil", var2.toString());
                return "none";
            }
        }
    }

    public static boolean isNetworkAvailable2(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities =
                manager.getNetworkCapabilities(manager.getActiveNetwork());
        if(networkCapabilities != null) {
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        return false;
    }

    /**
     * ConnectivityManager.MULTIPATH_PREFERENCE_HANDOVER
     * 标准的要传TYPE_MOBILE 但会出现空指针异常
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager con = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifi = false, internet = false;
//        boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
//        boolean internet = con.getNetworkInfo(ConnectivityManager.MULTIPATH_PREFERENCE_HANDOVER).isConnectedOrConnecting();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isAvailable()) {
            int type = networkInfo.getType();
            switch (type) {
                case ConnectivityManager.TYPE_WIFI:
                    Log.d("xyj000", "wifi 可用");
                    wifi = true;
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    Log.d("xyj000", networkInfo.toString());
                    if(networkInfo.getDetailedState() == NetworkInfo.DetailedState.SUSPENDED) {
                        Log.d("xyj000", "tbox网络可用，但无法连接internet");
                        internet = false;
                    } else if(networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        internet = true;
                        Log.d("xyj000", "tbox网络可用，可以连接internet");
                    }
                    break;
            }
        }
        Log.d("xyj000", "isNetworkAvailable: wifi::"+wifi+"..internet:"+internet);

        if (wifi | internet) {
            return true;
        } else {
            return false;
        }
    }
}
