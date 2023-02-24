package com.iflytek.adapter.oneshot;

public class CycleQueue {
    private byte[] arr;
    private int maxSize;// 最大空间
    private int len;// 有效长度
    private int end;// 队尾
    private boolean isFull;

    public CycleQueue(int size) {
        this.maxSize = size;
        this.arr = new byte[maxSize];
        this.len = 0;
        this.end = -1;
        this.isFull = false;
    }

    /**
     * 从队尾插入数据
     *
     * @param value
     */
    public void insert(byte value) {
        //如果满了
        if (end == maxSize - 1) {
            isFull = true;
            end = -1;
        }
        arr[++end] = value;
        if (isFull) {
            len = maxSize;
        } else {
            len++;
        }
    }

    public synchronized void produce(byte[] voiceBytes) {
        for (int i = 0; i < voiceBytes.length; i++) {
            insert(voiceBytes[i]);
        }
    }

    public synchronized byte[] consumeAll() {
        byte[] localBuffer = new byte[0];
        if (isFull) {
            localBuffer = new byte[maxSize];
            System.arraycopy(arr, end + 1, localBuffer, 0, maxSize - end - 1);
            if (end > -1) {
                System.arraycopy(arr, 0, localBuffer, maxSize - end - 1, end + 1);
            }
        } else {
            if (len > 0) {
                localBuffer = new byte[len];
                System.arraycopy(arr, 0, localBuffer, 0, len);
            }
        }
        this.len = 0;
        this.end = -1;
        this.isFull = false;
        return localBuffer;
    }

    /**
     * 判断是否为空
     *
     * @return
     */
    public boolean isEmpty() {
        return (len == 0);
    }

    /**
     * 判断是否满了
     *
     * @return
     */
    public boolean isFull() {
        return (len == maxSize);
    }

    /**
     * 获得队列的有效长度
     *
     * @return
     */
    public int size() {
        return len;
    }

    public void clear() {
        this.len = 0;
        this.end = -1;
        this.isFull = false;
    }
}