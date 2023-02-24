package com.chinatsp.ifly.aidlbean;

import android.os.Parcel;
import android.os.Parcelable;

public class NlpVoiceModel implements Parcelable {
    public String  service;
    public String  operation;
    public String  semantic;
    public String  text;
    public String  dataEntity;
    public String  response;
    public NlpVoiceModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.service);
        dest.writeString(this.operation);
        dest.writeString(this.semantic);
        dest.writeString(this.text);
        dest.writeString(this.dataEntity);
        dest.writeString(this.response);
    }

    protected NlpVoiceModel(Parcel in) {
        this.service = in.readString();
        this.operation = in.readString();
        this.semantic = in.readString();
        this.text = in.readString();
        this.dataEntity = in.readString();
        this.response = in.readString();
    }

    public static final Creator<NlpVoiceModel> CREATOR = new Creator<NlpVoiceModel>() {
        @Override
        public NlpVoiceModel createFromParcel(Parcel source) {
            return new NlpVoiceModel(source);
        }

        @Override
        public NlpVoiceModel[] newArray(int size) {
            return new NlpVoiceModel[size];
        }
    };
}
