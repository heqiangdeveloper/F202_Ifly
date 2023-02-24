//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.adapter.ttsservice;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;

public class ClientMap<K, V> extends HashMap<K, V> {
    public static final String TAG = "ClientMap";
    private static final long serialVersionUID = -6829352301501096392L;
    private LinkedList<K> keyList = new LinkedList();
    private int numberLimit;

    public ClientMap(int numberLimit) {
        this.numberLimit = numberLimit;
    }

    public boolean putClient(K client, V session) {
        if (this.containsKey(client)) {
            return false;
        } else {
            if (this.size() < this.numberLimit) {
                this.put(client, session);
                this.keyList.add(client);
            } else {
                this.remove(this.keyList.poll());
                this.put(client, session);
                this.keyList.add(client);
            }

            Log.d("ClientMap", "putClient is called:");
            Log.d("ClientMap", "mapSize = " + this.size());
            return true;
        }
    }

    public void removeClient(K client) {
        this.remove(client);
        this.keyList.remove(client);
        Log.d("ClientMap", "removeClient is called:");
        Log.d("ClientMap", "mapSize = " + this.size());
    }

    public int getPositionLeft() {
        return this.numberLimit - this.size();
    }
}
