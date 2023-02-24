package com.chinatsp.ifly.utils;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadClass {

    private static LoadClass loadClass = new LoadClass();

    public static LoadClass getInstance(){
        return loadClass;
    }

    private static String TAG = "Polyphone";

    public static HashMap<String,String> map;

    /**
     * 加载文字读音修正的配置文本，路径，文件名可以自定
     */
    public void loadPolyphone(){
        new Thread(){
            @Override
            public void run() {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream( "sdcard/iflytek/ica/tts/externalTTS/Polyphone.json"));
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = "";
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((line = bufferedReader.readLine())!=null){
                        stringBuffer.append(line);
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    JSONArray jsonArray = new JSONArray(stringBuffer.toString());
                    map= new HashMap<>();
                    JSONObject jsonObject1 = null;
                    for (int i = 0; i <jsonArray.length() ; i++) {
                        jsonObject1 = jsonArray.getJSONObject(i);
                        Log.d(TAG, "loadPolyphone: "+jsonObject1.toString());
                        Log.d(TAG, "word:"+jsonObject1.getString("word")+" ,replace:"+jsonObject1.getString("replace"));
                        map.put(jsonObject1.getString("word"),jsonObject1.getString("replace"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public String getStringNew(String text){
        String temp = text;
        // 如果没有加载到对应多音字文本，则直接跳过
        if(map == null||map.keySet().size() == 0){
            return text;
        }
        // 首先遍历所有多音字表格，将多音字替换完
        for (String key:map.keySet()) {
            if(temp.contains(key)){
                Log.d(TAG, "replace: key :"+key+" ,replace:"+map.get(key));
                temp = temp.replace(key,map.get(key));
            }
        }

        // 对于AM,FM的播报，使用中文播报
        temp = getRadio(temp);
        Log.d(TAG, "getStringNew: "+temp);
        return temp;
    }

    public String getRadio(String text){
		// 匹配AM/FM的正则规则
        String reg = "((?i)(AM|FM))(\\d{1,4}.\\d{1}|\\d{1,4})";
        Pattern pattern = Pattern.compile(reg);
        String matchText = text;
        Matcher matcher = pattern.matcher(matchText);
        HashSet<String> strings = new HashSet<>();
        while (matcher.find()){
            String temp = matcher.group();
            Log.d(TAG, "getRadio: "+temp);
            strings.add(temp);
            matchText = matchText.substring(matchText.indexOf(temp)+temp.length());
        }
        Log.d(TAG, "getRadio: "+strings.size());
        for (String sets: strings
        ) {
            if(sets.contains(".")){
                continue;
            }
            String temp = sets.substring(0,2)+"<figure>"+sets.substring(2)+"</figure type=ordinal>";
            text = text.replace(sets,temp);
        }
        Log.d(TAG, "getRadio: "+text);
        return text;
    }
}
