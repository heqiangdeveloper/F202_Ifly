package com.chinatsp.ifly.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Description : OkHttp网络连接封装工具类
 */
public class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";

    private static OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;

    private OkHttpUtils() {

        //增加头部信息
        Interceptor headerInterceptor =new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request build = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("appVersion", "1.0")
                        .build();
                return chain.proceed(build);
            }
        };

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(interceptor)
                .build();


        mDelivery = new Handler(Looper.getMainLooper());
    }

    private synchronized static OkHttpUtils getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpUtils();
        }
        return mInstance;
    }

    private void getRequest(String url, final ResultCallback callback) {
        final Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }

    private void postRequest(String url, final ResultCallback callback, List<Param> params) {
        Request request = buildPostRequest(url, params);
        deliveryResult(callback, request);
    }

    private void postRequest(String url, final ResultCallback callback, String json) {
        Log.d(TAG,"lh:requestUrl-"+url+",json:"+json);
        Request request = buildPostRequest(url, json);
        deliveryResult(callback, request);
    }

    private void postFormRequest(String url, final ResultCallback callback, Map<String,String> values) {
        Log.d(TAG,"lh:requestUrl-"+url+",json:"+values.toString());
        Request request = buildFormPostRequest(url, values);
        deliveryResult(callback, request);
    }


    private void deliveryResult(final ResultCallback callback, Request request) {

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailCallback(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body().string();
                    if (callback.mType == String.class) {
                        sendSuccessCallBack(callback, str);
                    } else {
                        Object object = deserialize(str, callback.mType);
                        sendSuccessCallBack(callback, object);
                    }
                } catch (final Exception e) {
                    sendFailCallback(callback, e);
                }
            }
        });
    }

    /**
     * 将json字符串转换为对象
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    private static <T> T deserialize(String json, Type type) throws JsonSyntaxException {
        return new Gson().fromJson(json, type);
    }

    /**
     * 检查字符串是不是标瘵
     * @param json
     * @return
     */
    private static boolean isGoodJson(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            Log.d(TAG, "bad json: " + json);
            return false;
        }
    }

    private void sendFailCallback(final ResultCallback callback, final Exception e) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void sendSuccessCallBack(final ResultCallback callback, final Object obj) {
        try {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onSuccess(obj);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request buildPostRequest(String url, List<Param> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Param param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder().url(url).post(requestBody).build();
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Request buildPostRequest(String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        return new Request.Builder().url(url).post(requestBody).build();
    }

    private Request buildFormPostRequest(String url, Map<String,String> value) {
//        RequestBody requestBody = RequestBody.create(JSON, json);
        if (value == null) return null;
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBodyBuilder.addFormDataPart("access_token", value.get("access_token"));
        requestBodyBuilder.addFormDataPart("timestamp", value.get("timestamp"));
        requestBodyBuilder.addFormDataPart("projectid", value.get("projectid"));
        if (value.size() >3) {
            requestBodyBuilder.addFormDataPart("projectversion", value.get("projectversion"));
        }
        RequestBody requestBody = requestBodyBuilder.build();
        return new Request.Builder().url(url).post(requestBody).build();
    }


    /**********************对外接口************************/

    /**
     * get请求
     * @param url  请求url
     * @param callback  请求回调
     */
    public static void get(String url, ResultCallback callback) {
        getInstance().getRequest(url, callback);
    }

    /**
     * post请求
     * @param url       请求url
     * @param callback  请求回调
     * @param params    请求参数
     */
    public static void post(String url, final ResultCallback callback, List<Param> params) {
        getInstance().postRequest(url, callback, params);
    }

    public static void postJson(String url, final ResultCallback callback, String json) {
        boolean goodJson = isGoodJson(json);
        if (!goodJson) {
            throw new InvalidParameterException("It's not good json: " + json);
        }
        getInstance().postRequest(url, callback, json);
    }

    public static void postFormData(String url, final ResultCallback callback, Map<String,String> values) {

        if (values == null) {
            throw new InvalidParameterException("It's not good json: " + values);
        }
        getInstance().postFormRequest(url, callback, values);
    }

    /**
     * http请求回调类,回调方法在UI线程中执行
     * @param <T>
     */
    public static abstract class ResultCallback<T> {

        Type mType; //<T> 类型

        public ResultCallback(){
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        /**
         * 请求成功回调
         * @param response
         */
        public abstract void onSuccess(T response);

        /**
         * 请求失败回调
         * @param e
         */
        public abstract void onFailure(Exception e);
    }

    /**
     * post请求参数类
     */
    public static class Param {

        String key;
        String value;

        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }
}
