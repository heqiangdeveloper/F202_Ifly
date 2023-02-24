package com.chinatsp.ifly.utils;

import android.os.AsyncTask;
import android.util.LruCache;

import com.chinatsp.ifly.base.BaseApplication;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

public class AssertsLoaderUtils {
    private static AssertsLoaderUtils sAssertsLoader;
    private LruCache<String, String> mMemoryCache;

    public static AssertsLoaderUtils getInstance() {
        if (sAssertsLoader == null) {
            sAssertsLoader = new AssertsLoaderUtils();
        }
        return sAssertsLoader;
    }

    private AssertsLoaderUtils() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        int cacheSize = maxMemory / 16;
        mMemoryCache = new LruCache<>(cacheSize);
    }

    public void addAssertsToMemoryCache(String key, String value) {
        if (getAssertsFromMemCache(key) == null) {
            mMemoryCache.put(key, value);
        }
    }

    public String getAssertsFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void setMvwKeyWords(int scene, String assetsKey) {
        String newKey = assetsKey.replace("/", "_");
        final String keywords = getAssertsFromMemCache(newKey);
        if (keywords != null) {
            MVWAgent.getInstance().setMvwKeyWords(scene, keywords);
        } else {
            MvwAssertsWorkerTask task = new MvwAssertsWorkerTask(scene);
            task.execute(newKey);
        }
    }

    public void setSrStksCmd(String assetsKey) {
        String newKey = assetsKey.replace("/", "_");
        final String stksCmd = getAssertsFromMemCache(newKey);
        if (stksCmd != null) {
            SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stksCmd);
        } else {
            SrAssertsWorkerTask task = new SrAssertsWorkerTask();
            task.execute(newKey);
        }
    }

    class MvwAssertsWorkerTask extends AsyncTask<String, Void, String> {

        private int scene;

        public MvwAssertsWorkerTask(int scene) {
            this.scene = scene;
        }

        @Override
        protected String doInBackground(String... params) {
            String keywords = Utils.getFromAssets(BaseApplication.getInstance(), params[0]);
            addAssertsToMemoryCache(params[0], keywords);
            return keywords;
        }

        @Override
        protected void onPostExecute(String keywords) {
            super.onPostExecute(keywords);
            if (keywords != null) {
                MVWAgent.getInstance().setMvwKeyWords(scene, keywords);
            }
        }
    }

    class SrAssertsWorkerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String keywords = Utils.getFromAssets(BaseApplication.getInstance(), params[0]);
            addAssertsToMemoryCache(params[0], keywords);
            return keywords;
        }

        @Override
        protected void onPostExecute(String stksCmd) {
            super.onPostExecute(stksCmd);
            if (stksCmd != null) {
                SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stksCmd);
            }
        }
    }
}
