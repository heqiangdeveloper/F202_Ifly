package com.chinatsp.ifly.module.me.recommend.bean;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/6
 */

public class VideoDataBean {
    int id;
    String read;
    String time;
    String picPath;
    String videoPath;
    String videoName;
    String parent_path;

    public VideoDataBean(int id, String read, String time, String picPath, String videoPath, String videoName, String parent_path) {
        this.id = id;
        this.read = read;
        this.time = time;
        this.picPath = picPath;
        this.videoPath = videoPath;
        this.videoName = videoName;
        this.parent_path = parent_path;
    }

    public int getId() {
        return id;
    }

    public String getRead() {
        return read;
    }

    public String getTime() {
        return time;
    }

    public String getPicPath() {
        return picPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoName() {

        return videoName;
    }
    public String getParent_path() {
        return parent_path;
    }

    public void setParent_path(String parent_path) {
        this.parent_path = parent_path;
    }
}
