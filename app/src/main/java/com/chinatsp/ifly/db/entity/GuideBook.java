package com.chinatsp.ifly.db.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.chinatsp.ifly.db.CommonContract;

public class GuideBook {

    public static final String SCENE_UNACTIVE = "unactive";
    public static final String SCENE_NEWER = "newer";
    public static final String SCENE_PROFICIENT = "proficient";
    public static final String SCENE_ALL = "all";

    public static final String PRIORITY_NORMAL = "normal";
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_AIRCONDITION_OPEN = "aircondition"; //空调
    public static final String PRIORITY_BACKTRUNK_CONFIG = "back_trunk"; //配置后备箱
    public static final String PRIORITY_MUSIC_BG = "music_bg"; //音乐未打开时
    public static final String PRIORITY_MUSIC_FG = "music_fg"; //音乐播放中
    public static final String PRIORITY_NAVING = "navi"; //导航
    public static final String PRIORITY_RADIO_FG = "radio_fg"; //电台播放中

    private static final int ID_INDEX = 0;
    private static final int SCENE_INDEX = 1;
    private static final int GUIDE_TYPE_INDEX = 2;
    private static final int COMMAND_INDEX = 3;
    private static final int USAGE_COUNT_INDEX = 4;

    public long id;
    public String scene;
    public String priority;
    public String command;
    public long usageCount;

    public GuideBook(String scene, String priority, String command, long usageCount) {
        this.scene = scene;
        this.priority = priority;
        this.command = command;
        this.usageCount = usageCount;
    }

    public GuideBook(Cursor c) {
        id = c.getLong(ID_INDEX);
        scene = c.getString(SCENE_INDEX);
        priority = c.getString(GUIDE_TYPE_INDEX);
        command = c.getString(COMMAND_INDEX);
        usageCount = c.getLong(USAGE_COUNT_INDEX);
    }

    public static ContentValues createContentValues(GuideBook guide) {
        ContentValues values = new ContentValues();

        values.put(CommonContract.GUIDEBOOK_COLUMNS.SCENE, guide.scene);
        values.put(CommonContract.GUIDEBOOK_COLUMNS.PRIORITY, guide.priority);
        values.put(CommonContract.GUIDEBOOK_COLUMNS.COMMAND, guide.command);
        values.put(CommonContract.GUIDEBOOK_COLUMNS.USAGE_COUNT, guide.usageCount);

        return values;
    }

    @Override
    public String toString() {
        return "GuideBook{" +
                "id=" + id +
                ", scene='" + scene + '\'' +
                ", priority='" + priority + '\'' +
                ", command='" + command + '\'' +
                ", usageCount=" + usageCount +
                '}';
    }
}
