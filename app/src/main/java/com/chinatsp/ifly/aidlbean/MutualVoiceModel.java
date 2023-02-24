package com.chinatsp.ifly.aidlbean;

import android.os.Parcel;
import android.os.Parcelable;

public class MutualVoiceModel implements Parcelable {
    public String  service;
    public String  operation;
    public String  text;
    public String  response;;

    public MutualVoiceModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.service);
        dest.writeString(this.operation);
        dest.writeString(this.text);
        dest.writeString(this.response);
    }

    protected MutualVoiceModel(Parcel in) {
        this.service = in.readString();
        this.operation = in.readString();
        this.text = in.readString();
        this.response = in.readString();
    }

    public static final Creator<MutualVoiceModel> CREATOR = new Creator<MutualVoiceModel>() {
        @Override
        public MutualVoiceModel createFromParcel(Parcel source) {
            return new MutualVoiceModel(source);
        }

        @Override
        public MutualVoiceModel[] newArray(int size) {
            return new MutualVoiceModel[size];
        }
    };
}
