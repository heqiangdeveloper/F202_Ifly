package com.chinatsp.ifly.db.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class CommandInfo implements Parcelable {

    private  int moduleVersionId ;
    private  String moduleVersion ;
    private  String moduleName ;
    private  String moduleType ;
    private  String order ;
    private  String skillName ;
    private  String isdisplay ;
    private  String instructdesc ;
    private  int itemId ;
    private  String isrecommanded ;
    private  String instructContent ;
    private  String instructTeach ;
    private  String skillIconUrl ;

    public CommandInfo(){

    }

    protected CommandInfo(Parcel in) {
        moduleVersionId = in.readInt();
        moduleVersion = in.readString();
        moduleName = in.readString();
        moduleType = in.readString();
        order = in.readString();
        skillName = in.readString();
        isdisplay = in.readString();
        instructdesc = in.readString();
        itemId = in.readInt();
        isrecommanded = in.readString();
        instructContent = in.readString();
        instructTeach = in.readString();
        skillIconUrl = in.readString();
        iconPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(moduleVersionId);
        dest.writeString(moduleVersion);
        dest.writeString(moduleName);
        dest.writeString(moduleType);
        dest.writeString(order);
        dest.writeString(skillName);
        dest.writeString(isdisplay);
        dest.writeString(instructdesc);
        dest.writeInt(itemId);
        dest.writeString(isrecommanded);
        dest.writeString(instructContent);
        dest.writeString(instructTeach);
        dest.writeString(skillIconUrl);
        dest.writeString(iconPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommandInfo> CREATOR = new Creator<CommandInfo>() {
        @Override
        public CommandInfo createFromParcel(Parcel in) {
            return new CommandInfo(in);
        }

        @Override
        public CommandInfo[] newArray(int size) {
            return new CommandInfo[size];
        }
    };

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    private  String iconPath;

    public int getModuleVersionId() {
        return moduleVersionId;
    }

    public void setModuleVersionId(int moduleVersionId) {
        this.moduleVersionId = moduleVersionId;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getIsdisplay() {
        return isdisplay;
    }

    public void setIsdisplay(String isdisplay) {
        this.isdisplay = isdisplay;
    }

    public String getInstructdesc() {
        return instructdesc;
    }

    public void setInstructdesc(String instructdesc) {
        this.instructdesc = instructdesc;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getIsrecommanded() {
        return isrecommanded;
    }

    public void setIsrecommanded(String isrecommanded) {
        this.isrecommanded = isrecommanded;
    }

    public String getInstructContent() {
        return instructContent;
    }

    public void setInstructContent(String instructContent) {
        this.instructContent = instructContent;
    }

    public String getInstructTeach() {
        return instructTeach;
    }

    public void setInstructTeach(String instructTeach) {
        this.instructTeach = instructTeach;
    }

    public String getSkillIconUrl() {
        return skillIconUrl;
    }

    public void setSkillIconUrl(String skillIconUrl) {
        this.skillIconUrl = skillIconUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "TtsInfo{" +
                "moduleName='" + moduleName + '\'' +
                ", moduleType='" + moduleType + '\'' +
                ", skillName='" + skillName + '\'' +
                ", instructdesc='" + instructdesc + '\'' +
                ", isrecommanded='" + isrecommanded + '\'' +
                ", instructContent='" + instructContent + '\'' +
                ", instructTeach='" + instructTeach + '\'' +
                '}';
    }
}
