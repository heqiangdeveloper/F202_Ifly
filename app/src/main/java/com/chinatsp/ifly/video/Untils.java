package com.chinatsp.ifly.video;


import com.chinatsp.ifly.api.constantApi.AppConstant;

import java.io.File;


public class Untils {

    public static String accuratePath(String path) {
        if (!path.equals(AppConstant.usbRoot)) {
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }
        return path;
    }

}
