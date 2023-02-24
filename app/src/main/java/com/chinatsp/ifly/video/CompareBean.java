package com.chinatsp.ifly.video;

import android.support.annotation.NonNull;

public abstract class CompareBean implements Comparable<CompareBean> {

    public abstract String getTitle();

    public abstract boolean isDir();

    protected String letters = "#";

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    @Override
    public int compareTo(@NonNull CompareBean o) {
        // 文件夹优先显示并按顺序
        if (isDir()) {
            if (!o.isDir()) {
                return -1;
            }
        } else {
            if (o.isDir()) {
                return 1;
            }
        }
        return letters.compareTo(o.letters);
    }
}
