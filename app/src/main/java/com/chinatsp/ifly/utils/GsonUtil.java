package com.chinatsp.ifly.utils;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class GsonUtil {
    private static Gson mGson = new Gson();

    /**
     * 将json字符串转化成实体对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T stringToObject(String json, Class<T> classOfT) {
        return mGson.fromJson(json, classOfT);
    }

    /**
     * 将对象准换为json字符串 或者 把list 转化成json
     *
     * @param object
     * @param <T>
     * @return
     */
    public static <T> String objectToString(T object) {
        return mGson.toJson(object);
    }

    /**
     * 把json 字符串转化成list
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> stringToList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }


//    /**
//     * 将json字符串转化成实体对象
//     * @param reader
//     * @param clazz
//     * @param <T>
//     * @return
//     */
//    public static <T> RspBean<T> fromJsonObject(String reader, Class<T> clazz) {
//        Type type = new ParameterizedTypeImpl(RspBean.class, new Class[]{clazz});
//        return new Gson().fromJson(reader, type);
//    }
//
//    /**
//     * 把json 字符串转化成list
//     * @param reader
//     * @param clazz
//     * @param <T>
//     * @return
//     */
//    public static <T> RspBean<List<T>> fromJsonArray(String reader, Class<T> clazz) {
//        // 生成List<T> 中的 List<T>
//        Type listType = new ParameterizedTypeImpl(List.class, new Class[]{clazz});
//        // 根据List<T>生成完整的Result<List<T>>
//        Type type = new ParameterizedTypeImpl(RspBean.class, new Type[]{listType});
//        return new Gson().fromJson(reader, type);
//    }

    public static boolean isJson(String content){
        if (TextUtils.isEmpty(content)){
            return false;
        }
        try {
            Gson gson = new Gson();
            gson.fromJson(content, JsonObject.class);
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
