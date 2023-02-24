package com.chinatsp.ifly.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.module.me.recommend.bean.HuVoiceAssitContentBean;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static ImageUtils sInstance;
    private Map<String,String> icons ;
    public static ImageUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TtsUtils.class) {
                if (sInstance == null) {
                    sInstance = new ImageUtils(context);
                }
            }
        }
        return sInstance;
    }

    private ImageUtils(Context c){
        icons  = new HashMap<>();
    }


    public String  getCommandIcon(CommandInfo info) {
        String url = info.getSkillIconUrl();
        String path = null;
        if(url!=null){
            if(icons.get(info.getSkillIconUrl())!=null){
                Log.d(TAG, "getCommandIcon() called with: info = [" +  icons.get(url) + "]");
                return icons.get(url);
            } else {
                String iconpath =isIconExist(info);
               if(!"".equals(iconpath)){//当前图片存在
                   icons.put(url,iconpath);
                   return iconpath;
               }
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(info.getSkillIconUrl())
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        byte[] b = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                        path = saveCommandIcon(bitmap,info.getSkillName());
                        icons.put(url,path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return path;
            }
        }
        return path;
    }

    private String imagePaht = Environment.getExternalStorageDirectory().getAbsolutePath() + "/iflytek/ica/picture/skillIcon";
    private  String imagePaht2 = "/data/navi/0/iflytek/ica/picture/skillIcon";
    private String  imageRealPath ="";
    public String saveCommandIcon(Bitmap bitmap,String name) {
        Log.d(TAG, "saveCommandIcon() called with: bitmap = [" + bitmap + "], name = [" + name + "]");
        FileOutputStream out = null;
        File file = null;
//        String sdCardDir ="/data/navi/0/iflytek/ica/picture/skillIcon";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(imagePaht);
            if (!dirFile.exists()) {
                Log.d(TAG,"imagePaht:no exists");
                dirFile = new File(imagePaht2);
                if (!dirFile.exists()){
                    Log.d(TAG,"imagePaht2:no exists");
                    dirFile.mkdirs();
                }else {
                    imageRealPath = imagePaht2;
                }
            }else {
                imageRealPath = imagePaht;
            }
            file = new File(imageRealPath, name+ ".png");
            try {
                out = new FileOutputStream(file);
                if(null != bitmap)
                    //设置为JPG格式，背景色会变黑,修改为PNG格式
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    out.flush();
                    out.close();
                    return file.getAbsolutePath();
//                    if(null != bitmap) bitmap.recycle();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        if(file!=null)
          return file.getAbsolutePath();
        else
            return null;
    }

    public static Bitmap getCompressBitmap(String path){
        if(path==null||"".equals(path))return null;
        Bitmap bitmap = null;
        File file = new File(path);
        BitmapFactory.Options options = null;
        if (!file.exists())
            return null;
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (options == null)
            return null;
        bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, options.outWidth, options.outHeight);
        }
        return bitmap;
    }

    private String isIconExist(CommandInfo info){
//        String sdCardDir ="/data/navi/0/iflytek/ica/picture/skillIcon";
        String name = info.getSkillName();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File dirFile = new File(imagePaht);
            if (!dirFile.exists()) {
                dirFile = new File(imagePaht2);
                Log.d(TAG,"imagePaht:no exists");
                if (!dirFile.exists()){
                    dirFile.mkdirs();
                    Log.d(TAG,"imagePaht2:no exists");
                }else {
                    imageRealPath = imagePaht2;
                }
            }else {
                imageRealPath = imagePaht;
            }

            File file = new File(imageRealPath, name+ ".png");
            if(file.exists()){
                return file.getAbsolutePath();
            }
        }else return "";
        return "";
    }

}
