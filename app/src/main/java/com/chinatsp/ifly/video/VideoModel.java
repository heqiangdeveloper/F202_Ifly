package com.chinatsp.ifly.video;

import java.io.Serializable;

public class VideoModel extends CompareBean implements Serializable, MediaBean {
    private static final long serialVersionUID = 1L;
    private String path = "";
    private String parentPath = "";// 父级目录
    private boolean isDir = false;
    private String name = "";
    private long id = -1;
    private boolean selected = false;
    private long duration = 0;

    public String getPath() {
        return path;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public boolean isDir() {
        return isDir;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int getType() {
        return 2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public boolean getIsFileOrDirect() {
        return isDir;
    }

    public void setIsFileOrDirect(boolean isFileOrDirect) {
        this.isDir = isFileOrDirect;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
