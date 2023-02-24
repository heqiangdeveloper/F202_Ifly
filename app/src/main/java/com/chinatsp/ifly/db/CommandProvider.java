package com.chinatsp.ifly.db;

import android.content.Context;
import android.util.Log;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.db.entity.CommandInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CommandProvider {
    private static final String TAG = "CommandProvider";
    private static volatile CommandProvider mInstance;
    private Context mContext;

    public static CommandProvider getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CommandProvider.class) {
                if (mInstance == null) {
                    mInstance = new CommandProvider(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    private CommandProvider(Context context) {
        mContext = context;

    }

    public synchronized List<String> getCommandTypes (){
        List<String> commandTypes=  CommandDbDao.getInstance(mContext).queryModules();
        if(commandTypes.size()==0){
            commandTypes.add("免唤醒");
            commandTypes.add("导航");
            commandTypes.add("娱乐");
            commandTypes.add("电话");
            commandTypes.add("系统");
            commandTypes.add("空调");
        }
       return commandTypes;
    }

    public void deleteCommandData(){
        CommandDbDao.getInstance(mContext).deleteAllData();
    }

    public synchronized List<String> queryModelSkills(String name){
        List<String> skills=  CommandDbDao.getInstance(mContext).queryModelSkills(name);
        return skills;
    }
    public synchronized ArrayList<CommandInfo> queryModelSkillContents(String model, String name){
        ArrayList<CommandInfo> skills=  CommandDbDao.getInstance(mContext).queryModelSkillContents(model,name);
        return skills;
    }

    public Map<String, Map<String, ArrayList<CommandInfo>>> stringMapMap = new HashMap<>();
    public void insertData(String model){
        Observable.just(model).map(new Function<String, List<String>>() {
            @Override
            public List<String> apply(String s) throws Exception {
                return queryModelSkills(s);
            }
        }).map(new Function<List<String>, Map<String,ArrayList<CommandInfo>>>() {
            @Override
            public Map<String, ArrayList<CommandInfo>> apply(List<String> strings) throws Exception {
                Map<String, ArrayList<CommandInfo>> skills= new LinkedHashMap<>();
                for (int i = 0; i <strings.size() ; i++) {
                    ArrayList<CommandInfo> commandInfoArrayList =queryModelSkillContents(model,strings.get(i));
                    skills.put(strings.get(i),commandInfoArrayList);
                }
                return skills;
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Consumer<Map<String, ArrayList<CommandInfo>>>() {
            @Override
            public void accept(Map<String, ArrayList<CommandInfo>> stringListMap) throws Exception {
                stringMapMap.put(model,stringListMap);
                Log.d(TAG,"stringMapMap ="+stringMapMap.size());
            }
        });
    }

    public Map<String, ArrayList<CommandInfo>> getCommandInfo(String key){
        return stringMapMap.get(key);
    }

    public void initCommandTypeInfo(){
        List<String> stringList =getCommandTypes();
        for (String string:stringList ){
            insertData(string);
        }
    }
}
