package com.chinatsp.ifly.aidlbean;

import android.os.Parcel;
import android.os.Parcelable;

public class CmdVoiceModel implements Parcelable {
    public int id;
    public String text;
    public  int  hide;
    public String  response;

    public CmdVoiceModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.text);
        dest.writeInt(this.hide);
        dest.writeString(this.response);
    }

    protected CmdVoiceModel(Parcel in) {
        this.id = in.readInt();
        this.text = in.readString();
        this.response = in.readString();
    }

    public static final Creator<CmdVoiceModel> CREATOR = new Creator<CmdVoiceModel>() {
        @Override
        public CmdVoiceModel createFromParcel(Parcel source) {
            return new CmdVoiceModel(source);
        }

        @Override
        public CmdVoiceModel[] newArray(int size) {
            return new CmdVoiceModel[size];
        }
    };
}
