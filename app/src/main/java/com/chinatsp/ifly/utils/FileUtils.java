package com.chinatsp.ifly.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;

public class FileUtils {

    //向指定的文件中写入指定的数据
    public static void writeFileData(Context context, String filename, String message) {
        if(TextUtils.isEmpty(message)) {
            Log.e("FileUtils", "writeFileData is empty");
            return;
        }
        try {
            FileOutputStream fout = context.openFileOutput(filename, Context.MODE_PRIVATE);
            //将要写入的字符串转换为byte数组
            byte[] bytes = message.getBytes();
            fout.write(bytes);//将byte数组写入文件
            fout.close();//关闭文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //打开指定文件，读取其数据，返回字符串对象
    public static String readFileData(Context context, String fileName) {
        String result = "";
        try {
            FileInputStream fis = context.openFileInput(fileName);
            int len = fis.available();
            byte[] buffer = new byte[len];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((fis.read(buffer)) != -1) {
                baos.write(buffer);
            }
            byte[] data = baos.toByteArray();
            baos.close();
            fis.close();
            return new String(data);
        } catch (Exception e) {
            //Log.e("FileUtils", "exception :" + e.toString());
        }
        return result;
    }

    public static boolean deleteFile(Context context, String fileName) {
        boolean ret = false;
        String filePath = context.getFilesDir().getAbsolutePath() + File.separator + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            ret = file.delete();
        }
        return ret;
    }
}