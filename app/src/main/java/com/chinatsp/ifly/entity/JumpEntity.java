package com.chinatsp.ifly.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class JumpEntity implements Parcelable {
    public String action;
    public String instructTeach;
    public String timeout;
    public String videoId;
    public String skillName;
    public String moduleName;

    public JumpEntity(){}

    protected JumpEntity(Parcel in) {
        action = in.readString();
        instructTeach = in.readString();
        timeout = in.readString();
        videoId = in.readString();
        skillName = in.readString();
        moduleName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
        dest.writeString(instructTeach);
        dest.writeString(timeout);
        dest.writeString(videoId);
        dest.writeString(skillName);
        dest.writeString(moduleName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JumpEntity> CREATOR = new Creator<JumpEntity>() {
        @Override
        public JumpEntity createFromParcel(Parcel in) {
            return new JumpEntity(in);
        }

        @Override
        public JumpEntity[] newArray(int size) {
            return new JumpEntity[size];
        }
    };

    @Override
    public String toString() {
        return "TtsEntity{" +
                "action='" + action + '\'' +
                ", instructTeach='" + instructTeach + '\'' +
                ", timeout='" + timeout + '\'' +
                ", skillName='" + skillName + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", videoId=" + videoId +
                '}';
    }
}
