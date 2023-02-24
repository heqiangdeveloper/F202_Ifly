package com.iflytek.seopt;

import java.lang.reflect.Method;

public class SeoptConstant {

    /**
     * 是否使用窄波束
     */
//    public static final boolean USE_SEOPT = getProperty("persist.sys.switch.seopt", "0").equals("1");
    public static final boolean USE_SEOPT = false;

    private static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }
}
