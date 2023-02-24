package com.chinatsp.ifly.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.chinatsp.ifly.db.entity.GuideBook;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

public class GuideBookDbDao {
    private static final String TAG = "GuideBookDbDao";
    private static volatile GuideBookDbDao mInstance;
    private CommonDbHelper mDbHelper;

    //未激活场景
    private static final String UNAC_DATA_1 = "('unactive', 'normal', '打开收音机', 0);";
    private static final String UNAC_DATA_2 = "('unactive', 'normal', '我想听FM93.8', 0);";
    private static final String UNAC_DATA_3 = "('unactive',  'normal', '打电话给张三', 0);";
    private static final String UNAC_DATA_4 = "('unactive',  'normal', '查看通讯录', 0);";
    private static final String UNAC_DATA_5 = "('unactive',  'normal', '打开空调', 0);";
    private static final String UNAC_DATA_6 = "('unactive',  'aircondition', '温度升高', 0);";
    private static final String UNAC_DATA_7 = "('unactive',  'aircondition', '风速降低', 0);";
    private static final String UNAC_DATA_8 = "('unactive',  'normal', '打开天窗', 0);";
    private static final String UNAC_DATA_9 = "('unactive',  'normal', '打开车窗', 0);";
//    private static final String UNAC_DATA_10 = "('unactive',  'normal', '打开氛围灯', 0);";
    private static final String UNAC_DATA_11 = "('unactive',  'normal', '氛围灯调节为红色', 0);";
    private static final String UNAC_DATA_12 = "('unactive',  'normal', '打开行车记录仪', 0);";
    private static final String UNAC_DATA_13 = "('unactive',  'normal', '打开雨刮', 0);";
    private static final String UNAC_DATA_14 = "('unactive',  'normal', '打开胎压监测', 0);";
    private static final String UNAC_DATA_15 = "('unactive',  'normal', '打开全景影像', 0);";
//    private static final String UNAC_DATA_16 = "('unactive',  'back_trunk', '打开后备箱', 0);";
    private static final String UNAC_DATA_17 = "('unactive',  'normal', '回首页', 0);";
    private static final String UNAC_DATA_18 = "('unactive',  'normal', '增大音量', 0);";
    private static final String UNAC_DATA_19 = "('unactive',  'normal', '静音', 0);";
    private static final String UNAC_DATA_20 = "('unactive',  'normal', '打开蓝牙', 0);";
    private static final String UNAC_DATA_21 = "('unactive',  'normal', '打开wifi', 0);";
    private static final String UNAC_DATA_22 = "('unactive',  'normal', '亮度升高', 0);";
//    private static final String UNAC_DATA_23 = "('unactive',  'normal', '打开按键查询', 0);";

    //新手场景
    private static final String NEWER_DATA_1 = "('newer', 'normal', '导航到东方明珠', 0);";
    private static final String NEWER_DATA_2 = "('newer', 'normal', '附近的加油站', 0);";
    private static final String NEWER_DATA_3 = "('newer', 'normal', '附近的停车场', 0);";
    private static final String NEWER_DATA_4 = "('newer', 'normal', '周边的美食', 0);";
    private static final String NEWER_DATA_5 = "('newer', 'normal', '我想吃火锅', 0);";
    private static final String NEWER_DATA_6 = "('newer', 'normal', '我想回家', 0);";
    private static final String NEWER_DATA_7 = "('newer', 'normal', '我要去公司', 0);";
//    private static final String NEWER_DATA_8 = "('newer', 'normal', '白天模式', 0);";
//    private static final String NEWER_DATA_9 = "('newer', 'normal', '黑夜模式', 0);";
    private static final String NEWER_DATA_10 = "('newer', 'music_bg', '打开音乐', 0);";
    private static final String NEWER_DATA_11 = "('newer', 'music_bg', '我想听歌', 0);";
    private static final String NEWER_DATA_12 = "('newer', 'normal', '最近好听的歌', 0);";
    private static final String NEWER_DATA_13 = "('newer', 'normal', '听刘德华的冰雨', 0);";
    private static final String NEWER_DATA_14 = "('newer', 'normal', '听王菲的歌', 0);";
    private static final String NEWER_DATA_15 = "('newer', 'normal', '我想听生僻字', 0);";
    private static final String NEWER_DATA_16 = "('newer', 'music_fg', '下一首', 0);";
    private static final String NEWER_DATA_17 = "('newer', 'music_fg', '暂停', 0);";
    private static final String NEWER_DATA_18 = "('newer', 'music_fg', '播放', 0);";
    private static final String NEWER_DATA_19 = "('newer', 'normal', '打开收音机', 0);";
    private static final String NEWER_DATA_20 = "('newer', 'normal', '我想听FM93.8', 0);";
    private static final String NEWER_DATA_21 = "('newer', 'normal', '打电话给张三', 0);";
    private static final String NEWER_DATA_22 = "('newer', 'normal', '查看通讯录', 0);";
    private static final String NEWER_DATA_23 = "('newer', 'normal', '打开空调', 0);";
    private static final String NEWER_DATA_24 = "('newer', 'aircondition', '温度升高', 0);";
    private static final String NEWER_DATA_25 = "('newer', 'aircondition', '太冷了', 0);";
    private static final String NEWER_DATA_26 = "('newer', 'aircondition', '风速降低', 0);";
    private static final String NEWER_DATA_27 = "('newer', 'normal', '打开天窗', 0);";
    private static final String NEWER_DATA_28 = "('newer', 'normal', '打开车窗', 0);";
    private static final String NEWER_DATA_29 = "('newer', 'normal', '回首页', 0);";
//    private static final String NEWER_DATA_30 = "('newer', 'normal', '打开按键查询', 0);";

    //熟手场景
    private static final String PROFICIENT_DATA_1 = "('proficient', 'normal', '导航到东方明珠', 0);";
    private static final String PROFICIENT_DATA_2 = "('proficient', 'normal', '附近的加油站', 0);";
    private static final String PROFICIENT_DATA_3 = "('proficient', 'normal', '附近的停车场', 0);";
    private static final String PROFICIENT_DATA_4 = "('proficient', 'normal', '周边的美食', 0);";
    private static final String PROFICIENT_DATA_5 = "('proficient', 'normal', '我想吃火锅', 0);";
    private static final String PROFICIENT_DATA_6 = "('proficient', 'normal', '我想回家', 0);";
    private static final String PROFICIENT_DATA_7 = "('proficient', 'normal', '我要去公司', 0);";
//    private static final String PROFICIENT_DATA_8 = "('proficient', 'normal', '白天模式', 0);";
//    private static final String PROFICIENT_DATA_9 = "('proficient', 'normal', '黑夜模式', 0);";
    private static final String PROFICIENT_DATA_10 = "('proficient', 'normal', '查询我的位置', 0);";
    private static final String PROFICIENT_DATA_11 = "('proficient', 'normal', '2D模式', 0);";
    private static final String PROFICIENT_DATA_12 = "('proficient', 'normal', '3D模式', 0);";
    private static final String PROFICIENT_DATA_13 = "('proficient', 'navi', '高速优先', 0);";
    private static final String PROFICIENT_DATA_14 = "('proficient', 'navi', '不走高速', 0);";
    private static final String PROFICIENT_DATA_15 = "('proficient', 'navi', '避免收费', 0);";
    private static final String PROFICIENT_DATA_16 = "('proficient', 'navi', '避免拥堵', 0);";
    private static final String PROFICIENT_DATA_17 = "('proficient', 'navi', '还有多久到', 0);";
    private static final String PROFICIENT_DATA_18 = "('proficient', 'music_bg', '打开音乐', 0);";
    private static final String PROFICIENT_DATA_19 = "('proficient', 'music_bg', '我想听歌', 0);";
    private static final String PROFICIENT_DATA_20 = "('proficient', 'normal', '最近好听的歌', 0);";
    private static final String PROFICIENT_DATA_21 = "('proficient', 'normal', '听刘德华的冰雨', 0);";
    private static final String PROFICIENT_DATA_22 = "('proficient', 'normal', '听王菲的歌', 0);";
    private static final String PROFICIENT_DATA_23 = "('proficient', 'normal', '我想听生僻字', 0);";
    private static final String PROFICIENT_DATA_24 = "('proficient', 'music_fg', '下一首', 0);";
    private static final String PROFICIENT_DATA_25 = "('proficient', 'music_fg', '暂停', 0);";
    private static final String PROFICIENT_DATA_26 = "('proficient', 'music_fg', '播放', 0);";
    private static final String PROFICIENT_DATA_27 = "('proficient', 'normal', '我想听伤感的歌', 0);";
    private static final String PROFICIENT_DATA_28 = "('proficient', 'normal', '我想听情歌', 0);";
    private static final String PROFICIENT_DATA_29 = "('proficient', 'music_fg', '收藏这首歌', 0);";
    private static final String PROFICIENT_DATA_30 = "('proficient', 'music_fg', '播放收藏音乐', 0);";
    private static final String PROFICIENT_DATA_31 = "('proficient', 'music_fg', '单曲循环', 0);";
    private static final String PROFICIENT_DATA_32 = "('proficient', 'music_fg', '列表循环', 0);";
    private static final String PROFICIENT_DATA_33 = "('proficient', 'music_fg', '顺序播放', 0);";
    private static final String PROFICIENT_DATA_34 = "('proficient', 'music_fg', '随机播放', 0);";
    private static final String PROFICIENT_DATA_35 = "('proficient', 'normal', '打开收音机', 0);";
    private static final String PROFICIENT_DATA_36 = "('proficient', 'normal', '我想听FM93.8', 0);";
    private static final String PROFICIENT_DATA_37 = "('proficient', 'normal', '打开在线电台', 0);";
    private static final String PROFICIENT_DATA_38 = "('proficient', 'normal', '我想听郭德纲相声', 0);";
//    private static final String PROFICIENT_DATA_39 = "('proficient', 'normal', '播放收藏节目', 0);";
    private static final String PROFICIENT_DATA_40 = "('proficient', 'normal', '讲个笑话', 0);";
    private static final String PROFICIENT_DATA_41 = "('proficient', 'normal', '听童话故事', 0);";
    private static final String PROFICIENT_DATA_42 = "('proficient', 'radio_fg', '收藏这个节目', 0);";
    private static final String PROFICIENT_DATA_43 = "('proficient', 'normal', '打电话给张三', 0);";
    private static final String PROFICIENT_DATA_44 = "('proficient', 'normal', '查看通讯录', 0);";
    private static final String PROFICIENT_DATA_45 = "('proficient', 'normal', '重拨', 0);";
    private static final String PROFICIENT_DATA_46 = "('proficient', 'normal', '回拨', 0);";
    private static final String PROFICIENT_DATA_47 = "('proficient', 'normal', '打开空调', 0);";
    private static final String PROFICIENT_DATA_48 = "('proficient', 'aircondition', '温度升高', 0);";
    private static final String PROFICIENT_DATA_49 = "('proficient', 'aircondition', '太冷了', 0);";
    private static final String PROFICIENT_DATA_50 = "('proficient', 'aircondition', '风速降低', 0);";
    private static final String PROFICIENT_DATA_51 = "('proficient', 'normal', '打开天窗', 0);";
    private static final String PROFICIENT_DATA_52 = "('proficient', 'normal', '打开车窗', 0);";
    private static final String PROFICIENT_DATA_53 = "('proficient', 'aircondition', '温度调到30°', 0);";
    private static final String PROFICIENT_DATA_54 = "('proficient', 'aircondition', '温度最高', 0);";
    private static final String PROFICIENT_DATA_55 = "('proficient', 'normal', '内循环', 0);";
    private static final String PROFICIENT_DATA_56 = "('proficient', 'normal', '外循环', 0);";
    private static final String PROFICIENT_DATA_57 = "('proficient', 'normal', '打开前除霜', 0);";
    private static final String PROFICIENT_DATA_58 = "('proficient', 'normal', '打开天窗通风', 0);";
    private static final String PROFICIENT_DATA_59 = "('proficient', 'normal', '天窗开二分之一', 0);";
    private static final String PROFICIENT_DATA_60 = "('proficient', 'normal', '打开遮阳帘', 0);";
//    private static final String PROFICIENT_DATA_61 = "('proficient', 'normal', '打开氛围灯', 0);";
    private static final String PROFICIENT_DATA_62 = "('proficient', 'normal', '氛围灯调节为红色', 0);";
    private static final String PROFICIENT_DATA_63 = "('proficient', 'normal', '打开行车记录仪', 0);";
    private static final String PROFICIENT_DATA_64 = "('proficient', 'normal', '打开雨刮', 0);";
    private static final String PROFICIENT_DATA_65 = "('proficient', 'normal', '回首页', 0);";
    private static final String PROFICIENT_DATA_66 = "('proficient', 'normal', '增大音量', 0);";
    private static final String PROFICIENT_DATA_67 = "('proficient', 'normal', '静音', 0);";
    private static final String PROFICIENT_DATA_68 = "('proficient', 'normal', '打开蓝牙', 0);";
    private static final String PROFICIENT_DATA_69 = "('proficient', 'normal', '打开wifi', 0);";
//    private static final String PROFICIENT_DATA_70 = "('proficient', 'normal', '打开按键查询', 0);";
//    private static final String PROFICIENT_DATA_71 = "('proficient', 'normal', '定速巡航怎么用', 0);";
    private static final String PROFICIENT_DATA_72 = "('proficient', 'normal', '杭州的天气怎么样', 0);";
    private static final String PROFICIENT_DATA_73 = "('proficient', 'normal', '明天下雨吗', 0);";
//    private static final String PROFICIENT_DATA_74 = "('proficient', 'normal', '我要吐槽', 0);";

    //全部场景
    private static final String ALL_DATA_1 = "('all', 'normal', '导航到东方明珠', 0);";
    private static final String ALL_DATA_2 = "('all', 'normal', '附近的加油站', 0);";
    private static final String ALL_DATA_3 = "('all', 'normal', '附近的停车场', 0);";
    private static final String ALL_DATA_4 = "('all', 'normal', '周边的美食', 0);";
    private static final String ALL_DATA_5 = "('all', 'normal', '我想吃火锅', 0);";
    private static final String ALL_DATA_6 = "('all', 'normal', '我想回家', 0);";
    private static final String ALL_DATA_7 = "('all', 'normal', '我要去公司', 0);";
//    private static final String ALL_DATA_8 = "('all', 'normal', '白天模式', 0);";
//    private static final String ALL_DATA_9 = "('all', 'normal', '黑夜模式', 0);";
    private static final String ALL_DATA_10 = "('all', 'normal', '查询我的位置', 0);";
    private static final String ALL_DATA_11 = "('all', 'normal', '2D模式', 0);";
    private static final String ALL_DATA_12 = "('all', 'normal', '3D模式', 0);";
    private static final String ALL_DATA_13 = "('all', 'navi', '高速优先', 0);";
    private static final String ALL_DATA_14 = "('all', 'navi', '不走高速', 0);";
    private static final String ALL_DATA_15 = "('all', 'navi', '避免收费', 0);";
    private static final String ALL_DATA_16 = "('all', 'navi', '避免拥堵', 0);";
    private static final String ALL_DATA_17 = "('all', 'navi', '还有多久到', 0);";
    private static final String ALL_DATA_18 = "('all', 'navi', '放大地图', 0);";
    private static final String ALL_DATA_19 = "('all', 'navi', '缩小地图', 0);";
    private static final String ALL_DATA_20 = "('all', 'navi', '关闭导航音量', 0);";
    private static final String ALL_DATA_21 = "('all', 'navi', '打开实时路况', 0);";
    private static final String ALL_DATA_22 = "('all', 'music_bg', '打开音乐', 0);";
    private static final String ALL_DATA_23 = "('all', 'music_bg', '我想听歌', 0);";
    private static final String ALL_DATA_24 = "('all', 'normal', '我想听歌', 0);";
    private static final String ALL_DATA_25 = "('all', 'normal', '播放收藏音乐', 0);";
    private static final String ALL_DATA_26 = "('all', 'normal', '听刘德华的冰雨', 0);";
    private static final String ALL_DATA_27 = "('all', 'normal', '听王菲的歌', 0);";
    private static final String ALL_DATA_28 = "('all', 'normal', '我想听生僻字', 0);";
    private static final String ALL_DATA_29 = "('all', 'music_fg', '下一首', 0);";
    private static final String ALL_DATA_30 = "('all', 'music_fg', '暂停', 0);";
    private static final String ALL_DATA_31 = "('all', 'music_fg', '播放', 0);";
    private static final String ALL_DATA_32 = "('all', 'normal', '我想听伤感的歌', 0);";
    private static final String ALL_DATA_33 = "('all', 'music_fg', '收藏这首歌', 0);";
    private static final String ALL_DATA_34 = "('all', 'music_fg', '单曲循环', 0);";
    private static final String ALL_DATA_35 = "('all', 'music_fg', '列表循环', 0);";
    private static final String ALL_DATA_36 = "('all', 'music_fg', '顺序播放', 0);";
    private static final String ALL_DATA_37 = "('all', 'music_fg', '随机播放', 0);";
    private static final String ALL_DATA_38 = "('all', 'music_fg', '打开听歌识曲', 0);";
    private static final String ALL_DATA_39 = "('all', 'music_fg', '显示歌词', 0);";
    private static final String ALL_DATA_40 = "('all', 'music_fg', '隐藏歌词', 0);";
    private static final String ALL_DATA_41 = "('all', 'music_fg', '查看歌曲列表', 0);";
    private static final String ALL_DATA_42 = "('all', 'music_fg', '关闭歌曲列表', 0);";
    private static final String ALL_DATA_43 = "('all', 'normal', '打开收音机', 0);";
    private static final String ALL_DATA_44 = "('all', 'normal', '我想听FM93.8', 0);";
    private static final String ALL_DATA_45 = "('all', 'normal', '打开在线电台', 0);";
    private static final String ALL_DATA_46 = "('all', 'normal', '我想听郭德纲相声', 0);";
    private static final String ALL_DATA_47 = "('all', 'normal', '播放收藏节目', 0);";
    private static final String ALL_DATA_48 = "('all', 'normal', '讲个笑话', 0);";
    private static final String ALL_DATA_49 = "('all', 'normal', '我要听新闻', 0);";
    private static final String ALL_DATA_50 = "('all', 'normal', '听童话故事', 0);";
    private static final String ALL_DATA_51 = "('all', 'radio_fg', '收藏这个节目', 0);";
    private static final String ALL_DATA_52 = "('all', 'normal', '我想听书', 0);";
    private static final String ALL_DATA_53 = "('all', 'normal', '打电话给张三', 0);";
    private static final String ALL_DATA_54 = "('all', 'normal', '查看通讯录', 0);";
    private static final String ALL_DATA_55 = "('all', 'normal', '重拨', 0);";
    private static final String ALL_DATA_56 = "('all', 'normal', '回拨', 0);";
    private static final String ALL_DATA_57 = "('all', 'normal', '打开空调', 0);";
    private static final String ALL_DATA_58 = "('all', 'aircondition', '温度升高', 0);";
    private static final String ALL_DATA_59 = "('all', 'aircondition', '太冷了', 0);";
    private static final String ALL_DATA_60 = "('all', 'aircondition', '风速降低', 0);";
    private static final String ALL_DATA_61 = "('all', 'normal', '打开天窗', 0);";
    private static final String ALL_DATA_62 = "('all', 'normal', '打开车窗', 0);";
    private static final String ALL_DATA_63 = "('all', 'aircondition', '温度调到30°', 0);";
    private static final String ALL_DATA_64 = "('all', 'aircondition', '温度最高', 0);";
    private static final String ALL_DATA_65 = "('all', 'normal', '内循环', 0);";
    private static final String ALL_DATA_66 = "('all', 'normal', '外循环', 0);";
    private static final String ALL_DATA_67 = "('all', 'normal', '打开前除霜', 0);";
    private static final String ALL_DATA_68 = "('all', 'normal', '打开天窗通风', 0);";
    private static final String ALL_DATA_69 = "('all', 'normal', '天窗开二分之一', 0);";
    private static final String ALL_DATA_70 = "('all', 'normal', '打开遮阳帘', 0);";
//    private static final String ALL_DATA_71 = "('all', 'normal', '打开氛围灯', 0);";
    private static final String ALL_DATA_72 = "('all', 'normal', '氛围灯调节为红色', 0);";
    private static final String ALL_DATA_73 = "('all', 'normal', '打开行车记录仪', 0);";
    private static final String ALL_DATA_74 = "('all', 'normal', '打开雨刮', 0);";
    private static final String ALL_DATA_75 = "('all', 'aircondition', '制冷模式', 0);";
    private static final String ALL_DATA_76 = "('all', 'aircondition', '制热模式', 0);";
    private static final String ALL_DATA_77 = "('all', 'normal', '温度最高', 0);";
    private static final String ALL_DATA_78 = "('all', 'normal', '风速最低', 0);";
    private static final String ALL_DATA_79 = "('all', 'normal', '空调吹面', 0);";
    private static final String ALL_DATA_80 = "('all', 'normal', '空调吹脚', 0);";
    private static final String ALL_DATA_81 = "('all', 'normal', '吹面吹脚', 0);";
    private static final String ALL_DATA_82 = "('all', 'normal', '吹脚除霜', 0);";
    private static final String ALL_DATA_83 = "('all', 'normal', '打开后除霜', 0);";
//    private static final String ALL_DATA_84 = "('all', 'normal', '增大氛围灯亮度', 0);";
//    private static final String ALL_DATA_85 = "('all', 'normal', '打开氛围灯律动', 0);";
    private static final String ALL_DATA_86 = "('all', 'normal', '打开车窗透气', 0);";
//    private static final String ALL_DATA_87 = "('all', 'back_trunk', '打开后备箱', 0);";
    private static final String ALL_DATA_88 = "('all', 'normal', '我要拍照', 0);";
    private static final String ALL_DATA_89 = "('all', 'normal', '打开胎压监测', 0);";
    private static final String ALL_DATA_90 = "('all', 'normal', '打开全景影像', 0);";
    private static final String ALL_DATA_91 = "('all', 'normal', '回首页', 0);";
//    private static final String ALL_DATA_92 = "('all', 'normal', '打开按键查询', 0);";
    private static final String ALL_DATA_93 = "('all', 'normal', '增大音量', 0);";
    private static final String ALL_DATA_94 = "('all', 'normal', '静音', 0);";
    private static final String ALL_DATA_95 = "('all', 'normal', '打开蓝牙', 0);";
    private static final String ALL_DATA_96 = "('all', 'normal', '打开wifi', 0);";
    private static final String ALL_DATA_97 = "('all', 'normal', '亮度升高', 0);";
    private static final String ALL_DATA_98 = "('all', 'normal', '亮度降低', 0);";
    private static final String ALL_DATA_99 = "('all', 'normal', '增大媒体音量', 0);";
    private static final String ALL_DATA_100 = "('all', 'normal', '降低导航音量', 0);";
//    private static final String ALL_DATA_101 = "('all', 'normal', '打开车信', 0);";
//    private static final String ALL_DATA_102 = "('all', 'normal', '给张三发消息', 0);";
//    private static final String ALL_DATA_103 = "('all', 'normal', '给张三发红包', 0);";
//    private static final String ALL_DATA_104 = "('all', 'normal', '将我的位置发给张三', 0);";
//    private static final String ALL_DATA_105 = "('all', 'normal', '播放未读消息', 0);";
    private static final String ALL_DATA_106 = "('all', 'normal', '杭州的天气怎么样', 0);";
    private static final String ALL_DATA_107 = "('all', 'normal', '明天下雨吗', 0);";
    private static final String ALL_DATA_108 = "('all', 'normal', '查一下北京的火车票', 0);";
    private static final String ALL_DATA_109 = "('all', 'normal', '查一下重庆到上海的航班', 0);";
    private static final String ALL_DATA_110 = "('all', 'normal', '长安汽车的股价', 0);";
//    private static final String ALL_DATA_111 = "('all', 'normal', '现在几点啦', 0);";
//    private static final String ALL_DATA_112 = "('all', 'normal', '今天是几号', 0);";
//    private static final String ALL_DATA_113 = "('all', 'normal', '按键查询', 0);";
//    private static final String ALL_DATA_114 = "('all', 'normal', '行车记录仪怎么用', 0);";
//    private static final String ALL_DATA_115 = "('all', 'normal', '定速巡航怎么用', 0);";
//    private static final String ALL_DATA_116 = "('all', 'normal', '全景影像怎么用', 0);";
//    private static final String ALL_DATA_117 = "('all', 'normal', '我要吐槽', 0);";

    public static GuideBookDbDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GuideBookDbDao.class) {
                if (mInstance == null) {
                    mInstance = new GuideBookDbDao(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    private GuideBookDbDao(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new CommonDbHelper(context);
        }
    }

    public void init() {
        if (!hasPresetData()) {
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    preSetData2db();
                }
            });
        } else {
            LogUtils.d(TAG, "guide_book table has preset guidebook data");
        }
    }

    private void preSetData2db() {
        String cs = ", "; //comma and space
        String insertMe = "INSERT INTO " + CommonContract.GUIDE_BOOK_TABLE_NAME + " (" +
                CommonContract.GUIDEBOOK_COLUMNS.SCENE + cs +
                CommonContract.GUIDEBOOK_COLUMNS.PRIORITY + cs +
                CommonContract.GUIDEBOOK_COLUMNS.COMMAND + cs +
                CommonContract.GUIDEBOOK_COLUMNS.USAGE_COUNT +
                ") VALUES ";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(insertMe + UNAC_DATA_1);
        db.execSQL(insertMe + UNAC_DATA_2);
        db.execSQL(insertMe + UNAC_DATA_3);
        db.execSQL(insertMe + UNAC_DATA_4);
        db.execSQL(insertMe + UNAC_DATA_5);
        db.execSQL(insertMe + UNAC_DATA_6);
        db.execSQL(insertMe + UNAC_DATA_7);
        db.execSQL(insertMe + UNAC_DATA_8);
        db.execSQL(insertMe + UNAC_DATA_9);
//        db.execSQL(insertMe + UNAC_DATA_10);
        db.execSQL(insertMe + UNAC_DATA_11);
        db.execSQL(insertMe + UNAC_DATA_12);
        db.execSQL(insertMe + UNAC_DATA_13);
        db.execSQL(insertMe + UNAC_DATA_14);
        db.execSQL(insertMe + UNAC_DATA_15);
//        db.execSQL(insertMe + UNAC_DATA_16);
        db.execSQL(insertMe + UNAC_DATA_17);
        db.execSQL(insertMe + UNAC_DATA_18);
        db.execSQL(insertMe + UNAC_DATA_19);
        db.execSQL(insertMe + UNAC_DATA_20);
        db.execSQL(insertMe + UNAC_DATA_21);
        db.execSQL(insertMe + UNAC_DATA_22);
//        db.execSQL(insertMe + UNAC_DATA_23);

        db.execSQL(insertMe + NEWER_DATA_1);
        db.execSQL(insertMe + NEWER_DATA_2);
        db.execSQL(insertMe + NEWER_DATA_3);
        db.execSQL(insertMe + NEWER_DATA_4);
        db.execSQL(insertMe + NEWER_DATA_5);
        db.execSQL(insertMe + NEWER_DATA_6);
        db.execSQL(insertMe + NEWER_DATA_7);
//        db.execSQL(insertMe + NEWER_DATA_8);
//        db.execSQL(insertMe + NEWER_DATA_9);
        db.execSQL(insertMe + NEWER_DATA_10);
        db.execSQL(insertMe + NEWER_DATA_11);
        db.execSQL(insertMe + NEWER_DATA_12);
        db.execSQL(insertMe + NEWER_DATA_13);
        db.execSQL(insertMe + NEWER_DATA_14);
        db.execSQL(insertMe + NEWER_DATA_15);
        db.execSQL(insertMe + NEWER_DATA_16);
        db.execSQL(insertMe + NEWER_DATA_17);
        db.execSQL(insertMe + NEWER_DATA_18);
        db.execSQL(insertMe + NEWER_DATA_19);
        db.execSQL(insertMe + NEWER_DATA_20);
        db.execSQL(insertMe + NEWER_DATA_21);
        db.execSQL(insertMe + NEWER_DATA_22);
        db.execSQL(insertMe + NEWER_DATA_23);
        db.execSQL(insertMe + NEWER_DATA_24);
        db.execSQL(insertMe + NEWER_DATA_25);
        db.execSQL(insertMe + NEWER_DATA_26);
        db.execSQL(insertMe + NEWER_DATA_27);
        db.execSQL(insertMe + NEWER_DATA_28);
        db.execSQL(insertMe + NEWER_DATA_29);
//        db.execSQL(insertMe + NEWER_DATA_30);

        db.execSQL(insertMe + PROFICIENT_DATA_1);
        db.execSQL(insertMe + PROFICIENT_DATA_2);
        db.execSQL(insertMe + PROFICIENT_DATA_3);
        db.execSQL(insertMe + PROFICIENT_DATA_4);
        db.execSQL(insertMe + PROFICIENT_DATA_5);
        db.execSQL(insertMe + PROFICIENT_DATA_6);
        db.execSQL(insertMe + PROFICIENT_DATA_7);
//        db.execSQL(insertMe + PROFICIENT_DATA_8);
//        db.execSQL(insertMe + PROFICIENT_DATA_9);
        db.execSQL(insertMe + PROFICIENT_DATA_10);
        db.execSQL(insertMe + PROFICIENT_DATA_11);
        db.execSQL(insertMe + PROFICIENT_DATA_12);
        db.execSQL(insertMe + PROFICIENT_DATA_13);
        db.execSQL(insertMe + PROFICIENT_DATA_14);
        db.execSQL(insertMe + PROFICIENT_DATA_15);
        db.execSQL(insertMe + PROFICIENT_DATA_16);
        db.execSQL(insertMe + PROFICIENT_DATA_17);
        db.execSQL(insertMe + PROFICIENT_DATA_18);
        db.execSQL(insertMe + PROFICIENT_DATA_19);
        db.execSQL(insertMe + PROFICIENT_DATA_20);
        db.execSQL(insertMe + PROFICIENT_DATA_21);
        db.execSQL(insertMe + PROFICIENT_DATA_22);
        db.execSQL(insertMe + PROFICIENT_DATA_23);
        db.execSQL(insertMe + PROFICIENT_DATA_24);
        db.execSQL(insertMe + PROFICIENT_DATA_25);
        db.execSQL(insertMe + PROFICIENT_DATA_26);
        db.execSQL(insertMe + PROFICIENT_DATA_27);
        db.execSQL(insertMe + PROFICIENT_DATA_28);
        db.execSQL(insertMe + PROFICIENT_DATA_29);
        db.execSQL(insertMe + PROFICIENT_DATA_30);
        db.execSQL(insertMe + PROFICIENT_DATA_31);
        db.execSQL(insertMe + PROFICIENT_DATA_32);
        db.execSQL(insertMe + PROFICIENT_DATA_33);
        db.execSQL(insertMe + PROFICIENT_DATA_34);
        db.execSQL(insertMe + PROFICIENT_DATA_35);
        db.execSQL(insertMe + PROFICIENT_DATA_36);
        db.execSQL(insertMe + PROFICIENT_DATA_37);
        db.execSQL(insertMe + PROFICIENT_DATA_38);
//        db.execSQL(insertMe + PROFICIENT_DATA_39);
        db.execSQL(insertMe + PROFICIENT_DATA_40);
        db.execSQL(insertMe + PROFICIENT_DATA_41);
        db.execSQL(insertMe + PROFICIENT_DATA_42);
        db.execSQL(insertMe + PROFICIENT_DATA_43);
        db.execSQL(insertMe + PROFICIENT_DATA_44);
        db.execSQL(insertMe + PROFICIENT_DATA_45);
        db.execSQL(insertMe + PROFICIENT_DATA_46);
        db.execSQL(insertMe + PROFICIENT_DATA_47);
        db.execSQL(insertMe + PROFICIENT_DATA_48);
        db.execSQL(insertMe + PROFICIENT_DATA_49);
        db.execSQL(insertMe + PROFICIENT_DATA_50);
        db.execSQL(insertMe + PROFICIENT_DATA_51);
        db.execSQL(insertMe + PROFICIENT_DATA_52);
        db.execSQL(insertMe + PROFICIENT_DATA_53);
        db.execSQL(insertMe + PROFICIENT_DATA_54);
        db.execSQL(insertMe + PROFICIENT_DATA_55);
        db.execSQL(insertMe + PROFICIENT_DATA_56);
        db.execSQL(insertMe + PROFICIENT_DATA_57);
        db.execSQL(insertMe + PROFICIENT_DATA_58);
        db.execSQL(insertMe + PROFICIENT_DATA_59);
        db.execSQL(insertMe + PROFICIENT_DATA_60);
//        db.execSQL(insertMe + PROFICIENT_DATA_61);
        db.execSQL(insertMe + PROFICIENT_DATA_62);
        db.execSQL(insertMe + PROFICIENT_DATA_63);
        db.execSQL(insertMe + PROFICIENT_DATA_64);
        db.execSQL(insertMe + PROFICIENT_DATA_65);
        db.execSQL(insertMe + PROFICIENT_DATA_66);
        db.execSQL(insertMe + PROFICIENT_DATA_67);
        db.execSQL(insertMe + PROFICIENT_DATA_68);
        db.execSQL(insertMe + PROFICIENT_DATA_69);
//        db.execSQL(insertMe + PROFICIENT_DATA_70);
//        db.execSQL(insertMe + PROFICIENT_DATA_71);
        db.execSQL(insertMe + PROFICIENT_DATA_72);
        db.execSQL(insertMe + PROFICIENT_DATA_73);
//        db.execSQL(insertMe + PROFICIENT_DATA_74);

        db.execSQL(insertMe + ALL_DATA_1);
        db.execSQL(insertMe + ALL_DATA_2);
        db.execSQL(insertMe + ALL_DATA_3);
        db.execSQL(insertMe + ALL_DATA_4);
        db.execSQL(insertMe + ALL_DATA_5);
        db.execSQL(insertMe + ALL_DATA_6);
        db.execSQL(insertMe + ALL_DATA_7);
//        db.execSQL(insertMe + ALL_DATA_8);
//        db.execSQL(insertMe + ALL_DATA_9);
        db.execSQL(insertMe + ALL_DATA_10);
        db.execSQL(insertMe + ALL_DATA_11);
        db.execSQL(insertMe + ALL_DATA_12);
        db.execSQL(insertMe + ALL_DATA_13);
        db.execSQL(insertMe + ALL_DATA_14);
        db.execSQL(insertMe + ALL_DATA_15);
        db.execSQL(insertMe + ALL_DATA_16);
        db.execSQL(insertMe + ALL_DATA_17);
        db.execSQL(insertMe + ALL_DATA_18);
        db.execSQL(insertMe + ALL_DATA_19);
        db.execSQL(insertMe + ALL_DATA_20);
        db.execSQL(insertMe + ALL_DATA_21);
        db.execSQL(insertMe + ALL_DATA_22);
        db.execSQL(insertMe + ALL_DATA_23);
        db.execSQL(insertMe + ALL_DATA_24);
        db.execSQL(insertMe + ALL_DATA_25);
        db.execSQL(insertMe + ALL_DATA_26);
        db.execSQL(insertMe + ALL_DATA_27);
        db.execSQL(insertMe + ALL_DATA_28);
        db.execSQL(insertMe + ALL_DATA_29);
        db.execSQL(insertMe + ALL_DATA_30);
        db.execSQL(insertMe + ALL_DATA_31);
        db.execSQL(insertMe + ALL_DATA_32);
        db.execSQL(insertMe + ALL_DATA_33);
        db.execSQL(insertMe + ALL_DATA_34);
        db.execSQL(insertMe + ALL_DATA_35);
        db.execSQL(insertMe + ALL_DATA_36);
        db.execSQL(insertMe + ALL_DATA_37);
        db.execSQL(insertMe + ALL_DATA_38);
        db.execSQL(insertMe + ALL_DATA_39);
        db.execSQL(insertMe + ALL_DATA_40);
        db.execSQL(insertMe + ALL_DATA_41);
        db.execSQL(insertMe + ALL_DATA_42);
        db.execSQL(insertMe + ALL_DATA_43);
        db.execSQL(insertMe + ALL_DATA_44);
        db.execSQL(insertMe + ALL_DATA_45);
        db.execSQL(insertMe + ALL_DATA_46);
        db.execSQL(insertMe + ALL_DATA_47);
        db.execSQL(insertMe + ALL_DATA_48);
        db.execSQL(insertMe + ALL_DATA_49);
        db.execSQL(insertMe + ALL_DATA_50);
        db.execSQL(insertMe + ALL_DATA_51);
        db.execSQL(insertMe + ALL_DATA_52);
        db.execSQL(insertMe + ALL_DATA_53);
        db.execSQL(insertMe + ALL_DATA_54);
        db.execSQL(insertMe + ALL_DATA_55);
        db.execSQL(insertMe + ALL_DATA_56);
        db.execSQL(insertMe + ALL_DATA_57);
        db.execSQL(insertMe + ALL_DATA_58);
        db.execSQL(insertMe + ALL_DATA_59);
        db.execSQL(insertMe + ALL_DATA_60);
        db.execSQL(insertMe + ALL_DATA_61);
        db.execSQL(insertMe + ALL_DATA_62);
        db.execSQL(insertMe + ALL_DATA_63);
        db.execSQL(insertMe + ALL_DATA_64);
        db.execSQL(insertMe + ALL_DATA_65);
        db.execSQL(insertMe + ALL_DATA_66);
        db.execSQL(insertMe + ALL_DATA_67);
        db.execSQL(insertMe + ALL_DATA_68);
        db.execSQL(insertMe + ALL_DATA_69);
        db.execSQL(insertMe + ALL_DATA_70);
//        db.execSQL(insertMe + ALL_DATA_71);
        db.execSQL(insertMe + ALL_DATA_72);
        db.execSQL(insertMe + ALL_DATA_73);
        db.execSQL(insertMe + ALL_DATA_74);
        db.execSQL(insertMe + ALL_DATA_75);
        db.execSQL(insertMe + ALL_DATA_76);
        db.execSQL(insertMe + ALL_DATA_77);
        db.execSQL(insertMe + ALL_DATA_78);
        db.execSQL(insertMe + ALL_DATA_79);
        db.execSQL(insertMe + ALL_DATA_80);
        db.execSQL(insertMe + ALL_DATA_81);
        db.execSQL(insertMe + ALL_DATA_82);
        db.execSQL(insertMe + ALL_DATA_83);
//        db.execSQL(insertMe + ALL_DATA_84);
//        db.execSQL(insertMe + ALL_DATA_85);
        db.execSQL(insertMe + ALL_DATA_86);
//        db.execSQL(insertMe + ALL_DATA_87);
        db.execSQL(insertMe + ALL_DATA_88);
        db.execSQL(insertMe + ALL_DATA_89);
        db.execSQL(insertMe + ALL_DATA_90);
        db.execSQL(insertMe + ALL_DATA_91);
//        db.execSQL(insertMe + ALL_DATA_92);
        db.execSQL(insertMe + ALL_DATA_93);
        db.execSQL(insertMe + ALL_DATA_94);
        db.execSQL(insertMe + ALL_DATA_95);
        db.execSQL(insertMe + ALL_DATA_96);
        db.execSQL(insertMe + ALL_DATA_97);
        db.execSQL(insertMe + ALL_DATA_98);
        db.execSQL(insertMe + ALL_DATA_99);
        db.execSQL(insertMe + ALL_DATA_100);
//        db.execSQL(insertMe + ALL_DATA_101);
//        db.execSQL(insertMe + ALL_DATA_102);
//        db.execSQL(insertMe + ALL_DATA_103);
//        db.execSQL(insertMe + ALL_DATA_104);
//        db.execSQL(insertMe + ALL_DATA_105);
        db.execSQL(insertMe + ALL_DATA_106);
        db.execSQL(insertMe + ALL_DATA_107);
        db.execSQL(insertMe + ALL_DATA_108);
        db.execSQL(insertMe + ALL_DATA_109);
        db.execSQL(insertMe + ALL_DATA_110);
//        db.execSQL(insertMe + ALL_DATA_111);
//        db.execSQL(insertMe + ALL_DATA_112);
//        db.execSQL(insertMe + ALL_DATA_113);
//        db.execSQL(insertMe + ALL_DATA_114);
//        db.execSQL(insertMe + ALL_DATA_115);
//        db.execSQL(insertMe + ALL_DATA_116);
//        db.execSQL(insertMe + ALL_DATA_117);
    }

    private boolean hasPresetData() {
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommonContract.GUIDE_BOOK_TABLE_NAME, null,
                    null, null, null, null, null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                ret = true;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "isPackageExistInDB Exception: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ret;
    }

    public List<GuideBook> queryAllByScene(String scene) {
        Cursor cursor = null;
        List<GuideBook> guideBookList = new ArrayList<>();
        GuideBook guideBook;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommonContract.GUIDE_BOOK_TABLE_NAME, null,
                    CommonContract.GUIDEBOOK_COLUMNS.SCENE + "=?", new String[]{scene},
                    null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        guideBook = new GuideBook(cursor);
                        guideBookList.add(guideBook);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return guideBookList;
    }

    public List<GuideBook> queryByScene(String scene) {
        Cursor cursor = null;
        List<GuideBook> guideBookList = new ArrayList<>();
        GuideBook guideBook;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommonContract.GUIDE_BOOK_TABLE_NAME, null,
                    CommonContract.GUIDEBOOK_COLUMNS.SCENE + "=?",
                    new String[]{scene}, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        guideBook = new GuideBook(cursor);
                        guideBookList.add(guideBook);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return guideBookList;
    }

    public GuideBook queryById(long id) {
        Cursor cursor = null;
        GuideBook guideBook = null;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommonContract.GUIDE_BOOK_TABLE_NAME, null,
                    CommonContract.GUIDEBOOK_COLUMNS._ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    guideBook = new GuideBook(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return guideBook;
    }

    public List<GuideBook> queryByCondition(String scene, String priority) {
        Cursor cursor = null;
        List<GuideBook> guideBookList = new ArrayList<>();
        GuideBook guideBook;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommonContract.GUIDE_BOOK_TABLE_NAME, null,
                    CommonContract.GUIDEBOOK_COLUMNS.SCENE + "=? AND " + CommonContract.GUIDEBOOK_COLUMNS.PRIORITY + "=?",
                    new String[]{scene, priority}, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        guideBook = new GuideBook(cursor);
                        guideBookList.add(guideBook);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return guideBookList;
    }

    public void preSetDatas(List<GuideBook> guideBookList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues;
        try {
            for (GuideBook guideBook : guideBookList) {
                contentValues = GuideBook.createContentValues(guideBook);
                long rows = db.insert(CommonContract.GUIDE_BOOK_TABLE_NAME, null, contentValues);
                Log.d(TAG, "insert return rows:" + rows);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "updateNotificationWhitelist error");
        } finally {
            db.endTransaction();
        }
    }

    public void updateUsageCountById(long id, long usageCount) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CommonContract.GUIDEBOOK_COLUMNS.USAGE_COUNT, usageCount);
        int rows = db.update(CommonContract.GUIDE_BOOK_TABLE_NAME, values, CommonContract.GUIDEBOOK_COLUMNS._ID + "=?",
                new String[]{String.valueOf(id)});
        LogUtils.d(TAG, "updateUsageCountById rows:" + rows);
    }

    public void updatePriorityById(long id, String priority) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CommonContract.GUIDEBOOK_COLUMNS.PRIORITY, priority);
        int rows = db.update(CommonContract.GUIDE_BOOK_TABLE_NAME, values, CommonContract.GUIDEBOOK_COLUMNS._ID + "=?",
                new String[]{String.valueOf(id)});
        LogUtils.d(TAG, "updatePriorityById rows:" + rows);
    }

    public void deleteById(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows = db.delete(CommonContract.GUIDE_BOOK_TABLE_NAME, CommonContract.GUIDEBOOK_COLUMNS._ID + "=?", new String[]{String.valueOf(id)});
        LogUtils.d(TAG, "deleteById rowid:" + rows);
    }

}
