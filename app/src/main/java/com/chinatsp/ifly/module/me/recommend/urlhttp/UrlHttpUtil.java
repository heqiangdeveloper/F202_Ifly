package com.chinatsp.ifly.module.me.recommend.urlhttp;

import java.io.File;
import java.util.List;
import java.util.Map;


public class UrlHttpUtil {
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    public static final String FILE_TYPE_FILE = "file/*";
    public static final String FILE_TYPE_IMAGE = "image/*";
    public static final String FILE_TYPE_AUDIO = "audio/*";
    public static final String FILE_TYPE_VIDEO = "video/*";

    /**
     * get请求
     * @param url：url
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void get(String url,int postion,int id, CallBackUtil callBack) {
        get(url,postion,id, null, null, callBack);
    }

    /**
     * get请求，可以传递参数
     * @param url：url
     * @param paramsMap：map集合，封装键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void get(String url, int position,int id,Map<String, String> paramsMap, CallBackUtil callBack) {
        get(url, position,id,paramsMap, null, callBack);
    }

    /**
     * get请求，可以传递参数
     * @param url：url
     * @param paramsMap：map集合，封装键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void get(String url,int position,int id,Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_GET, url, position,id,paramsMap, headerMap, callBack).execute();
    }

    /**
     * post请求
     * @param url：url
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void post(String url, int position,int id,CallBackUtil callBack) {
        post(url,position, id,null, callBack);
    }

    /**
     * post请求，可以传递参数
     * @param url：url
     * @param paramsMap：map集合，封装键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void post(String url, int postion,int id,Map<String, String> paramsMap, CallBackUtil callBack) {
        post(url, postion,id,paramsMap, null, callBack);
    }

    /**
     * post请求，可以传递参数
     * @param url：url
     * @param paramsMap：map集合，封装键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void post(String url, int position,int id,Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_POST,url,position,id,paramsMap,headerMap,callBack).execute();
    }
    /**
     * post请求，可以传递参数
     * @param url：url
     * @param jsonStr：json格式的键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void postJson(String url, int postion,int id,String jsonStr, CallBackUtil callBack) {
        postJson(url, postion,id,jsonStr, null, callBack);
    }

    /**
     * post请求，可以传递参数
     * @param url：url
     * @param jsonStr：json格式的键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void postJson(String url,int position,int id, String jsonStr, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url,position,id,jsonStr,headerMap,callBack).execute();
    }




    /**
     * post请求，上传单个文件
     * @param url：url
     * @param file：File对象
     * @param fileKey：上传参数时file对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。还可以重写onProgress方法，得到上传进度
     */
    public static void uploadFile(String url, int postion,int id,File postionfile, String fileKey, String fileType, CallBackUtil callBack) {
        uploadFile(url, postion,id,postionfile, fileKey,fileType, null, callBack);
    }

    /**
     * post请求，上传单个文件
     * @param url：url
     * @param file：File对象
     * @param fileKey：上传参数时file对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。还可以重写onProgress方法，得到上传进度
     */
    public static void uploadFile(String url,int postion, int id,File file, String fileKey, String fileType, Map<String, String> paramsMap, CallBackUtil callBack) {
        uploadFile(url,postion, id,file,fileKey, fileType, paramsMap, null, callBack);
    }

    /**
     * post请求，上传单个文件
     * @param url：url
     * @param file：File对象
     * @param fileKey：上传参数时file对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。还可以重写onProgress方法，得到上传进度
     */
    public static void uploadFile(String url, int position,int id,File file, String fileKey, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url,position,id,file,null,null,fileKey,fileType,paramsMap,headerMap,callBack).execute();
    }


    /**
     * post请求，上传多个文件，以list集合的形式
     * @param url：url
     * @param fileList：集合元素是File对象
     * @param fileKey：上传参数时fileList对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadListFile(String url, int postion,int id,List<File> fileList, String fileKey, String fileType, CallBackUtil callBack) {
        uploadListFile(url, postion,id,fileList, fileKey, fileType,null, callBack);
    }

    /**
     * post请求，上传多个文件，以list集合的形式
     * @param url：url
     * @param fileList：集合元素是File对象
     * @param fileKey：上传参数时fileList对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadListFile(String url,int position,int id, List<File> fileList, String fileKey, String fileType, Map<String, String> paramsMap, CallBackUtil callBack) {
        uploadListFile(url,position,id, fileList, fileKey, fileType,paramsMap, null, callBack);
    }

    /**
     * post请求，上传多个文件，以list集合的形式
     * @param url：url
     * @param fileList：集合元素是File对象
     * @param fileKey：上传参数时fileList对应的键
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadListFile(String url, int position,int id,List<File> fileList, String fileKey, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url,position,id,null,fileList,null,fileKey,fileType,paramsMap,headerMap,callBack).execute();
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     * @param url：url
     * @param fileMap：集合key是File对象对应的键，集合value是File对象
     * @param fileType：File类型，是image，video，audio，file
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadMapFile(String url,int position,int id, Map<String, File> fileMap, String fileType, CallBackUtil callBack) {
        uploadMapFile(url, position,id,fileMap, fileType, null, callBack);
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     * @param url：url
     * @param fileMap：集合key是File对象对应的键，集合value是File对象
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadMapFile(String url,int position,int id, Map<String, File> fileMap, String fileType, Map<String, String> paramsMap, CallBackUtil callBack) {
        uploadMapFile(url,position, id,fileMap, fileType, paramsMap, null, callBack);
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     * @param url：url
     * @param fileMap：集合key是File对象对应的键，集合value是File对象
     * @param fileType：File类型，是image，video，audio，file
     * @param paramsMap：map集合，封装键值对参数
     * @param headerMap：map集合，封装请求头键值对
     * @param callBack：回调接口，onFailure方法在请求失败时调用，onResponse方法在请求成功后调用，这两个方法都执行在UI线程。
     */
    public static void uploadMapFile(String url, int position,int id,Map<String, File> fileMap, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url,position,id,null,null,fileMap,null,fileType,paramsMap,headerMap,callBack).execute();
    }

    /**
     * 加载图片
     */
    public static void getBitmap(String url,int position,int id, CallBackUtil.CallBackBitmap callBack) {
        getBitmap(url,position,id,null, callBack);
    }
    /**
     * 加载图片，带参数
     */
    public static void getBitmap(String url,int position,int id,Map<String, String> paramsMap,  CallBackUtil.CallBackBitmap callBack) {
        get(url, position,id,paramsMap, null, callBack);
    }

    /**
     * 下载文件,不带参数
     */
    public static void downloadFile(String url, int position,int id,CallBackUtil.CallBackFile callBack) {
        downloadFile(url,position,id,null,callBack);
    }

    /**
     * 下载文件,带参数
     */
    public static void downloadFile(String url,int position,int id, Map<String, String> paramsMap, CallBackUtil.CallBackFile callBack) {
        downloadFile(url, position,id,paramsMap, null, callBack);
    }
    /**
     * 下载文件,带参数,带请求头
     */
    public static void downloadFile(String url, int position,int id,Map<String, String> paramsMap,Map<String, String> headerMap, CallBackUtil.CallBackFile callBack) {
        get(url, position,id,paramsMap, headerMap, callBack);
    }

}
